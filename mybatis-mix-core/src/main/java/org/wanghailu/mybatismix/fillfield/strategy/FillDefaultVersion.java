package org.wanghailu.mybatismix.fillfield.strategy;

import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.fillfield.FillFieldStrategy;
import org.wanghailu.mybatismix.support.EntityPropertyDescriptor;
import org.wanghailu.mybatismix.util.EntityUtils;
import org.wanghailu.mybatismix.util.TruckUtils;

public class FillDefaultVersion implements FillFieldStrategy {

    public static final String STRATEGY_NAME = "defaultVersion";

    @Override
    public String name() {
        return FillDefaultVersion.STRATEGY_NAME;
    }

    @Override
    public Object fillValueBeforeInvoke(Object entity, String fieldName, MybatisMixConfiguration configuration) {
        EntityPropertyDescriptor propertyDescriptor = EntityUtils
                .getVersionPropertyDescriptor(entity.getClass());
        Class fieldType = propertyDescriptor.getGetMethod().getReturnType();
        return TruckUtils.convertSimpleType(1,fieldType);
    }
}
