package org.wanghailu.mybatismix.fillfield;

import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.support.SpiExtension;

/**
 * @author cdhuang
 * @date 2023/1/29
 */
public interface FillFieldStrategy extends SpiExtension {
    
    /**
     * 在执行前填充值
     * @param entity
     * @param fieldName
     * @param configuration
     * @return
     */
    Object fillValueBeforeInvoke(Object entity, String fieldName, MybatisMixConfiguration configuration);
    
    /**
     *
     * @return
     */
    default boolean onlyFillWhenOriginalValueIsNull(){
        return true;
    }
}
