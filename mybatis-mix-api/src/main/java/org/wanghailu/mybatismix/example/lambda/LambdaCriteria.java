package org.wanghailu.mybatismix.example.lambda;


import org.wanghailu.mybatismix.constant.SqlSymbolConstant;
import org.wanghailu.mybatismix.example.criteria.BaseCriteria;
import org.wanghailu.mybatismix.support.SerializableFunction;
import org.wanghailu.mybatismix.util.TruckUtils;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * 使用lambda表达式进行条件设置
 */
public class LambdaCriteria<ENTITY> extends BaseCriteria<LambdaCriteria<ENTITY>> {

    public LambdaCriteria(Class<ENTITY> entityClass, boolean isOrLogic) {
        super(entityClass,isOrLogic);
    }

    /**
     * and条件中嵌套or条件链
     * @param func
     * @return
     */
    public LambdaCriteria<ENTITY> or(Consumer<LambdaCriteria<ENTITY>> func) {
        LambdaCriteria<ENTITY> orLambdaCriteria = new LambdaCriteria<>(entityClass,true);
        func.accept(orLambdaCriteria);
        addCriterion(orLambdaCriteria);
        return this;
    }

    /**
     * or条件中嵌套and条件链
     * @param func
     * @return
     */
    public LambdaCriteria<ENTITY> and(Consumer<LambdaCriteria<ENTITY>> func) {
        LambdaCriteria<ENTITY> andLambdaCriteria = new LambdaCriteria<>(entityClass,false);
        func.accept(andLambdaCriteria);
        addCriterion(andLambdaCriteria);
        return this;
    }

    /**
     * 传入 AbcEntity::getXyz ,即为条件 xyz is null
     *
     * @param column
     * @return
     */
    public LambdaCriteria<ENTITY> isNull(SerializableFunction<ENTITY, ?> column) {
        addCriterion(column, SqlSymbolConstant.IS_NULL);
        return this;
    }

    /**
     * 传入  AbcEntity::getXyz ,即为条件 xyz is not null
     *
     * @param column
     * @return
     */
    public LambdaCriteria<ENTITY> isNotNull(SerializableFunction<ENTITY, ?> column) {
        addCriterion(column, SqlSymbolConstant.IS_NOT_NULL);
        return this;
    }

    /**
     * 等于，比如：传入  AbcEntity::getXyz ,即为条件 xyz = value
     *
     * @param column
     * @return
     */
    public LambdaCriteria<ENTITY> eq(SerializableFunction<ENTITY, ?> column, Object value) {
        addCriterion(column, SqlSymbolConstant.EQ, value);
        return this;
    }

    /**
     * 不等于，比如：传入  AbcEntity::getXyz ,即为条件 xyz <> value
     *
     * @param column
     * @return
     */
    public LambdaCriteria<ENTITY> notEq(SerializableFunction<ENTITY, ?> column, Object value) {
        addCriterion(column, SqlSymbolConstant.NOT_EQ, value);
        return this;
    }

    /**
     * 大于，比如：传入  AbcEntity::getXyz ,即为条件 xyz > value
     *
     * @param column
     * @return
     */
    public LambdaCriteria<ENTITY> gt(SerializableFunction<ENTITY, ?> column, Object value) {
        addCriterion(column, SqlSymbolConstant.GT, value);
        return this;
    }


    /**
     * 大于等于，比如：传入  AbcEntity::getXyz ,即为条件 xyz >= value
     *
     * @param column
     * @return
     */
    public LambdaCriteria<ENTITY> ge(SerializableFunction<ENTITY, ?> column, Object value) {
        addCriterion(column, SqlSymbolConstant.GE, value);
        return this;
    }

    /**
     * 小于，比如：传入  AbcEntity::getXyz ,即为条件 xyz < value
     *
     * @param column
     * @return
     */
    public LambdaCriteria<ENTITY> lt(SerializableFunction<ENTITY, ?> column, Object value) {
        addCriterion(column, SqlSymbolConstant.LT, value);
        return this;
    }

