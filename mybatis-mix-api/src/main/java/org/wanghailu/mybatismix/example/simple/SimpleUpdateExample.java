package org.wanghailu.mybatismix.example.simple;

import org.wanghailu.mybatismix.example.BaseUpdateExample;

import java.util.function.Consumer;

public class SimpleUpdateExample<ENTITY> extends BaseUpdateExample<ENTITY, SimpleUpdateExample<ENTITY>> {
    
    public static <ENTITY> SimpleUpdateExample<ENTITY> from(Class<ENTITY> entityClass) {
        return new SimpleUpdateExample<>(entityClass);
    }
    
    public static <ENTITY> SimpleUpdateExample<ENTITY> newExample(Class<ENTITY> entityClass) {
        return new SimpleUpdateExample<>(entityClass);
    }

    protected SimpleUpdateExample(Class<ENTITY> entityClass) {
        super(entityClass);
    }
    
    /**
     * 构建where条件
     *
     * @param func
     * @return
     */
    public SimpleUpdateExample<ENTITY> where(Consumer<SimpleCriteria> func) {
        SimpleCriteria andCriteria = new SimpleCriteria(entityClass, false);
        func.accept(andCriteria);
        this.whereCondition = andCriteria;
        return this;
    }
}
