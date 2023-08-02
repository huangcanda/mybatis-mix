package org.wanghailu.mybatismix.dialect;


import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.wanghailu.mybatismix.constant.DbTypeConstant;
import org.wanghailu.mybatismix.model.Pageable;
import org.wanghailu.mybatismix.page.mapping.PageBoundSqlBuilder;

/**
 * Oracle方言
 */
public class OracleDialect implements Dialect {

    @Override
    public String getDbType() {
        return DbTypeConstant.oracle;
    }

    @Override
    public BoundSql getPageBoundSql(MappedStatement originalMappedStatement, BoundSql originalBoundSql, Pageable page) {
        PageBoundSqlBuilder pageBoundSqlBuilder = new PageBoundSqlBuilder(originalBoundSql,originalMappedStatement.getConfiguration(),page)
                .append("SELECT * FROM ( ")
                .append(" SELECT TMP_PAGE.*, ROWNUM ROW_ID FROM ( ")
                .appendOriginalBoundSql()
                .append(" ) TMP_PAGE WHERE ROWNUM <=")
                .appendPageParam(Pageable::getOffsetEnd)
                .append(") WHERE ROW_ID >")
                .appendPageParam(Pageable::getOffsetStartPrev);
        return pageBoundSqlBuilder.build();
    }
}
