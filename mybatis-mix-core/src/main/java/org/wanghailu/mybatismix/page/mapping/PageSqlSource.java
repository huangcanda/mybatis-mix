package org.wanghailu.mybatismix.page.mapping;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.dialect.DialectManager;
import org.wanghailu.mybatismix.model.Pageable;
import org.wanghailu.mybatismix.page.PageHelper;

/**
 * 生成分页limit语句的SqlSource
 * @author cdhuang
 * @date 2021/9/27
 */
public class PageSqlSource implements SqlSource {

    public static final String pageParameterName = "__page__";

    private MappedStatement oldMappedStatement;


    public PageSqlSource(MappedStatement oldMappedStatement) {
        this.oldMappedStatement = oldMappedStatement;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        BoundSql originalBoundSql = oldMappedStatement.getBoundSql(parameterObject);
        Pageable page = PageHelper.getLocalPage();
        MybatisMixConfiguration configuration = (MybatisMixConfiguration) oldMappedStatement.getConfiguration();
        BoundSql boundSql = configuration.getManager(DialectManager.class).getDialect().getPageBoundSql(oldMappedStatement, originalBoundSql, page);
        if(page.isSqlParamMode()){
            boundSql.setAdditionalParameter(pageParameterName, page);
        }
        return boundSql;
    }
}
