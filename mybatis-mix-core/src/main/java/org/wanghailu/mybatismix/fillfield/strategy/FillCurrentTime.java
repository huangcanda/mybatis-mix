package org.wanghailu.mybatismix.fillfield.strategy;

import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.fillfield.FillFieldStrategy;

import java.util.Date;

/**
 * 填充当前时间
 * @author cdhuang
 * @date 2023/1/29
 */
public class FillCurrentTime implements FillFieldStrategy {
    
    public static final String STRATEGY_NAME = "now";
    
    @Override
    public String name() {
        return STRATEGY_NAME;
    }
    
    @Override
    public Object fillValueBeforeInvoke(Object entity, String fieldName, MybatisMixConfiguration configuration) {
        return new Date();
    }

    @Override
    public boolean onlyFillWhenOriginalValueIsNull() {
        return false;
    }
}
