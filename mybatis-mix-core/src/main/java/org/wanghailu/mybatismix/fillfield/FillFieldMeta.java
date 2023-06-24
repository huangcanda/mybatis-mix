package org.wanghailu.mybatismix.fillfield;

import org.wanghailu.mybatismix.annotation.FillField;
import org.wanghailu.mybatismix.constant.ConfigurationKeyConstant;
import org.wanghailu.mybatismix.exception.MybatisMixException;
import org.wanghailu.mybatismix.fillfield.condition.FillFieldConditionOnInsert;
import org.wanghailu.mybatismix.fillfield.condition.FillFieldConditionOnUpdate;
import org.wanghailu.mybatismix.fillfield.strategy.FillDefaultVersion;
import org.wanghailu.mybatismix.fillfield.strategy.FillPrimaryId;
import org.wanghailu.mybatismix.fillfield.strategy.FillUpdatedVersion;
import org.wanghailu.mybatismix.support.EntityPropertyDescriptor;
import org.wanghailu.mybatismix.util.EntityUtils;
import org.wanghailu.mybatismix.util.MybatisContext;
import org.wanghailu.mybatismix.util.PrivateStringUtils;
import org.wanghailu.mybatismix.util.ReflectUtils;
import org.wanghailu.mybatismix.util.TruckUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author cdhuang
 * @date 2023/2/8
 */
public class FillFieldMeta {
    
    private static Map<Class, FillFieldMeta> fillFieldMetaMap = new HashMap<>();
    
    
    public static FillFieldMeta getFillFieldMeta(Class entityClass) {
        FillFieldMeta meta = fillFieldMetaMap.get(entityClass);
        if (meta == null) {
            synchronized (fillFieldMetaMap) {
                meta = fillFieldMetaMap.get(entityClass);
                if (meta == null) {
                    meta = initFillFieldMeta(entityClass);
                    fillFieldMetaMap.put(entityClass, meta);
                }
            }
        }
        return meta;
    }
    
    private Map<String, Map<String, String>> fillFieldMapBeforeInvoke;

    private Map<String, Map<String, String>> fillFieldMapAfterInvoke;
    
    public Map<String, Map<String, String>> getFillFieldMapBeforeInvoke() {
        return fillFieldMapBeforeInvoke;
    }

    public Map<String, Map<String, String>> getFillFieldMapAfterInvoke() {
        return fillFieldMapAfterInvoke;
    }

    private static FillFieldMeta initFillFieldMeta(Class entityClass) {
        FillFieldMeta meta = new FillFieldMeta();
        Map<String, Map<String, String>> fillFieldMapBeforeInvoke = new HashMap<>(8);
        Map<String, Map<String, String>> fillFieldMapAfterInvoke = new HashMap<>(8);
        meta.fillFieldMapBeforeInvoke = fillFieldMapBeforeInvoke;
        meta.fillFieldMapAfterInvoke = fillFieldMapAfterInvoke;
        for (EntityPropertyDescriptor entityPropertyDescriptor : EntityUtils.getEntityPropertyDescriptor(entityClass)) {
            List<FillField> fillFields = ReflectUtils
                    .findAnnotation(entityPropertyDescriptor.getField(), FillField.class);
            if (TruckUtils.isEmpty(fillFields)) {
                fillFields = ReflectUtils.findAnnotation(entityPropertyDescriptor.getGetMethod(), FillField.class);
            }
            String fieldName = entityPropertyDescriptor.getFieldName();
            for (FillField fillField : fillFields) {
                String condition = fillField.condition();
                String strategy = fillField.strategy();
                if(strategyInMapBeforeInvoke(strategy)){
                    putStrategyInMap(fillFieldMapBeforeInvoke,fieldName,condition,strategy,true);
                }
                if(strategyInMapAfterInvoke(strategy)){
                    putStrategyInMap(fillFieldMapAfterInvoke,fieldName,condition,strategy,true);
                }
            }
        }

        String idDefaultStrategy = MybatisContext.getConfiguration().getProperty(ConfigurationKeyConstant.fillField$idDefaultStrategy,FillPrimaryId.STRATEGY_NAME);
        putStrategyInMap(fillFieldMapBeforeInvoke, EntityUtils.getPrimaryKeyFieldName(entityClass),FillFieldConditionOnInsert.CONDITION_NAME,idDefaultStrategy,false);

        String versionFieldName = EntityUtils.getVersionFieldName(entityClass);
        if(PrivateStringUtils.isNotEmpty(versionFieldName)){
            putStrategyInMap(fillFieldMapBeforeInvoke,versionFieldName,FillFieldConditionOnInsert.CONDITION_NAME,FillDefaultVersion.STRATEGY_NAME,true);
            putStrategyInMap(fillFieldMapAfterInvoke,versionFieldName,FillFieldConditionOnUpdate.CONDITION_NAME, FillUpdatedVersion.STRATEGY_NAME,true);
        }



        return meta;
    }

    private static void putStrategyInMap(Map<String, Map<String, String>> fillFieldMap,String fieldName,String condition,String strategy,boolean replace){
        Map<String, String> fieldStrategyMap = fillFieldMap.get(condition);
        if(fieldStrategyMap==null){
            fieldStrategyMap = new HashMap<>(8);
            fillFieldMap.put(condition,fieldStrategyMap);
        }
        if(replace){
            String oldStrategy = fieldStrategyMap.put(fieldName, strategy);
            if (oldStrategy != null && !oldStrategy.equals(strategy)) {
                throw new MybatisMixException(
                        "条件" + condition + "，字段" + fieldName + "，存在多个策略：" + oldStrategy + "," + strategy);
            }
        }else{
            if(!fieldStrategyMap.containsKey(fieldName)){
                fieldStrategyMap.put(fieldName,strategy);
            }
        }

    }

    private static boolean strategyInMapBeforeInvoke(String strategyName){
        FillFieldStrategy strategy=  FillFieldManager.strategyMap.get(strategyName);
        if(strategy==null){
            throw new MybatisMixException("找不到对应的填充值策略，策略名："+strategyName);
        }
        if(strategy instanceof FillFieldEnhanceStrategy && ((FillFieldEnhanceStrategy) strategy).onlyDeclareAfterInvoke()){
            return false;
        }else{
            return true;
        }
    }

    private static boolean strategyInMapAfterInvoke(String strategyName){
        FillFieldStrategy strategy=  FillFieldManager.strategyMap.get(strategyName);
        if(strategy==null){
            throw new MybatisMixException("找不到对应的填充值策略，策略名："+strategyName);
        }
        return strategy instanceof FillFieldEnhanceStrategy;
    }
}
