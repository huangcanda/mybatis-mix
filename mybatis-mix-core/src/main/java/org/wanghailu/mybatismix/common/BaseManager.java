package org.wanghailu.mybatismix.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.support.SpiExtension;

/**
 * 管理器基类
 * @author cdhuang
 * @date 2023/1/30
 */
public abstract class BaseManager implements SpiExtension {
    
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    
    protected MybatisMixConfiguration configuration;
    
    public MybatisMixConfiguration getConfiguration() {
        return configuration;
    }
    
    public void setConfiguration(MybatisMixConfiguration configuration) {
        this.configuration = configuration;
    }
    
    public Class<? extends BaseManager> managerType(){
        return this.getClass();
    }
    
    public void initAfterSetProperties() {
    
    }
    
    public void initAfterMybatisInit(){
        logger.info("MybatisMix 管理器 "+ this.getClass().getSimpleName()+" 已初始化！");
    }
    
    public void close(){
    
    }

    @Override
    public String name() {
        return managerType().getName();
    }
}
