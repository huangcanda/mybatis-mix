package org.wanghailu.mybatismix.example.simple;

import org.wanghailu.mybatismix.example.BaseQueryExample;

import java.util.function.Consumer;

public class SimpleQueryExample<ENTITY> extends BaseQueryExample<ENTITY, SimpleQueryExample<ENTITY>> {

    public static <ENTITY> SimpleQueryExample<ENTITY> from(Class<ENTITY> entityClass) {
        return new SimpleQueryExample<>(entityClass);
    }
    
    public static <ENTITY> SimpleQueryExample<ENTITY> newExample(Class<ENTITY> entityClass) {
        return new SimpleQueryExample<>(entityClass);
    }

    protected SimpleQueryExample(Class<ENTITY> entityClass) {
        super(entityClass);
    }

    /**
     * 构建where条件
     * @param func
     * @return
     */
    public SimpleQueryExample<ENTITY> where(Consumer<SimpleCriteria> func) {
        SimpleCriteria andCriteria = new SimpleCriteria(entityClass,false);
        func.accept(andCriteria);
        this.whereCondition = andCriteria;
        return this;
    }
}
