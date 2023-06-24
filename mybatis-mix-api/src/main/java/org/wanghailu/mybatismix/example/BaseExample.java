package org.wanghailu.mybatismix.example;

import org.wanghailu.mybatismix.example.criteria.BaseCriteria;
import org.wanghailu.mybatismix.model.AdditionalParameters;

import java.io.Serializable;

public abstract class BaseExample<ENTITY> implements Serializable {

    protected Class<ENTITY> entityClass;

    protected BaseCriteria<?> whereCondition;
    
    protected AdditionalParameters additionalParameters = new AdditionalParameters("additionalParameters.");
    
    public BaseExample(Class<ENTITY> entityClass) {
        this.entityClass = entityClass;
    }

    public Class<ENTITY> getEntityClass() {
        return entityClass;
    }

    public BaseCriteria<?> getWhereCondition() {
        return whereCondition;
    }
    
    public AdditionalParameters getAdditionalParameters() {
        return additionalParameters;
    }
}
