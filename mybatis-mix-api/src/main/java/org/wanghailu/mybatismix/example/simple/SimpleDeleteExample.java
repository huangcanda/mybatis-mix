package org.wanghailu.mybatismix.example.simple;

import org.wanghailu.mybatismix.example.BaseDeleteExample;

import java.util.function.Consumer;

public class SimpleDeleteExample<ENTITY> extends BaseDeleteExample<ENTITY> {

    public static <ENTITY> SimpleDeleteExample<ENTITY> from(Class<ENTITY> entityClass) {
        return new SimpleDeleteExample<>(entityClass);
    }
    
    public static <ENTITY> SimpleDeleteExample<ENTITY> newExample(Class<ENTITY> entityClass) {
        return new SimpleDeleteExample<>(entityClass);
    }

    protected SimpleDeleteExample(Class<ENTITY> entityClass) {
        super(entityClass);
    }

    /**
     * 构建where条件
     * @param func
     * @return
     */
    public SimpleDeleteExample<ENTITY> where(Consumer<SimpleCriteria> func) {
        SimpleCriteria andCriteria = new SimpleCriteria(entityClass,false);
        func.accept(andCriteria);
        this.whereCondition = andCriteria;
        return this;
    }
}
