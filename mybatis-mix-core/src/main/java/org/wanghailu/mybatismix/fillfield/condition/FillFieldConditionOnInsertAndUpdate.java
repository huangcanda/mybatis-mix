package org.wanghailu.mybatismix.fillfield.condition;

import org.apache.ibatis.mapping.MappedStatement;

/**
 * 填充条件为实体插入或更新
 * @author cdhuang
 * @date 2023/1/30
 */
public class FillFieldConditionOnInsertAndUpdate extends FillFieldConditionOnInsert {
    
    public static final String CONDITION_NAME ="insert-update";
    
    @Override
    public String name() {
        return CONDITION_NAME;
    }
    
    @Override
    public Object getEntity(MappedStatement ms, Object parameter,Object result) {
        Object entity = super.getEntity(ms, parameter,result);
        if(entity!=null){
            return entity;
        }else{
            return FillFieldConditionOnUpdate.getEntityOnUpdate(ms, parameter);
        }
    }
    
}
