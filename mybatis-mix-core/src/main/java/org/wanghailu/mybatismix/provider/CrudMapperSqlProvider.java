package org.wanghailu.mybatismix.provider;


import org.wanghailu.mybatismix.model.AdditionalParameters;
import org.wanghailu.mybatismix.support.EntityPropertyDescriptor;
import org.wanghailu.mybatismix.util.BeanInvokeUtils;
import org.wanghailu.mybatismix.util.EntityUtils;
import org.wanghailu.mybatismix.util.PrivateStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 根据jpa信息，自动生成增删改查
 */
public class CrudMapperSqlProvider implements MapperSqlProvider {
    
    
    private Class entityClass;
    
    public CrudMapperSqlProvider(Class aClass) {
        this.entityClass = aClass;
    }
    
    public String selectAll(Map<String, Object> param) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ");
        sb.append(EntityUtils.getTableName(entityClass));
        return sb.toString();
    }
    
    /**
     * 根据主键查询
     *
     * @param param
     * @return
     */
    public String selectById(Map<String, Object> param) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ");
        sb.append(EntityUtils.getTableName(entityClass));
        sb.append(" WHERE ");
        sb.append(EntityUtils.getPrimaryKeyColumnName(entityClass));
        sb.append(" = #{primaryKey}");
        return sb.toString();
    }
    
    public String selectListByIds(Map<String, Object> primaryKeyMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ");
        sb.append(EntityUtils.getTableName(entityClass));
        sb.append(" WHERE ");
        sb.append(EntityUtils.getPrimaryKeyColumnName(entityClass));
        sb.append(" IN (");
        List<String> primaryKeyList = new ArrayList<>();
        for (String primaryKey : primaryKeyMap.keySet()) {
            primaryKeyList.add("#{" + primaryKey + "}");
        }
        sb.append(PrivateStringUtils.join(primaryKeyList, ","));
        sb.append(")");
        return sb.toString();
    }
    
    /**
     * 根据主键删除
     *
     * @param param
     * @return
     */
    public String delete(Map<String, Object> param) {
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ");
        sb.append(EntityUtils.getTableName(entityClass));
        sb.append(" WHERE ");
        sb.append(EntityUtils.getPrimaryKeyColumnName(entityClass));
        sb.append(" = #{primaryKey}");
        return sb.toString();
    }
    
    /**
     * 根据主键修改有值字段
     *
     * @param param
     * @return
     */
    public String update(Map<String, Object> param) {
        Object entity = param.get("setEntity");
        int updateMode = (int) param.get("updateMode");
        String entityParamNamePrefix = "setEntity.";
        // 主键字段
        EntityPropertyDescriptor primaryKeyPropertyDescriptor = EntityUtils.getPrimaryKeyPropertyDescriptor(entityClass);
        // 版本字段
        EntityPropertyDescriptor versionPropertyDescriptor = EntityUtils.getVersionPropertyDescriptor(entityClass);
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ");
        sb.append(EntityUtils.getTableName(entity.getClass()));
        sb.append(" SET ");
        // 组装update set 的设值部分
        sb.append(SqlProviderHelper
                .getUpdateSetSqlByUpdateMode(entity, updateMode, entity.getClass(), entityParamNamePrefix));
        sb.append(" WHERE ");
        // 组装where部分
        sb.append(primaryKeyPropertyDescriptor.getColumnName());
        sb.append("=#{");
        sb.append(entityParamNamePrefix);
        sb.append(primaryKeyPropertyDescriptor.getFieldName());
        sb.append("}");
        // 版本号作为条件
        if (versionPropertyDescriptor != null) {
            sb.append(" and ");
            sb.append(versionPropertyDescriptor.getColumnName());
            sb.append("=#{");
            sb.append(entityParamNamePrefix);
            sb.append(versionPropertyDescriptor.getFieldName());
            sb.append("}");
        }
        return sb.toString();
    }
    
    /**
     * 插入有值字段
     *
     * @param entity
     * @return
     */
    public String insert(Object entity) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(EntityUtils.getTableName(entity.getClass()));
        StringBuilder nameSql = new StringBuilder();
        StringBuilder valueSql = new StringBuilder();
        String comma = "";
        //取到所有列名
        List<EntityPropertyDescriptor> entityPropertyDescriptorList = EntityUtils
                .getEntityPropertyDescriptor(entityClass);
        entityPropertyDescriptorList = entityPropertyDescriptorList.stream()
                .filter(x -> x.getFieldName() != null && x.getColumnName() != null).collect(Collectors.toList());
        for (EntityPropertyDescriptor entityPropertyDescriptor : entityPropertyDescriptorList) {
            String fieldName = entityPropertyDescriptor.getFieldName();
            Object fieldValue = BeanInvokeUtils.getValueByFieldName(entity, fieldName);
            String columnName = entityPropertyDescriptor.getColumnName();
            //只追加有值实体字段
            if (PrivateStringUtils.isNotEmpty(fieldName) && PrivateStringUtils.isNotEmpty(columnName)
                    && fieldValue != null) {
                nameSql.append(comma);
                valueSql.append(comma);
                nameSql.append(columnName);
                valueSql.append("#{");
                valueSql.append(fieldName);
                valueSql.append("}");
                comma = ",";
            }
        }
        sb.append(" (");
        sb.append(nameSql.toString());
        sb.append(") VALUES (");
        sb.append(valueSql.toString());
        sb.append(")");
        return sb.toString();
    }
    
    public String insertList(Map<String, Object> param) {
        List list = (List) param.get("list");
        List<EntityPropertyDescriptor> entityPropertyDescriptorList = EntityUtils
                .getEntityPropertyDescriptor(entityClass);
        entityPropertyDescriptorList = entityPropertyDescriptorList.stream()
                .filter(x -> x.getFieldName() != null && x.getColumnName() != null).collect(Collectors.toList());
        StringBuilder sb = new StringBuilder("INSERT INTO ");
        sb.append(EntityUtils.getTableName(entityClass));
        sb.append("(");
        sb.append(entityPropertyDescriptorList.stream().map(EntityPropertyDescriptor::getColumnName)
                .collect(Collectors.joining(",")));
        sb.append(") VALUES ");
        String comma = "";
        AdditionalParameters additionalParameters = new AdditionalParameters("");
        param.put("params", additionalParameters.getParams());
        for (Object entity : list) {
            sb.append(comma);
            sb.append("(");
            sb.append(entityPropertyDescriptorList.stream().map(x -> additionalParameters
                    .setParam(BeanInvokeUtils.getValueByFieldName(entity, x.getFieldName())))
                    .collect(Collectors.joining(",")));
            sb.append(")");
            comma = ",";
        }
        return sb.toString();
    }
    
    public String deleteOnInsertLogicTable(Map<String, Object> param) {
        StringBuilder sb = new StringBuilder(SqlProviderHelper.insertLogicDeleteTable(entityClass));
        sb.append(" WHERE ");
        sb.append(EntityUtils.getPrimaryKeyColumnName(entityClass));
        sb.append(" = #{primaryKey}");
        return sb.toString();
    }
}
