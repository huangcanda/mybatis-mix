package org.wanghailu.mybatismix.fillfield.condition;

import org.apache.ibatis.mapping.MappedStatement;
import org.wanghailu.mybatismix.common.BaseEntitySqlSource;
import org.wanghailu.mybatismix.fillfield.FillFieldCondition;
import org.wanghailu.mybatismix.mapping.EntityMappedStatementNameEnum;

import java.util.Collection;
import java.util.Map;

/**
 * 填充条件为实体插入
 * @author cdhuang
 * @date 2023/1/30
 */
public class FillFieldConditionOnInsert implements FillFieldCondition {
    
    public static final String CONDITION_NAME ="insert";
    
    @Override
    public String name() {
        return CONDITION_NAME;
    }
    
    @Override
    public Object getEntity(MappedStatement ms, Object parameter,Object result) {
        if (ms.getSqlSource() instanceof BaseEntitySqlSource) {
            BaseEntitySqlSource entitySqlSource = (BaseEntitySqlSource) ms.getSqlSource();
            if(EntityMappedStatementNameEnum.insert.name().equals(entitySqlSource.getSqlSourceName())){
                return parameter;
            }
        }
        return null;
    }
    
    @Override
    public Collection getEntities(MappedStatement ms, Object parameter,Object result) {
        if (ms.getSqlSource() instanceof BaseEntitySqlSource) {
            BaseEntitySqlSource entitySqlSource = (BaseEntitySqlSource) ms.getSqlSource();
            if(EntityMappedStatementNameEnum.insertList.name().equals(entitySqlSource.getSqlSourceName())){
                return (Collection) ((Map)parameter).get("list");
                
            }
        }
        return null;
    }
}
