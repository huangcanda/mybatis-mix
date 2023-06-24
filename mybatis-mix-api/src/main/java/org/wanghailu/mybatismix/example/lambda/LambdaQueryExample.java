package org.wanghailu.mybatismix.example.lambda;

import org.wanghailu.mybatismix.example.BaseQueryExample;
import org.wanghailu.mybatismix.support.SerializableFunction;
import org.wanghailu.mybatismix.util.TruckUtils;

import java.util.Arrays;
import java.util.function.Consumer;

public class LambdaQueryExample<ENTITY> extends BaseQueryExample<ENTITY, LambdaQueryExample<ENTITY>> {
    
    public static <ENTITY> LambdaQueryExample<ENTITY> from(Class<ENTITY> entityClass) {
        return new LambdaQueryExample<>(entityClass);
    }
    
    public static <ENTITY> LambdaQueryExample<ENTITY> newExample(Class<ENTITY> entityClass) {
        return new LambdaQueryExample<>(entityClass);
    }

    protected LambdaQueryExample(Class<ENTITY> entityClass) {
        super(entityClass);
    }
    
    /**
     * 构建where条件
     *
     * @param func
     * @return
     */
    public LambdaQueryExample<ENTITY> where(Consumer<LambdaCriteria<ENTITY>> func) {
        LambdaCriteria<ENTITY> andLambdaCriteria = new LambdaCriteria<>(entityClass, false);
        func.accept(andLambdaCriteria);
        this.whereCondition = andLambdaCriteria;
        return this;
    }
    
    /**
     * 指定要查询的字段
     *
     * @param column
     * @return
     */
    public LambdaQueryExample<ENTITY> select(SerializableFunction<ENTITY, ?>... column) {
        Arrays.stream(column).forEach(x -> addSelect(getFieldName(x)));
        return this;
    }
    
    /**
     * 指定分组的字段
     *
     * @param column
     * @return
     */
    public LambdaQueryExample<ENTITY> groupBy(SerializableFunction<ENTITY, ?>... column) {
        Arrays.stream(column).forEach(x -> addGroupBy(getFieldName(x)));
        return this;
    }
    
    /**
     * 指定排序的字段 默认升序
     *
     * @param column
     * @return
     */
    public LambdaQueryExample<ENTITY> orderByAsc(SerializableFunction<ENTITY, ?>... column) {
        Arrays.stream(column).forEach(x -> addOrderBy(getFieldName(x) + " asc"));
        return this;
    }
    
    /**
     * 指定排序的字段 倒序
     *
     * @param column
     * @return
     */
    public LambdaQueryExample<ENTITY> orderByDesc(SerializableFunction<ENTITY, ?>... column) {
        Arrays.stream(column).forEach(x -> addOrderBy(getFieldName(x) + " desc"));
        return this;
    }
    
    private String getFieldName(SerializableFunction<ENTITY, ?> func) {
        return TruckUtils.getLambdaFuncFieldName(func);
    }
}
