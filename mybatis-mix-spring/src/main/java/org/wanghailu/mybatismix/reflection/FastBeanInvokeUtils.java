package org.wanghailu.mybatismix.reflection;

import org.springframework.cglib.beans.BeanMap;
import org.wanghailu.mybatismix.util.PrivateStringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cdhuang
 * @date 2023/4/13
 */
public class FastBeanInvokeUtils {
    
    protected static Map<Class, BeanMap> beanMapMap = new HashMap<>();
    
    protected static BeanMap getBeanMap(Class type) {
        BeanMap beanMap = beanMapMap.get(type);
        if (beanMap == null) {
            synchronized (beanMapMap) {
                beanMap = beanMapMap.get(type);
                if (beanMap == null) {
                    BeanMap.Generator gen = new BeanMap.Generator();
                    gen.setBeanClass(type);
                    beanMap = gen.create();
                    beanMapMap.put(type, beanMap);
                }
            }
        }
        return beanMap;
    }
    
    /**
     * 根据字段名 获得 字段值
     *
     * @param obj
     * @param fieldName
     * @return
     */
    public static <Entity extends Object> Object getValueByFieldName(Entity obj, String fieldName) {
        if (PrivateStringUtils.isEmpty(fieldName) || obj == null) {
            return null;
        }
        if (obj instanceof Map) {
            if (((Map) obj).containsKey(fieldName)) {
                return ((Map) obj).get(fieldName);
            } else {
                return null;
            }
            
        }
        BeanMap beanMap = getBeanMap(obj.getClass());
        return beanMap.get(obj, fieldName);
    }
    
    /**
     * 根据 字段名 设置 字段值
     *
     * @param obj
     * @param fieldName
     * @return
     */
    public static <Entity extends Object> void setValueByFieldName(Entity obj, String fieldName, Object value) {
        if (PrivateStringUtils.isEmpty(fieldName) || obj == null) {
            return;
        }
        if (obj instanceof Map) {
            ((Map) obj).put(fieldName, value);
        }
        BeanMap beanMap = getBeanMap(obj.getClass());
        beanMap.put(obj, fieldName, value);
    }
}
