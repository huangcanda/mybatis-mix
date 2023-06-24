package org.wanghailu.mybatismix.fillfield.condition;

import org.apache.ibatis.mapping.MappedStatement;
import org.wanghailu.mybatismix.common.BaseEntitySqlSource;
import org.wanghailu.mybatismix.fillfield.FillFieldCondition;
import org.wanghailu.mybatismix.mapping.EntityMappedStatementNameEnum;
import org.wanghailu.mybatismix.util.BeanInvokeUtils;

import java.util.Collection;

/**
 * 填充条件为实体更新
 *
 * @author cdhuang
 * @date 2023/1/30
 */
public class FillFieldConditionOnUpdate implements FillFieldCondition {
    
    public static final String CONDITION_NAME = "update";
    
    @Override
    public String name() {
        return CONDITION_NAME;
    }
    
    @Override
    public Object getEntity(MappedStatement ms, Object parameter, Object result) {
        return getEntityOnUpdate(ms, parameter);
    }
    
    public static Object getEntityOnUpdate(MappedStatement ms, Object parameter) {
        if (ms.getSqlSource() instanceof BaseEntitySqlSource) {
            BaseEntitySqlSource entitySqlSource = (BaseEntitySqlSource) ms.getSqlSource();
            if (EntityMappedStatementNameEnum.update.name().equals(entitySqlSource.getSqlSourceName())
                    || EntityMappedStatementNameEnum.updateByExample.name()
                    .equals(entitySqlSource.getSqlSourceName())) {
                return BeanInvokeUtils.getValueByFieldName(parameter, "setEntity");
            }
        }
        return null;
    }
    
    @Override
    public Collection getEntities(MappedStatement ms, Object parameter, Object result) {
        return null;
    }
}
