package org.wanghailu.mybatismix.example.lambda;

import org.wanghailu.mybatismix.example.BaseDeleteExample;

import java.util.function.Consumer;

public class LambdaDeleteExample<ENTITY> extends BaseDeleteExample<ENTITY> {

    public static <ENTITY> LambdaDeleteExample<ENTITY> from(Class<ENTITY> entityClass) {
        return new LambdaDeleteExample<>(entityClass);
    }
    
    public static <ENTITY> LambdaDeleteExample<ENTITY> newExample(Class<ENTITY> entityClass) {
        return new LambdaDeleteExample<>(entityClass);
    }

    protected LambdaDeleteExample(Class<ENTITY> entityClass) {
        super(entityClass);
    }

    /**
     * 构建where条件
     * @param func
     * @return
     */
    public LambdaDeleteExample<ENTITY> where(Consumer<LambdaCriteria<ENTITY>> func) {
        LambdaCriteria<ENTITY> andLambdaCriteria = new LambdaCriteria<>(entityClass,false);
        func.accept(andLambdaCriteria);
        this.whereCondition = andLambdaCriteria;
        return this;
    }
}
