package org.wanghailu.mybatismix.dialect;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.wanghailu.mybatismix.model.Pageable;
import org.wanghailu.mybatismix.pagehelper.countsql.CountSqlManager;
import org.wanghailu.mybatismix.pagehelper.mapping.CountBoundSql;
import org.wanghailu.mybatismix.support.SpiExtension;

import java.util.Properties;

/**
 * 数据库方言，根据不同的是数据库进行实现。目前主要用于分页时方言处理
 */
public interface Dialect extends SpiExtension {
    
    String getDbType();
    
    /**
     * mybatis分页插件处理pageSql
     *
     * @param originalMappedStatement
     * @param originalBoundSql
     * @param page
     * @return
     */
    BoundSql getPageBoundSql(MappedStatement originalMappedStatement, BoundSql originalBoundSql, Pageable page);

    /**
     * mybatis分页插件处理countSql
     *
     * @param originalMappedStatement
     * @param originalBoundSql
     * @return
     */
    default BoundSql getCountBoundSql(MappedStatement originalMappedStatement, BoundSql originalBoundSql,CountSqlManager countSqlManager) {
        String countSql = countSqlManager.getCountSql(originalBoundSql.getSql(), getDbType());
        return new CountBoundSql(originalMappedStatement.getConfiguration(), originalBoundSql, countSql);
    }
    
    /**
     * 传入参数配置
     * @param properties
     */
    default void setProperties(Properties properties) {}

    @Override
    default String name(){
        return getDbType();
    }
}
