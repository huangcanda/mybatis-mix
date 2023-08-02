package org.wanghailu.mybatismix.page.mapping;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.wanghailu.mybatismix.mapping.BaseMappedStatementSupplier;

/**
 * 定义创建分页MappedStatement的逻辑
 */
public class PageMappedStatementSupplier extends BaseMappedStatementSupplier {


    private static String statementSuffix = ".AutoLimit";

    public PageMappedStatementSupplier(MappedStatement oldMappedStatement) {
        super(oldMappedStatement);
        this.id = oldMappedStatement.getId() + statementSuffix;
    }

    @Override
    public SqlSource getSqlSource() {
        return new PageSqlSource(oldMappedStatement);
    }
}
