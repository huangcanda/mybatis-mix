package org.wanghailu.mybatismix.boot;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.util.StringUtils;
import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.util.ConfigurationUtils;
import org.wanghailu.mybatismix.util.ExceptionUtils;
import org.wanghailu.mybatismix.util.PrivateStringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author cdhuang
 * @date 2023/4/6
 */
@Configuration
@ConditionalOnClass({SqlSessionFactory.class, SqlSessionFactoryBean.class})
public class PropertiesBeanPostProcessor implements SmartInstantiationAwareBeanPostProcessor, EnvironmentAware, BeanFactoryAware {
    
    private static Logger logger = LoggerFactory.getLogger(PropertiesBeanPostProcessor.class);
    
    private Environment environment;

    private BeanFactory beanFactory;
    
    private MybatisMixConfiguration configuration;

    private SqlSessionFactory sqlSessionFactory;

    private SqlSessionTemplate sqlSessionTemplate;
    
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
    
    protected Class<? extends MybatisMixConfiguration> getConfigurationType() {
        String configurationType = environment.getProperty("mybatis-mix.configuration-type");
        Class<? extends MybatisMixConfiguration> configurationClass = MybatisMixConfiguration.class;
        if (PrivateStringUtils.isNotEmpty(configurationType)) {
            try {
                configurationClass = (Class<? extends MybatisMixConfiguration>) Class.forName(configurationType);
                logger.info("成功加载配置的configuration类：" + configurationType);
            } catch (ClassNotFoundException e) {
                logger.error("无法加载配置的configuration类：" + configurationType);
            }
        }
        return configurationClass;
    }
    
    protected InputStream getConfigLocation(MybatisProperties mybatisProperties) {
        InputStream inputStream = null;
        String configLocation = mybatisProperties.getConfigLocation();
        if (PrivateStringUtils.isEmpty(configLocation)) {
            configLocation = environment.getProperty("mybatis.config-location");
            try {
                inputStream = new DefaultResourceLoader().getResource(configLocation).getInputStream();
            } catch (IOException e) {
                ExceptionUtils.throwException(e);
            }
        }
        return inputStream;
    }
    
    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName)
            throws BeansException {
        if (bean instanceof MybatisProperties) {
            MybatisProperties mybatisProperties = (MybatisProperties) bean;
            if(configuration==null){
                configuration= ConfigurationUtils
                        .createConfiguration(getConfigurationType(), getConfigLocation(mybatisProperties));
            }
            mybatisProperties.setConfiguration(configuration);
        }
        return null;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof SqlSessionFactory || bean instanceof SqlSessionTemplate){
            if(bean instanceof SqlSessionTemplate){
                this.sqlSessionTemplate = (SqlSessionTemplate) bean;
            }else{
                this.sqlSessionFactory = (SqlSessionFactory) bean;
            }
            if(sqlSessionFactory!=null && sqlSessionTemplate!=null){
                MybatisMixConfiguration configuration = (MybatisMixConfiguration) sqlSessionFactory.getConfiguration();
                ConfigurationUtils.mybatisInitialized(configuration, sqlSessionFactory, sqlSessionTemplate);
            }
        }
        if (bean instanceof MybatisProperties) {
            MybatisProperties mybatisProperties = (MybatisProperties) bean;
            if (PrivateStringUtils.isNotEmpty(mybatisProperties.getConfigLocation())) {
                mybatisProperties.setConfigLocation(null);
            }
            initEntityPackages(mybatisProperties);
            ConfigurationUtils
                    .putConfigurationProperties(configuration, mybatisProperties.getConfigurationProperties());
        }
        return bean;
    }

    private void initEntityPackages(MybatisProperties mybatisProperties){
        String entityPackages = environment.getProperty("mybatis-mix.entity-packages");
        if(PrivateStringUtils.isNotEmpty(entityPackages)){
            mybatisProperties.getConfigurationProperties().setProperty("entity-packages",entityPackages);
        }else{
            entityPackages = mybatisProperties.getConfigurationProperties().getProperty("entity-packages");
        }
        if (PrivateStringUtils.isEmpty(entityPackages) && AutoConfigurationPackages.has(beanFactory)) {
            List<String> packages = AutoConfigurationPackages.get(beanFactory);
            entityPackages = StringUtils.collectionToCommaDelimitedString(packages);
            mybatisProperties.getConfigurationProperties().setProperty("entity-packages",entityPackages);
        }
    }
}
