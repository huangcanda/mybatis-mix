package org.wanghailu.mybatismix.fillfield.strategy;

import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.fillfield.FillFieldEnhanceStrategy;
import org.wanghailu.mybatismix.util.BeanInvokeUtils;
import org.wanghailu.mybatismix.util.EntityUtils;
import org.wanghailu.mybatismix.util.TruckUtils;

public class FillUpdatedVersion implements FillFieldEnhanceStrategy {

    public static final String STRATEGY_NAME = "updatedVersion";

    @Override
    public String name() {
        return FillUpdatedVersion.STRATEGY_NAME;
    }

    @Override
    public Object fillValueAfterInvoke(Object entity, String fieldName, MybatisMixConfiguration configuration, Object result) {
        //执行更新抛异常或者更新的影响行数为0的情况
        if(result == null || ((Integer)result).intValue()<=0){
            return null;
        }
        Object versionFieldValue = BeanInvokeUtils.getValueByFieldName(entity, fieldName);
        Class fieldType = EntityUtils.getPropertyDescriptorByFieldName(entity.getClass(), fieldName)
                .getGetMethod().getReturnType();
        long value = 0;
        if(versionFieldValue instanceof Number){
            value = ((Number) versionFieldValue).longValue();
        }
        value = value + 1;
        return TruckUtils.convertSimpleType(value,fieldType);
    }
}
