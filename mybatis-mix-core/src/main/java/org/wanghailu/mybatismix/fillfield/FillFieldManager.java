package org.wanghailu.mybatismix.fillfield;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.wanghailu.mybatismix.common.BaseEntitySqlSource;
import org.wanghailu.mybatismix.common.BaseManager;
import org.wanghailu.mybatismix.support.EntityPropertyDescriptor;
import org.wanghailu.mybatismix.util.BeanInvokeUtils;
import org.wanghailu.mybatismix.util.EntityUtils;
import org.wanghailu.mybatismix.util.PrivateStringUtils;
import org.wanghailu.mybatismix.util.SpiExtensionLoader;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.wanghailu.mybatismix.constant.ConfigurationKeyConstant.fillField$defaultStrategy;

/**
 * 字段填充管理器
 * @author cdhuang
 * @date 2023/1/30
 */
public class FillFieldManager extends BaseManager {
    
    protected static Map<String, FillFieldCondition> conditionMap = new HashMap<>();
    
    protected static Map<String, FillFieldStrategy> strategyMap = new HashMap<>();
    
    static {
        conditionMap.putAll(SpiExtensionLoader.loadSpiExtensionMap(FillFieldCondition.class));
        strategyMap.putAll(SpiExtensionLoader.loadSpiExtensionMap(FillFieldStrategy.class));
    }

    public void fillFieldBeforeInvoke(MappedStatement ms, Object parameter){
        fillField(ms, parameter,null,true);
    }

    public void fillFieldAfterInvoke(MappedStatement ms, Object parameter,Object result){
        fillField(ms, parameter,result,false);
    }

    protected void fillField(MappedStatement ms, Object parameter,Object result,boolean isBeforeInvoke){
        Class entityClass = getEntityClass(ms,parameter);
        if(entityClass==null){
            return;
        }
        FillFieldMeta fillFieldMeta = FillFieldMeta.getFillFieldMeta(entityClass);
        Map<String, Map<String, String>> fillFieldMap = isBeforeInvoke?fillFieldMeta.getFillFieldMapBeforeInvoke():fillFieldMeta.getFillFieldMapAfterInvoke();
        if (fillFieldMap==null || fillFieldMap.size()==0) {
            return;
        }
        for (Map.Entry<String, Map<String, String>> entry : fillFieldMap.entrySet()) {
            String conditionName = entry.getKey();
            FillFieldCondition condition = conditionMap.get(conditionName);
            Object entity = condition.getEntity(ms, parameter,null);
            if (entity != null) {
                doFillField(entityClass, entity, entry.getValue(),result,isBeforeInvoke);
            }
            Collection entities = condition.getEntities(ms, parameter,null);
            if (entities != null) {
                for (Object obj : entities) {
                    doFillField(entityClass, obj, entry.getValue(),result,isBeforeInvoke);
                }
            }
        }
    }

    protected void doFillField(Class entityClass, Object entity,
                                           Map<String, String> fieldNameAndStrategyNameMap,Object result,boolean isBeforeInvoke) {
        for (Map.Entry<String, String> entry : fieldNameAndStrategyNameMap.entrySet()) {
            String fieldName = entry.getKey();
            String strategyName = entry.getValue();
            if (PrivateStringUtils.isEmpty(strategyName)) {
                EntityPropertyDescriptor propertyDescriptor = EntityUtils
                        .getPropertyDescriptorByFieldName(entityClass, fieldName);
                Class type = propertyDescriptor.getField().getType();
                strategyName = configuration.getProperty(fillField$defaultStrategy+"." + type.getSimpleName());
                if (PrivateStringUtils.isEmpty(strategyName)) {
                    logger.warn("找不到对应的字段填充策略，类型：{}，字段名：{}", entityClass.getSimpleName(), fieldName);
                    continue;
                }
            }
            FillFieldStrategy strategy = strategyMap.get(strategyName);
            if (strategy.onlyFillWhenOriginalValueIsNull()) {
                Object originalValue = BeanInvokeUtils.getValueByFieldName(entity, fieldName);
                if (originalValue != null) {
                    continue;
                }
            }
            Object value;
            if(isBeforeInvoke){
                value= strategy.fillValueBeforeInvoke(entity, fieldName, configuration);
            }else{
                FillFieldEnhanceStrategy enhanceStrategy = (FillFieldEnhanceStrategy) strategy;
                value= enhanceStrategy.fillValueAfterInvoke(entity, fieldName, configuration,result);
            }
            if (value != null) {
                BeanInvokeUtils.setValueByFieldName(entity, fieldName, value);
            }
        }
    }

    protected Class getEntityClass(MappedStatement ms, Object parameter){
        if(SqlCommandType.SELECT.equals(ms.getSqlCommandType())){
            return null;
        }
        if (ms.getSqlSource() instanceof BaseEntitySqlSource) {
            return ((BaseEntitySqlSource) ms.getSqlSource()).getEntityClass();
        }
        return null;
    }
}
