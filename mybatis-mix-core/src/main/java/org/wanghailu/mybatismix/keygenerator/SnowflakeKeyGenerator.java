package org.wanghailu.mybatismix.keygenerator;

import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.constant.ConfigurationKeyConstant;
import org.wanghailu.mybatismix.util.TruckUtils;

/**
 * 基于雪花算法进行主键生成
 * @author cdhuang
 * @date 2023/4/18
 */
public class SnowflakeKeyGenerator extends BaseKeyGenerator<Number> {
    
    protected SnowflakeIDGenImpl snowflakeIDGen;
    
    @Override
    public void setConfiguration(MybatisMixConfiguration configuration) {
        super.setConfiguration(configuration);
        initSnowflakeIDGenImpl();
    }
    
    protected void initSnowflakeIDGenImpl() {
        Long workerId = configuration.getLongProperty(ConfigurationKeyConstant.workerId);
        if (workerId == null) {
            snowflakeIDGen = new SnowflakeIDGenImpl();
        } else {
            snowflakeIDGen = new SnowflakeIDGenImpl(workerId);
        }
        SnowflakeIDGenImpl.INSTANCE = snowflakeIDGen;
    }
    
    @Override
    public Number generateKey(Class entityType, String fieldName, Class keyType) {
        long val= snowflakeIDGen.generateKey();
        return (Number) TruckUtils.convertSimpleType(val,keyType);
    }
}
