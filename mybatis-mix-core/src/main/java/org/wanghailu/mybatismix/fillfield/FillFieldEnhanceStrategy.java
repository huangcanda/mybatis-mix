package org.wanghailu.mybatismix.fillfield;

import org.wanghailu.mybatismix.MybatisMixConfiguration;

/**
 * 填充值增强策略，主要用在 执行后填充值，可填充参数及结果
 */
public interface FillFieldEnhanceStrategy extends FillFieldStrategy{

    @Override
    default Object fillValueBeforeInvoke(Object entity, String fieldName, MybatisMixConfiguration configuration){
        return null;
    }

    /**
     * 仅
     * @return
     */
    default boolean onlyDeclareAfterInvoke(){
        return true;
    }

    @Override
    default boolean onlyFillWhenOriginalValueIsNull() {
        return false;
    }

    /**
     * 在执行后填充值
     * @param entity
     * @param fieldName
     * @param configuration
     * @return
     */
    Object fillValueAfterInvoke(Object entity, String fieldName, MybatisMixConfiguration configuration,Object result);
}
