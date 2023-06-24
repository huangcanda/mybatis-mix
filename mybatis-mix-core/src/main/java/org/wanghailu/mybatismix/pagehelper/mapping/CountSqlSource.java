package org.wanghailu.mybatismix.pagehelper.mapping;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.dialect.DialectManager;
import org.wanghailu.mybatismix.pagehelper.countsql.CountSqlManager;

/**
 * 生成分页count语句的SqlSource
 * @author cdhuang
 * @date 2021/9/27
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
