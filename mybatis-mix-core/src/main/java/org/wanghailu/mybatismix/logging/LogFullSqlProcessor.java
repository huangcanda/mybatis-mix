package org.wanghailu.mybatismix.logging;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.constant.ConfigurationKeyConstant;
import org.wanghailu.mybatismix.util.PrivateStringUtils;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 打印拼接后的完全sql
 *
 * @author cdhuang
 * @date 2023/1/30
 */
public class LogFullSqlProcessor {
    
    public void logFullSql(MappedStatement mappedStatement, long startTime, BoundSql boundSql, long result) {
        MybatisMixConfiguration configuration = (MybatisMixConfiguration) mappedStatement.getConfiguration();
        long costTime = System.currentTimeMillis() - startTime;
        String sql = getSql(configuration, boundSql);
        if (configuration.getBoolProperty(ConfigurationKeyConstant.logging$formatFullSql, false)) {
            sql = new SqlFormatter().format(sql);
        }
        Log statementLog = mappedStatement.getStatementLog();
        if (statementLog.isDebugEnabled()) {
            String rowStr ="";
            if(result!=-1){
                if(SqlCommandType.SELECT.equals(mappedStatement.getSqlCommandType())){
                    rowStr = " | 结果行数:" + result;
                }else{
                    rowStr = " | 影响行数:" + result;
                }
            }
            statementLog.debug("耗时:" + costTime + "ms" + rowStr + " |sql：" + sql);
        }
    }
    
    protected String getSql(Configuration configuration, BoundSql boundSql) {
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
        if (parameterMappings.size() > 0 && parameterObject != null) {
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = PrivateStringUtils.replaceOnce(sql, "?", getParameterValue(parameterObject));
                
            } else {
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                for (ParameterMapping parameterMapping : parameterMappings) {
                    String propertyName = parameterMapping.getProperty();
                    if (metaObject.hasGetter(propertyName)) {
                        Object obj = metaObject.getValue(propertyName);
                        sql = PrivateStringUtils.replaceOnce(sql, "?", getParameterValue(obj));
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        Object obj = boundSql.getAdditionalParameter(propertyName);
                        sql = PrivateStringUtils.replaceOnce(sql, "?", getParameterValue(obj));
                    }
                }
            }
        }
        return sql;
    }
    
    protected String getParameterValue(Object obj) {
        String value;
        if (obj instanceof String) {
            value = "'" + obj.toString() + "'";
        } else if (obj instanceof Date) {
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            value = "'" + formatter.format((Date) obj) + "'";
        } else {
            if (obj != null) {
                value = obj.toString();
            } else {
                value = "null";
            }
            
        }
        return value;
    }
}
