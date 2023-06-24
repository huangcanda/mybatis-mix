package org.wanghailu.mybatismix.fillfield.strategy;

import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.fillfield.FillFieldStrategy;
import org.wanghailu.mybatismix.keygenerator.KeyGeneratorManager;

/**
 * 填充主键
 * @author cdhuang
 * @date 2023/1/29
 */
public class FillPrimaryId implements FillFieldStrategy {
    
    public static final String STRATEGY_NAME = "id";
    
    @Override
    public String name() {
        return FillPrimaryId.STRATEGY_NAME;
    }
    
    
    @Override
    public Object fillValueBeforeInvoke(Object entity, String fieldName, MybatisMixConfiguration configuration) {
        KeyGeneratorManager keyGeneratorManager = configuration.getManager(KeyGeneratorManager.class);
        return keyGeneratorManager.generateKey(entity, fieldName);
    }
}
