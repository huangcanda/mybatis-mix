package org.wanghailu.mybatismix.boot;

import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.wanghailu.mybatismix.boot.properties.MybatisMixProperties;

/**
 * springcloud 配置中心支持
 * @author cdhuang
 * @date 2023/4/14
 */
@Configuration
public class RefreshPropertiesBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {
    
    private static Logger logger = LoggerFactory.getLogger(RefreshPropertiesBeanDefinitionRegistryPostProcessor.class);
    
    private static final String REFRESH_SCORE_NAME = "refresh";
    
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        Scope scope = beanFactory.getRegisteredScope(REFRESH_SCORE_NAME);
        if(scope==null){
            return;
        }
        String[] beanDefinitionNames= beanFactory.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);
            if(MybatisProperties.class.getName().equals(beanDefinition.getBeanClassName())){
                beanDefinition.setScope(REFRESH_SCORE_NAME);
                logger.info("添加MybatisProperties对配置刷新的支持！");
            }
            if(MybatisMixProperties.class.getName().equals(beanDefinition.getBeanClassName())){
                beanDefinition.setScope(REFRESH_SCORE_NAME);
                logger.info("添加MybatisMixProperties对配置刷新的支持！");
            }
        }
    }
    
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    
    }
}
