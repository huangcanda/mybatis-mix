package org.wanghailu.mybatismix.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * spring上下文工具类
 */
@Component
public class SpringUtils implements BeanFactoryPostProcessor, ApplicationContextAware {
    
    private static ConfigurableListableBeanFactory beanFactory;
    
    private static ApplicationContext applicationContext;
    
    /**
     * 设置spring 上下文
     *
     * @param ctx
     */
    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        SpringUtils.applicationContext = ctx;
    }
    
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        SpringUtils.beanFactory = beanFactory;
    }
    
    public static ListableBeanFactory getBeanFactory() {
        if (beanFactory != null) {
            return beanFactory;
        }
        if (applicationContext != null) {
            return applicationContext;
        }
        return null;
    }
    
    /**
     * 获取spring 加载的上下文
     *
     * @return
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
    
    /**
     * 根据name获取实例
     *
     * @param name
     * @return
     */
    public static Object getBean(String name) {
        return getBeanFactory().getBean(name);
    }
    
    /**
     * 根据name和Class 获取spring 上下文实例
     *
     * @param name
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name, Class<T> type) {
        return (T) getBeanFactory().getBean(name);
    }
    
    /**
     * 根据实例name 获取Class
     *
     * @param name
     * @return
     */
    public static Class<?> getType(String name) {
        return getBeanFactory().getType(name);
    }
    
    /**
     * 根据Class获取实例Map
     *
     * @param type
     * @return
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> type) {
        return getBeanFactory().getBeansOfType(type);
    }
    
    /**
     * 根据Class获取实例 【推荐使用 getBean 】
     *
     * @param type
     * @return
     */
    @Deprecated
    public static <T> T getBeanOfType(Class<T> type) {
        return getBean(type);
    }
    
    /**
     * 根据Class获取实例
     *
     * @param type
     * @return
     */
    public static <T> T getBean(Class<T> type) {
        return getBeanFactory().getBean(type);
    }
    
    public static boolean isSpringApplicationContextInit() {
        return getBeanFactory() != null;
    }
}





