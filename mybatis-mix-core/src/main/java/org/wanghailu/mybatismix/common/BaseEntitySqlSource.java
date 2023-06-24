package org.wanghailu.mybatismix.common;

import org.apache.ibatis.mapping.SqlSource;

/**
 * @author cdhuang
 * @date 2023/4/4
 */
public abstract class BaseEntitySqlSource implements SqlSource {
    
    protected Class<?> entityClass;
    
    public BaseEntitySqlSource(Class<?> entityClass) {
        this.entityClass = entityClass;
    }
    
    public Class<?> getEntityClass() {
        return entityClass;
    }
    
    /**
     * 目前主要是在fillField模块中用到，约定 insert开头和update开头特殊处理
     * @return
     */
    abstract public String getSqlSourceName();
}
