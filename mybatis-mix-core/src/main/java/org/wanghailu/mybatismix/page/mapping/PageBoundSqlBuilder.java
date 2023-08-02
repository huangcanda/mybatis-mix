package org.wanghailu.mybatismix.page.mapping;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;
import org.wanghailu.mybatismix.model.Pageable;
import org.wanghailu.mybatismix.support.SerializableFunction;
import org.wanghailu.mybatismix.util.BeanInvokeUtils;
import org.wanghailu.mybatismix.util.ReflectUtils;
import org.wanghailu.mybatismix.util.TruckUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 拼接一个分页的BoundSql BoundSql表示一个完整的sql语句，sql字符串(参数用？占位符表示) + 参数
 *
 * @author cdhuang
 * @date 2021/9/28
 */
public class PageBoundSqlBuilder {
    
    private Configuration configuration;
    
    private StringBuilder sqlBuilder = new StringBuilder();
    
    private List<ParameterMapping> parameterMappings = new ArrayList<>();
    
    private BoundSql originalBoundSql;
    
    private Pageable page;
    
    public PageBoundSqlBuilder(BoundSql originalBoundSql, Configuration configuration, Pageable page) {
        this.originalBoundSql = originalBoundSql;
        this.configuration = configuration;
        this.page = page;
    }
    
    /**
     * 表示添加原先的sql语句
     *
     * @return
     */
    public PageBoundSqlBuilder appendOriginalBoundSql() {
        sqlBuilder.append(originalBoundSql.getSql());
        if (originalBoundSql.getParameterMappings() != null) {
            parameterMappings.addAll(originalBoundSql.getParameterMappings());
        }
        return this;
    }
    
    /**
     * 拼接sql语句，比如recordStart
     *
     * @param sql
     * @return
     */
    public PageBoundSqlBuilder append(String sql) {
        sqlBuilder.append(sql);
        return this;
    }
    
    /**
     * 传入Page类的属性
     *
     * @param pageParameterName
     * @return
     */
    public PageBoundSqlBuilder appendPageParam(String pageParameterName) {
        if (page.isSqlParamMode()) {
            sqlBuilder.append(" ? ");
            parameterMappings.add(new ParameterMapping.Builder(configuration,
                    PageSqlSource.pageParameterName + "." + pageParameterName, Object.class).build());
        } else {
            sqlBuilder.append(" ");
            sqlBuilder.append(BeanInvokeUtils.getValueByFieldName(page, pageParameterName));
            sqlBuilder.append(" ");
        }
        return this;
    }
    
    /**
     * 传入Page类的属性（lambda方式，比如Page::getRecordStart）
     *
     * @param function
     * @return
     */
    public PageBoundSqlBuilder appendPageParam(SerializableFunction<Pageable, Long> function) {
        String pageParameterName = TruckUtils.getLambdaFuncFieldName(function);
        return appendPageParam(pageParameterName);
    }
    
    /**
     * 生成新的BoundSql
     *
     * @return
     */
    public BoundSql build() {
        BoundSql boundSql = new BoundSql(configuration, sqlBuilder.toString(), parameterMappings,
                originalBoundSql.getParameterObject());
        Map<String, Object> additionalParameters = (Map<String, Object>) ReflectUtils
                .getFieldValue(originalBoundSql, "additionalParameters");
        for (Map.Entry<String, Object> entry : additionalParameters.entrySet()) {
            boundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
        }
        return boundSql;
    }
}
