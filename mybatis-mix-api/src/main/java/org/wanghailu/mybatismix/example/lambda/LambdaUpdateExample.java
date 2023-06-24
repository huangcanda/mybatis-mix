package org.wanghailu.mybatismix.example.lambda;

import org.wanghailu.mybatismix.example.BaseUpdateExample;

import java.util.function.Consumer;

public class LambdaUpdateExample<ENTITY> extends BaseUpdateExample<ENTITY,LambdaUpdateExample<ENTITY>> {

    public static <ENTITY> LambdaUpdateExample<ENTITY> from(Class<ENTITY> entityClass) {
        return new LambdaUpdateExample<>(entityClass);
    }
    
    public static <ENTITY> LambdaUpdateExample<ENTITY> newExample(Class<ENTITY> entityClass) {
        return new LambdaUpdateExample<>(entityClass);
    }

    protected LambdaUpdateExample(Class<ENTITY> entityClass) {
        super(entityClass);
    }

    /**
     * 构建where条件
     * @param func
     * @return
     */
    public LambdaUpdateExample<ENTITY> where(Consumer<LambdaCriteria<ENTITY>> func) {
        LambdaCriteria<ENTITY> andLambdaCriteria = new LambdaCriteria<>(entityClass,false);
        func.accept(andLambdaCriteria);
        this.whereCondition = andLambdaCriteria;
        return this;
    }
}
