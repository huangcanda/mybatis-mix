package org.wanghailu.mybatismix.mapping;

import org.wanghailu.mybatismix.exception.MybatisMixException;
import org.wanghailu.mybatismix.mapper.impl.BaseMapperImpl;

/**
 * 使用枚举类，列举所有的增删改查方法，方法出自SimpleSqlProvider，LambdaSqlProvider，ConditionSqlProvider
 * @author cdhuang
 * @date 2022/7/4
 */
@SuppressWarnings("ALL")
public enum EntityMappedStatementNameEnum {
    
    selectAll,
    selectById,
    selectListByIds,
    delete,
    update,
    insert,
    insertList,
    deleteOnInsertLogicTable,
    selectByExample,
    countByExample,
    deleteByExample,
    updateByExample,
    deleteOnInsertLogicTableByExample;
    
    public String getStatementId(Object object) {
        Class clazz;
        if (object == null) {
            throw new MybatisMixException("无法操作空对象");
        }
        if(object instanceof Class){
            clazz = (Class) object;
        }else if(object instanceof BaseMapperImpl){
            clazz =((BaseMapperImpl) object).getEntityClass();
        }else{
            clazz = object.getClass();
        }
        return EntityMappedStatementCreator.getNamespaceKey(clazz) + "." + this.name();
    }
}
