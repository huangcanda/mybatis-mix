package org.wanghailu.mybatismix.dialect;


import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.wanghailu.mybatismix.constant.DbTypeConstant;
import org.wanghailu.mybatismix.model.Pageable;
import org.wanghailu.mybatismix.page.mapping.PageBoundSqlBuilder;

public class Db2Dialect implements Dialect {


    @Override
    public String getDbType() {
        return DbTypeConstant.db2;
    }

    @Override
    public BoundSql getPageBoundSql(MappedStatement originalMappedStatement, BoundSql originalBoundSql, Pageable page) {
        PageBoundSqlBuilder pageBoundSqlBuilder = new PageBoundSqlBuilder(originalBoundSql,originalMappedStatement.getConfiguration(),page)
                .append("SELECT * FROM (SELECT TMP_PAGE.*,ROWNUMBER() OVER() AS ROW_ID FROM ( ")
                .appendOriginalBoundSql()
                .append(" ) AS TMP_PAGE) TMP_PAGE WHERE ROW_ID BETWEEN")
                .appendPageParam(Pageable::getOffsetStart)
                .append("AND")
                .appendPageParam(Pageable::getOffsetEnd);
        return pageBoundSqlBuilder.build();
    }
}
