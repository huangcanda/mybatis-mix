package org.wanghailu.mybatismix.example.simple;


import org.wanghailu.mybatismix.constant.SqlSymbolConstant;
import org.wanghailu.mybatismix.example.criteria.BaseCriteria;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * 进行条件设置
 *
 * @author cdhuang
 * @date 2023/3/22
 */
public class SimpleCriteria extends BaseCriteria<SimpleCriteria> {

    public SimpleCriteria(Class entityClass, boolean isOrLogic) {
        super(entityClass,isOrLogic);
    }

    /**
     * and条件中嵌套or条件链
     * @param func
     * @return
     */
    public SimpleCriteria or(Consumer<SimpleCriteria> func) {
        SimpleCriteria orLambdaCriteria = new SimpleCriteria(entityClass,true);
        func.accept(orLambdaCriteria);
        addCriterion(orLambdaCriteria);
        return this;
    }

    /**
     * or条件中嵌套and条件链
     * @param func
     * @return
     */
    public SimpleCriteria and(Consumer<SimpleCriteria> func) {
        SimpleCriteria andLambdaCriteria = new SimpleCriteria(entityClass,false);
        func.accept(andLambdaCriteria);
        addCriterion(andLambdaCriteria);
        return this;
    }

    /**
     * 传入 AbcEntity::getXyz ,即为条件 xyz is null
     *
     * @param fieldName
     * @return
     */
    public SimpleCriteria isNull(String fieldName) {
        addCriterion(fieldName, SqlSymbolConstant.IS_NULL);
        return this;
    }

    /**
     * 传入  AbcEntity::getXyz ,即为条件 xyz is not null
     *
     * @param fieldName
     * @return
     */
    public SimpleCriteria isNotNull(String fieldName) {
        addCriterion(fieldName, SqlSymbolConstant.IS_NOT_NULL);
        return this;
    }

    /**
     * 等于，比如：传入  AbcEntity::getXyz ,即为条件 xyz = value
     *
     * @param fieldName
     * @return
     */
    public SimpleCriteria eq(String fieldName, Object value) {
        addCriterion(fieldName, SqlSymbolConstant.EQ, value);
        return this;
    }

    /**
     * 不等于，比如：传入  AbcEntity::getXyz ,即为条件 xyz <> value
     *
     * @param fieldName
     * @return
     */
    public SimpleCriteria notEq(String fieldName, Object value) {
        addCriterion(fieldName, SqlSymbolConstant.NOT_EQ, value);
        return this;
    }

    /**
     * 大于，比如：传入  AbcEntity::getXyz ,即为条件 xyz > value
     *
     * @param fieldName
     * @return
     */
    public SimpleCriteria gt(String fieldName, Object value) {
        addCriterion(fieldName, SqlSymbolConstant.GT, value);
        return this;
    }


    /**
     * 大于等于，比如：传入  AbcEntity::getXyz ,即为条件 xyz >= value
     *
     * @param fieldName
     * @return
     */
    public SimpleCriteria ge(String fieldName, Object value) {
        addCriterion(fieldName, SqlSymbolConstant.GE, value);
        return this;
    }

    /**
     * 小于，比如：传入  AbcEntity::getXyz ,即为条件 xyz < value
     *
     * @param fieldName
     * @return
     */
    public SimpleCriteria lt(String fieldName, Object value) {
        addCriterion(fieldName, SqlSymbolConstant.LT, value);
        return this;
    }

    /**
     * 小于等于，比如：传入  AbcEntity::getXyz ,即为条件 xyz <= value
     *
     * @param fieldName
     * @return
     */
    public SimpleCriteria le(String fieldName, Object value) {
        addCriterion(fieldName, SqlSymbolConstant.LE, value);
        return this;
    }

    /**
     * 传入  AbcEntity::getXyz ,即为条件 xyz like value
     *
     * @param fieldName
     * @return
     */
    public SimpleCriteria like(String fieldName, Object value) {
        addCriterion(fieldName, SqlSymbolConstant.LIKE, "%" + value + "%");
        return this;
    }

    /**
     * 传入  AbcEntity::getXyz ,即为条件 xyz like value
     *
     * @param fieldName
     * @return
     */
    public SimpleCriteria likeLeft(String fieldName, Object value) {
        addCriterion(fieldName, SqlSymbolConstant.LIKE, "%" + value);
        return this;
    }

    /**
     * 传入  AbcEntity::getXyz ,即为条件 xyz like value
     *
     * @param fieldName
     * @return
     */
    public SimpleCriteria likeRight(String fieldName, Object value) {
        addCriterion(fieldName, SqlSymbolConstant.LIKE, value + "%");
        return this;
    }

    /**
     * 传入  AbcEntity::getXyz ,即为条件 xyz in (value1,value2,value3,...)
     *
     * @param fieldName
     * @return
     */
    public SimpleCriteria in(String fieldName, List<Object> values) {
        addCriterion(fieldName, SqlSymbolConstant.IN, values);
        return this;
    }

    /**
     * 传入  AbcEntity::getXyz ,即为条件 xyz in (value1,value2,value3,...)
     *
     * @param fieldName
     * @return
     */
    public SimpleCriteria in(String fieldName, Object... values) {
        addCriterion(fieldName, SqlSymbolConstant.IN, Arrays.asList(values));
        return this;
    }

    /**
     * 传入  AbcEntity::getXyz ,即为条件 xyz not in (value1,value2,value3,...)
     *
     * @param fieldName
     * @return
     */
    public SimpleCriteria notIn(String fieldName, List<Object> values) {
        addCriterion(fieldName, SqlSymbolConstant.NOT_IN, values);
        return this;
    }

    /**
     * 传入  AbcEntity::getXyz ,即为条件 xyz not in (value1,value2,value3,...)
     *
     * @param fieldName
     * @return
     */
    public SimpleCriteria notIn(String fieldName, Object... values) {
        addCriterion(fieldName, SqlSymbolConstant.NOT_IN, Arrays.asList(values));
        return this;
    }

    /**
     * 传入  AbcEntity::getXyz ,即为条件 xyz between value1 and value2
     *
     * @param fieldName
     * @return
     */
    public SimpleCriteria between(String fieldName, Object value1, Object value2) {
        addCriterion(fieldName, SqlSymbolConstant.BETWEEN, value1, value2);
        return this;
    }

    /**
     * 传入  AbcEntity::getXyz ,即为条件 xyz not between value1 and value2
     *
     * @param fieldName
     * @return
     */
    public SimpleCriteria notBetween(String fieldName, Object value1, Object value2) {
        addCriterion(fieldName, SqlSymbolConstant.NOT_BETWEEN, value1, value2);
        return this;
    }
    
}
