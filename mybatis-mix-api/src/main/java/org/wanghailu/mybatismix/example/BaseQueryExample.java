package org.wanghailu.mybatismix.example;

import org.wanghailu.mybatismix.util.TruckUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseQueryExample<ENTITY, CHILD extends BaseQueryExample<ENTITY, CHILD>> extends BaseExample<ENTITY> {

    protected List<String> selectList;

    protected List<String> groupByList;

    protected List<String> orderByList;

    protected boolean forUpdate;

    
    public BaseQueryExample(Class<ENTITY> entityClass) {
        super(entityClass);
    }
    
    /**
     * 指定查询的字段，传入字符串
     * @param column
     * @return
     */
    public CHILD select(String column) {
        addSelect(column);
        return (CHILD)this;
    }
    
    /**
     * 指定分组的字段，传入字符串
     * @param groupBy
     * @return
     */
    public CHILD groupBy(String groupBy) {
        addGroupBy(groupBy);
        return (CHILD)this;
    }
    
    /**
     * 指定排序的字段，传入字符串
     * @param orderBy
     * @return
     */
    public CHILD orderByAsc(String orderBy) {
        addOrderBy(orderBy);
        return (CHILD)this;
    }
    
    /**
     * 指定排序的字段，传入字符串
     * @param orderBy
     * @return
     */
    public CHILD orderByDesc(String orderBy) {
        addOrderBy(orderBy);
        return (CHILD)this;
    }
    

    protected void addSelect(String columnName) {
        if (selectList == null) {
            selectList = new ArrayList<>();
        }
        if (TruckUtils.isEmpty(columnName)) {
            throw new NullPointerException();
        }
        selectList.add(columnName);
    }

    /**
     * 指定分组的字段，传入字符串
     * @param groupBy
     * @return
     */
    protected void addGroupBy(String groupBy) {
        if (groupByList == null) {
            groupByList = new ArrayList<>();
        }
        groupByList.add(groupBy);
    }

    /**
     * 指定排序的字段，传入字符串
     * @param orderBy
     * @return
     */
    protected void addOrderBy(String orderBy) {
        if (orderByList == null) {
            orderByList = new ArrayList<>();
        }
        orderByList.add(orderBy);
    }

    protected List<String> getSelectList() {
        return selectList;
    }

    protected List<String> getGroupByList() {
        return groupByList;
    }

    protected List<String> getOrderByList() {
        return orderByList;
    }

    protected boolean isForUpdate() {
        return forUpdate;
    }

    public void clear() {
        selectList = null;
        whereCondition= null;
        groupByList = null;
        orderByList = null;
        forUpdate = false;
        additionalParameters.clear();
    }
}
