package org.wanghailu.mybatismix.example.criteria;

import org.wanghailu.mybatismix.support.EntityPropertyDescriptor;
import org.wanghailu.mybatismix.util.EntityUtils;
import org.wanghailu.mybatismix.util.TruckUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 表示一串的条件
 *
 * @author cdhuang
 * @date 2021/2/2
 */
public class BaseCriteria<CHILD extends BaseCriteria> implements Serializable {
    
    protected Class entityClass;
    
    /**
     * 多个条件组成条件链
     */
    protected List<Criterion> criterionList;
    
    /**
     * criterionList 之间的逻辑关系，是or还是and
     */
    protected boolean isOrLogic;
    
    /**
     * 无论参数是否为空都添加条件
     */
    protected boolean applyOnCheckArgNotEmpty = false;
    
    /**
     * 根据输入的断言判断是否添加条件
     */
    protected boolean applyOnCondition = true;

    protected boolean autoEndIf = false;
    
    protected BaseCriteria(Class entityClass, boolean isOrLogic) {
        super();
        this.entityClass = entityClass;
        criterionList = new ArrayList<>();
        this.isOrLogic = isOrLogic;
    }
    
    public CHILD ifArgNotEmpty() {
        applyOnCheckArgNotEmpty = true;
        return (CHILD) this;
    }
    
    public CHILD ifCondition(boolean condition) {
        applyOnCondition = condition;
        return (CHILD) this;
    }

    public CHILD endIf(){
        applyOnCheckArgNotEmpty = false;
        applyOnCondition = true;
        return (CHILD) this;
    }

    public CHILD autoEndIf(){
        autoEndIf = true;
        return (CHILD) this;
    }

    protected void checkEndIf() {
        if(autoEndIf){
            endIf();
        }
    }

    
    public boolean isOrLogic() {
        return isOrLogic;
    }
    
    public boolean isValid() {
        return criterionList.size() > 0;
    }
    
    public List<Criterion> getAllCriteria() {
        return criterionList;
    }
    
    private static final Object DEFAULT_OBJECT = new Object();
    
    protected boolean noApplyCheck(Object value) {
        return !applyOnCondition || (applyOnCheckArgNotEmpty && TruckUtils.isEmpty(value));
    }
    
    protected void addCriterion(BaseCriteria c) {
        if (noApplyCheck(DEFAULT_OBJECT)) {
            checkEndIf();
            return;
        }
        checkEndIf();
        criterionList.add(new Criterion(c));
    }
    
    protected void addCriterion(String fieldName, String condition) {
        if (noApplyCheck(DEFAULT_OBJECT)) {
            checkEndIf();
            return;
        }
        checkEndIf();
        String columnName = getColumnNameByFieldName(fieldName);
        criterionList.add(new Criterion(columnName, condition));
    }
    
    protected void addCriterion(String fieldName, String condition, Object value) {
        if (noApplyCheck(value)) {
            checkEndIf();
            return;
        }
        checkEndIf();
        if (value == null) {
            throw new RuntimeException("Value for " + fieldName + " cannot be null");
        }
        String columnName = getColumnNameByFieldName(fieldName);
        criterionList.add(new Criterion(columnName, condition, value));
    }
    
    protected void addCriterion(String fieldName, String condition, Object value1, Object value2) {
        if (noApplyCheck(value1) || noApplyCheck(value2)) {
            checkEndIf();
            return;
        }
        checkEndIf();
        if (value1 == null || value2 == null) {
            throw new RuntimeException("Between values for " + fieldName + " cannot be null");
        }
        String columnName = getColumnNameByFieldName(fieldName);
        criterionList.add(new Criterion(columnName, condition, value1, value2));
    }
    
    protected String getColumnNameByFieldName(String fieldName){
        Map<String, EntityPropertyDescriptor> entityPropertyDescriptorMap= EntityUtils.getEntityDescriptor(entityClass).getFieldPropertyDescriptorMap();
        EntityPropertyDescriptor entityPropertyDescriptor = entityPropertyDescriptorMap.get(fieldName);
        if(entityPropertyDescriptor==null){
            return fieldName;
        }else{
            return entityPropertyDescriptor.getColumnName();
        }
    }

}
