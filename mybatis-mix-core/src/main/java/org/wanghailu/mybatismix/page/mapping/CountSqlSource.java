package org.wanghailu.mybatismix.page.mapping;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.dialect.DialectManager;
import org.wanghailu.mybatismix.page.countsql.CountSqlManager;

/**
 * 生成分页count语句的SqlSource
 */
public class CountSqlSource implements SqlSource {


    private MappedStatement oldMappedStatement;


    public CountSqlSource(MappedStatement oldMappedStatement) {
        this.oldMappedStatement = oldMappedStatement;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        BoundSql originalBoundSql = oldMappedStatement.getBoundSql(parameterObject);
        MybatisMixConfiguration configuration = (MybatisMixConfiguration) oldMappedStatement.getConfiguration();
        CountSqlManager countSqlManager = configuration.getManager(CountSqlManager.class);
        return configuration.getManager(DialectManager.class).getDialect().getCountBoundSql(oldMappedStatement, originalBoundSql,countSqlManager);
    }
}
