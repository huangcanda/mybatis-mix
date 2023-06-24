package org.wanghailu.mybatismix.dialect;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.wanghailu.mybatismix.constant.DbTypeConstant;
import org.wanghailu.mybatismix.model.Pageable;
import org.wanghailu.mybatismix.pagehelper.mapping.PageBoundSqlBuilder;

/**
 * Mysql方言类
 */
public class MysqlDialect implements Dialect {

    @Override
    public String getDbType() {
        return DbTypeConstant.mysql;
    }

    @Override
    public BoundSql getPageBoundSql(MappedStatement originalMappedStatement, BoundSql originalBoundSql, Pageable page) {
        PageBoundSqlBuilder pageBoundSqlBuilder = new PageBoundSqlBuilder(originalBoundSql, originalMappedStatement.getConfiguration(),page)
                .appendOriginalBoundSql()
                .append(" LIMIT")
                .appendPageParam(Pageable::getOffsetStartPrev)
                .append(",")
                .appendPageParam(Pageable::getPageSize);
        return pageBoundSqlBuilder.build();
    }
}
