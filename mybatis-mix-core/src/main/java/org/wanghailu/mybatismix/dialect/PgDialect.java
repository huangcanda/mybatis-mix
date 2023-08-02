package org.wanghailu.mybatismix.dialect;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.wanghailu.mybatismix.constant.DbTypeConstant;
import org.wanghailu.mybatismix.model.Pageable;
import org.wanghailu.mybatismix.page.mapping.PageBoundSqlBuilder;

/**
 * Postgresql方言类
 */
public class PgDialect implements Dialect {

    @Override
    public BoundSql getPageBoundSql(MappedStatement originalMappedStatement, BoundSql originalBoundSql, Pageable page) {
        PageBoundSqlBuilder pageBoundSqlBuilder = new PageBoundSqlBuilder(originalBoundSql,originalMappedStatement.getConfiguration(),page)
                .appendOriginalBoundSql()
                .append(" limit")
                .appendPageParam(Pageable::getPageSize)
                .append("offset")
                .appendPageParam(Pageable::getOffsetStartPrev);
        return pageBoundSqlBuilder.build();
    }
    
    @Override
    public String getDbType() {
        return DbTypeConstant.postgresql;
    }
}
