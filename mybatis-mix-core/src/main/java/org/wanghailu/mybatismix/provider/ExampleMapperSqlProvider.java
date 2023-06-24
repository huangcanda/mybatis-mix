package org.wanghailu.mybatismix.provider;

import org.wanghailu.mybatismix.example.BaseDeleteExample;
import org.wanghailu.mybatismix.example.BaseQueryExample;
import org.wanghailu.mybatismix.example.BaseUpdateExample;
import org.wanghailu.mybatismix.example.ExampleHelper;
import org.wanghailu.mybatismix.util.EntityUtils;

public class ExampleMapperSqlProvider implements MapperSqlProvider {
    
    
    /**
     * 条件查询
     *
     * @param example
     * @return
     */
    public String selectByExample(BaseQueryExample example) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append(ExampleHelper.getSelectStr(example));
        sb.append(" FROM ");
        sb.append(EntityUtils.getTableName(example.getEntityClass()));
        sb.append(ExampleHelper.getWhereCondition(example));
        sb.append(ExampleHelper.getGroupByStr(example));
        sb.append(ExampleHelper.getOrderByStr(example));
        sb.append(ExampleHelper.getForUpdateStr(example));
        return sb.toString();
    }
    
    /**
     * 条件查询count
     *
     * @param example
     * @return
     */
    public String countByExample(BaseQueryExample example) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT count(*) as countNum FROM ");
        sb.append(EntityUtils.getTableName(example.getEntityClass()));
        sb.append(ExampleHelper.getWhereCondition(example));
        sb.append(ExampleHelper.getGroupByStr(example));
        sb.append(ExampleHelper.getOrderByStr(example));
        return sb.toString();
    }
    
    /**
     * 条件查询count
     *
     * @param example
     * @return
     */
    public String deleteByExample(BaseDeleteExample example) {
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ");
        sb.append(EntityUtils.getTableName(example.getEntityClass()));
        sb.append(ExampleHelper.getWhereCondition(example));
        return sb.toString();
    }
    
    public String deleteOnInsertLogicTableByExample(BaseDeleteExample example) {
        StringBuilder sb = new StringBuilder(SqlProviderHelper.insertLogicDeleteTable(example.getEntityClass()));
        sb.append(ExampleHelper.getWhereCondition(example));
        return sb.toString();
    }
    
    /**
     * 条件查询count
     *
     * @param example
     * @return
     */
    public String updateByExample(BaseUpdateExample example) {
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ");
        sb.append(EntityUtils.getTableName(example.getEntityClass()));
        sb.append(" SET ");
        sb.append(getUpdateConditionSetSqlByUpdateMode(example));
        sb.append(ExampleHelper.getWhereCondition(example));
        return sb.toString();
    }
    
    protected String getUpdateConditionSetSqlByUpdateMode(BaseUpdateExample example) {
        Class entityClass = example.getEntityClass();
        Object entity = example.getSetEntity();
        int updateMode = ExampleHelper.getUpdateModelByUpdateExample(example);
        String setSql = SqlProviderHelper
                .getUpdateSetSqlByUpdateMode(entity, updateMode, entityClass,"setEntity.");
        return setSql;
    }
}
