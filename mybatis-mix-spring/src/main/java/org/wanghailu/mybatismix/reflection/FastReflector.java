package org.wanghailu.mybatismix.reflection;

import org.apache.ibatis.reflection.Reflector;
import org.apache.ibatis.reflection.invoker.Invoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.beans.BeanMap;
import org.wanghailu.mybatismix.util.ReflectUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * 使用字节码技术加快mybatis中的反射读写
 * @author cdhuang
 * @date 2023/1/12
 */
public class FastReflector extends Reflector {
    
    private static Logger logger = LoggerFactory.getLogger(FastReflector.class);
    
    public FastReflector(Class<?> clazz) {
        super(clazz);
        initFastInvoke();
    }
    
    private void initFastInvoke() {
        if (getType().isPrimitive() || getType().isArray()) {
            return;
        }
        if (Map.class.isAssignableFrom(getType())) {
            return;
        }
        Map<String, Invoker> getMethods = (Map<String, Invoker>) ReflectUtils.getFieldValue(this, "getMethods");
        Map<String, Invoker> setMethods = (Map<String, Invoker>) ReflectUtils.getFieldValue(this, "setMethods");
        try {
            PropertyDescriptor[] getPropertyDescriptors = org.springframework.cglib.core.ReflectUtils
                    .getBeanGetters(getType());
            for (PropertyDescriptor getPropertyDescriptor : getPropertyDescriptors) {
                String propertyName = getPropertyDescriptor.getName();
                getMethods.put(propertyName, new FastGetInvoker(getType(), propertyName, getMethods.get(propertyName)));
            }
            PropertyDescriptor[] setPropertyDescriptors = org.springframework.cglib.core.ReflectUtils
                    .getBeanSetters(getType());
            for (PropertyDescriptor setPropertyDescriptor : setPropertyDescriptors) {
                String propertyName = setPropertyDescriptor.getName();
                setMethods.put(propertyName, new FastSetInvoker(getType(), propertyName, setMethods.get(propertyName)));
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        
    }
    
    protected static class FastGetInvoker implements Invoker {
        
        protected BeanMap beanMap;
        
        private String propertyName;
        
        private Invoker originalInvoker;
        
        public FastGetInvoker(Class<?> type, String propertyName, Invoker originalInvoker) {
            beanMap = FastBeanInvokeUtils.getBeanMap(type);
            this.propertyName = propertyName;
            this.originalInvoker = originalInvoker;
        }
        
        @Override
        public Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException {
            try {
                return beanMap.get(target, propertyName);
            } catch (Throwable e) {
                logger.warn("FastGetInvoker invoke fail ,propertyName:" + propertyName);
                return originalInvoker.invoke(target, args);
            }
        }
        
        @Override
        public Class<?> getType() {
            return originalInvoker.getType();
        }
    }
    
    protected static class FastSetInvoker implements Invoker {
        
        protected BeanMap beanMap;
        
        private String propertyName;
        
        private Invoker originalInvoker;
        
        public FastSetInvoker(Class<?> type, String propertyName, Invoker originalInvoker) {
            beanMap = FastBeanInvokeUtils.getBeanMap(type);
            this.propertyName = propertyName;
            this.originalInvoker = originalInvoker;
        }
        
        @Override
        public Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException {
            try {
                return beanMap.put(target, propertyName, args[0]);
            } catch (Throwable e) {
                logger.warn("FastSetInvoker invoke fail ,propertyName:" + propertyName);
                return originalInvoker.invoke(target, args);
            }
        }
        
        @Override
        public Class<?> getType() {
            return originalInvoker.getType();
        }
    }
    
}