    /**
     * 小于等于，比如：传入  AbcEntity::getXyz ,即为条件 xyz <= value
     *
     * @param column
     * @return
     */
    public LambdaCriteria<ENTITY> le(SerializableFunction<ENTITY, ?> column, Object value) {
        addCriterion(column, SqlSymbolConstant.LE, value);
        return this;
    }

    /**
     * 传入  AbcEntity::getXyz ,即为条件 xyz like value
     *
     * @param column
     * @return
     */
    public LambdaCriteria<ENTITY> like(SerializableFunction<ENTITY, ?> column, Object value) {
        addCriterion(column, SqlSymbolConstant.LIKE, "%" + value + "%");
        return this;
    }

    /**
     * 传入  AbcEntity::getXyz ,即为条件 xyz like value
     *
     * @param column
     * @return
     */
    public LambdaCriteria<ENTITY> likeLeft(SerializableFunction<ENTITY, ?> column, Object value) {
        addCriterion(column, SqlSymbolConstant.LIKE, "%" + value);
        return this;
    }

    /**
     * 传入  AbcEntity::getXyz ,即为条件 xyz like value
     *
     * @param column
     * @return
     */
    public LambdaCriteria<ENTITY> likeRight(SerializableFunction<ENTITY, ?> column, Object value) {
        addCriterion(column, SqlSymbolConstant.LIKE, value + "%");
        return this;
    }

    /**
     * 传入  AbcEntity::getXyz ,即为条件 xyz in (value1,value2,value3,...)
     *
     * @param column
     * @return
     */
    public LambdaCriteria<ENTITY> in(SerializableFunction<ENTITY, ?> column, List<Object> values) {
        addCriterion(column, SqlSymbolConstant.IN, values);
        return this;
    }

    /**
     * 传入  AbcEntity::getXyz ,即为条件 xyz in (value1,value2,value3,...)
     *
     * @param column
     * @return
     */
    public LambdaCriteria<ENTITY> in(SerializableFunction<ENTITY, ?> column, Object... values) {
        addCriterion(column, SqlSymbolConstant.IN, Arrays.asList(values));
        return this;
    }

    /**
     * 传入  AbcEntity::getXyz ,即为条件 xyz not in (value1,value2,value3,...)
     *
     * @param column
     * @return
     */
    public LambdaCriteria<ENTITY> notIn(SerializableFunction<ENTITY, ?> column, List<Object> values) {
        addCriterion(column, SqlSymbolConstant.NOT_IN, values);
        return this;
    }

    /**
     * 传入  AbcEntity::getXyz ,即为条件 xyz not in (value1,value2,value3,...)
     *
     * @param column
     * @return
     */
    public LambdaCriteria<ENTITY> notIn(SerializableFunction<ENTITY, ?> column, Object... values) {
        addCriterion(column, SqlSymbolConstant.NOT_IN, Arrays.asList(values));
        return this;
    }

    /**
     * 传入  AbcEntity::getXyz ,即为条件 xyz between value1 and value2
     *
     * @param column
     * @return
     */
    public LambdaCriteria<ENTITY> between(SerializableFunction<ENTITY, ?> column, Object value1, Object value2) {
        addCriterion(column, SqlSymbolConstant.BETWEEN, value1, value2);
        return this;
    }

    /**
     * 传入  AbcEntity::getXyz ,即为条件 xyz not between value1 and value2
     *
     * @param column
     * @return
     */
    public LambdaCriteria<ENTITY> notBetween(SerializableFunction<ENTITY, ?> column, Object value1, Object value2) {
        addCriterion(column, SqlSymbolConstant.NOT_BETWEEN, value1, value2);
        return this;
    }

    protected void addCriterion(SerializableFunction<ENTITY, ?> column, String condition) {
        String fieldName = TruckUtils.getLambdaFuncFieldName(column);
        addCriterion(fieldName, condition);
    }

    protected void addCriterion(SerializableFunction<ENTITY, ?> column, String condition, Object value) {
        String fieldName = TruckUtils.getLambdaFuncFieldName(column);
        addCriterion(fieldName, condition, value);
    }

    protected void addCriterion(SerializableFunction<ENTITY, ?> column, String condition, Object value1, Object value2) {
        String fieldName = TruckUtils.getLambdaFuncFieldName(column);
        addCriterion(fieldName, condition, value1, value2);
    }
}
