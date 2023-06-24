package org.wanghailu.mybatismix.mapper.jpaquery;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.LanguageDriver;
import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.common.BaseEntitySqlSource;
import org.wanghailu.mybatismix.dialect.DialectManager;
import org.wanghailu.mybatismix.mapping.EntityMappedStatementNameEnum;
import org.wanghailu.mybatismix.model.Page;
import org.wanghailu.mybatismix.pagehelper.mapping.PageSqlSource;

/**
 * @author cdhuang
 * @date 2023/3/31
 */
public class JpaQuerySqlSource extends BaseEntitySqlSource {
    
    private PartTree partTree;
    
    private MybatisMixConfiguration configuration;
    
    private LanguageDriver languageDriver;
    
    private MappedStatement mappedStatement;
    
    private Page page;
    
    public JpaQuerySqlSource(PartTree partTree, MybatisMixConfiguration configuration) {
        super(partTree.getDomainClass());
        this.partTree = partTree;
        this.configuration = configuration;
        this.languageDriver = configuration.getLanguageDriver(null);
        if (partTree.getLimit() != null) {
            page = new Page(1, partTree.getLimit());
            page.setSelectCount(false);
            page.setSqlParamMode(false);
        }
    }
    
    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        MapperMethodArgsMap argsMap = new MapperMethodArgsMap(parameterObject);
        String str = partTree.getSql(argsMap);
        Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, str, parameterType);
        BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
        if (argsMap.additionalParameters != null) {
            boundSql.setAdditionalParameter(MapperMethodArgsMap.ADDITIONAL_PARAMETERS_KEY, argsMap.additionalParameters);
        }
        if (page != null) {
            boundSql = configuration.getManager(DialectManager.class).getDialect()
                    .getPageBoundSql(mappedStatement, boundSql, page);
            boundSql.setAdditionalParameter(PageSqlSource.pageParameterName, page);
        }
        return boundSql;
    }
    
    public void setLanguageDriver(LanguageDriver languageDriver) {
        this.languageDriver = languageDriver;
    }
    
    @Override
    public String getSqlSourceName() {
        SqlCommandType sqlCommandType = partTree.getSqlCommandType();
        if (sqlCommandType == SqlCommandType.UPDATE) {
            return EntityMappedStatementNameEnum.updateByExample.name();
        } else if (sqlCommandType == SqlCommandType.DELETE) {
            return EntityMappedStatementNameEnum.deleteByExample.name();
        } else {
            return partTree.getSource();
        }
    }
    
    public void setMappedStatement(MappedStatement mappedStatement) {
        this.mappedStatement = mappedStatement;
    }
}
