package org.wanghailu.mybatismix.example;

import org.wanghailu.mybatismix.constant.UpdateModeEnum;

public abstract class BaseUpdateExample <ENTITY, CHILD extends BaseUpdateExample<ENTITY, CHILD>> extends BaseExample<ENTITY> {

    protected ENTITY setEntity;
    
    protected int updateModel = UpdateModeEnum.DEFAULT.getValue();

    public BaseUpdateExample(Class<ENTITY> entityClass) {
        super(entityClass);
    }

    public CHILD set(ENTITY setEntity) {
        this.setEntity = setEntity;
        return (CHILD)this;
    }

    public CHILD updateModel(int updateModel) {
        this.updateModel = updateModel;
        return (CHILD)this;
    }

    public ENTITY getSetEntity() {
        return setEntity;
    }
}
