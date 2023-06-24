package org.wanghailu.mybatismix.example;

public abstract class BaseDeleteExample<ENTITY> extends BaseExample<ENTITY> {

    public BaseDeleteExample(Class<ENTITY> entityClass) {
        super(entityClass);
    }
}
