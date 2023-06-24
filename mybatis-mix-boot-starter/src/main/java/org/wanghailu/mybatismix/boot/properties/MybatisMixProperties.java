package org.wanghailu.mybatismix.boot.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.wanghailu.mybatismix.MybatisMixConfiguration;

/**
 *
 */
@ConfigurationProperties(prefix = MybatisMixProperties.PREFIX)
public class MybatisMixProperties {

    public static final String PREFIX = "mybatis-mix";
    
    private Class<?> configurationType = MybatisMixConfiguration.class;
    /**
     * 扫描Mapper的包路径
     */
    private String entityPackages;
    
    public Class<?> getConfigurationType() {
        return configurationType;
    }
    
    public void setConfigurationType(Class<?> configurationType) {
        this.configurationType = configurationType;
    }
    
    public String getEntityPackages() {
        return entityPackages;
    }
    
    public void setEntityPackages(String entityPackages) {
        this.entityPackages = entityPackages;
    }
}