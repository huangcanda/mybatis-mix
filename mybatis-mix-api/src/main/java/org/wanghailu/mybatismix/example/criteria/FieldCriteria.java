package org.wanghailu.mybatismix.example.criteria;

import org.wanghailu.mybatismix.constant.SqlSymbolConstant;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * 某个字段的条件表达式
 * @param <T>
 */
public class FieldCriteria<T extends BaseCriteria> implements Serializable {

    protected T criteria;

    protected String fieldName;
    

    public FieldCriteria(T criteria, String fieldName) {
        this.criteria = criteria;
        this.fieldName = fieldName;
    }
    
    /**
     * 条件 xyz is null
     *
     * @return
     */
    public T isNull() {
        criteria.addCriterion(fieldName, SqlSymbolConstant.IS_NULL);
        return criteria;
    }

    /**
     * 传入  AbcEntity::getXyz ,即为条件 xyz is not null
     *
     * @return
     */
    public T isNotNull() {
        criteria.addCriterion(fieldName, SqlSymbolConstant.IS_NOT_NULL);
        return criteria;
    }

    /**
     * 等于，比如：传入  AbcEntity::getXyz ,即为条件 xyz = value
     *
     * @return
     */
    public T eq(Object value) {
        criteria.addCriterion(fieldName, SqlSymbolConstant.EQ, value);
        return criteria;
    }

    /**
     * 不等于，比如：传入  AbcEntity::getXyz ,即为条件 xyz <> value
     *

     * @return
     */
    public T notEq(Object value) {
        criteria.addCriterion(fieldName, SqlSymbolConstant.NOT_EQ, value);
        return criteria;
    }

    /**
     * 大于，比如：传入  AbcEntity::getXyz ,即为条件 xyz > value
     *
     * @return
     */
    public T gt(Object value) {
        criteria.addCriterion(fieldName, SqlSymbolConstant.GT, value);
        return criteria;
    }


    /**
     * 大于等于，比如：传入  AbcEntity::getXyz ,即为条件 xyz >= value
     *
     * @return
     */
    public T ge(Object value) {
        criteria.addCriterion(fieldName, SqlSymbolConstant.GE, value);
        return criteria;
    }

    /**
     * 小于，比如：传入  AbcEntity::getXyz ,即为条件 xyz < value
     *
     * @return
     */
    public T lt(Object value) {
        criteria.addCriterion(fieldName, SqlSymbolConstant.LT, value);
        return criteria;
    }

    /**
     * 小于等于，比如：传入  AbcEntity::getXyz ,即为条件 xyz <= value
     *
     * @return
     */
    public T le(Object value) {
        criteria.addCriterion(fieldName, SqlSymbolConstant.LE, value);
        return criteria;
    }

    /**
     * 传入  AbcEntity::getXyz ,即为条件 xyz in (value1,value2,value3,...)
     *
     * @return
     */
    public T in(List<Object> values) {
        criteria.addCriterion(fieldName, SqlSymbolConstant.IN, values);
        return criteria;
    }

    /**
     * 传入  AbcEntity::getXyz ,即为条件 xyz in (value1,value2,value3,...)
     *
     * @return
     */
    public T in(Object... values) {
        criteria.addCriterion(fieldName, SqlSymbolConstant.IN, Arrays.asList(values));
        return criteria;
    }

    /**
     * 传入  AbcEntity::getXyz ,即为条件 xyz not in (value1,value2,value3,...)
     *
     * @return
     */
    public T notIn(List<Object> values) {
        criteria.addCriterion(fieldName, SqlSymbolConstant.NOT_IN, values);
        return criteria;
    }

    /**
     * 传入  AbcEntity::getXyz ,即为条件 xyz not in (value1,value2,value3,...)
     *
     * @return
     */
    public T notIn(Object... values) {
        criteria.addCriterion(fieldName, SqlSymbolConstant.NOT_IN, Arrays.asList(values));
        return criteria;
    }

    /**
     * 传入  AbcEntity::getXyz ,即为条件 xyz between value1 and value2
     *
     * @return
     */
    public T between(Object value1, Object value2) {
        criteria.addCriterion(fieldName, SqlSymbolConstant.BETWEEN, value1, value2);
        return criteria;
    }

    /**
     * 传入  AbcEntity::getXyz ,即为条件 xyz not between value1 and value2
     *
     * @return
     */
    public T notBetween(Object value1, Object value2) {
        criteria.addCriterion(fieldName, SqlSymbolConstant.NOT_BETWEEN, value1, value2);
        return criteria;
    }

    /**
     * 传入  AbcEntity::getXyz ,即为条件 xyz like value
     *
     * @return
     */
    public T like(Object value) {
        criteria.addCriterion(fieldName, SqlSymbolConstant.LIKE, "%" + value + "%");
        return criteria;
    }

    /**
     * 传入  AbcEntity::getXyz ,即为条件 xyz like value
     *
     * @return
     */
    public T likeLeft(Object value) {
        criteria.addCriterion(fieldName, SqlSymbolConstant.LIKE, "%" + value);
        return criteria;
    }

    /**
     * 传入  AbcEntity::getXyz ,即为条件 xyz like value
     *
     * @return
     */
    public T likeRight(Object value) {
        criteria.addCriterion(fieldName, SqlSymbolConstant.LIKE, value + "%");
        return criteria;
    }
    
}
