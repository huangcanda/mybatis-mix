package org.wanghailu.mybatismix.util;


import org.apache.ibatis.reflection.Reflector;
import org.apache.ibatis.reflection.invoker.Invoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Map;


/**
 * 调用实体的get和set方法 Created by cd_huang on 2019/6/10.
 */
public class BeanInvokeUtils {
    
    private static Logger logger = LoggerFactory.getLogger(BeanInvokeUtils.class);
    
    protected static final Object[] NO_ARGUMENTS = new Object[0];
    
    /**
     * 获取 主键值
     *
     * @param entity
     * @return
     */
    public static Serializable getPrimaryKeyValue(Object entity) {
        return (Serializable) getValueByFieldName(entity, EntityUtils.getPrimaryKeyFieldName(entity.getClass()));
    }
    
    public static boolean hasSetter(Class<?> type, String propertyName) {
        Reflector reflector = MybatisContext.configuration.getReflectorFactory().findForClass(type);
        return reflector.hasSetter(propertyName);
    }
    
    /**
     * 、
     *
     * @param type
     * @param propertyName
     * @return
     */
    public static boolean hasGetter(Class<?> type, String propertyName) {
        Reflector reflector = MybatisContext.configuration.getReflectorFactory().findForClass(type);
        return reflector.hasGetter(propertyName);
    }
    
    /**
     * 根据字段名 获得 字段值
     *
     * @param obj
     * @param fieldName
     * @return
     */
    public static <Entity extends Object> Object getValueByFieldName(Entity obj, String fieldName) {
        if (PrivateStringUtils.isEmpty(fieldName)) {
            return null;
        }
        if (obj instanceof Map) {
            if(((Map) obj).containsKey(fieldName)){
                return ((Map) obj).get(fieldName);
            }else{
                return null;
            }
        }
        Reflector reflector = MybatisContext.configuration.getReflectorFactory().findForClass(obj.getClass());
        Invoker invoker = reflector.getGetInvoker(fieldName);
        try {
            return invoker.invoke(obj, NO_ARGUMENTS);
        } catch (Exception e) {
            ExceptionUtils.throwException(e);
            return null;
        }
    }
    
    /**
     * 根据 字段名 设置 字段值
     *
     * @param obj
     * @param fieldName
     * @return
     */
    public static <Entity extends Object> void setValueByFieldName(Entity obj, String fieldName, Object value) {
        if (PrivateStringUtils.isEmpty(fieldName)) {
            return;
        }
        if (obj instanceof Map) {
            ((Map) obj).put(fieldName, value);
        }
        Reflector reflector = MybatisContext.configuration.getReflectorFactory().findForClass(obj.getClass());
        Invoker invoker = reflector.getSetInvoker(fieldName);
        try {
            Object[] params = {value};
            invoker.invoke(obj, params);
        } catch (Exception e) {
            ExceptionUtils.throwException(e);
        }
    }
    
    public static Serializable getValueByFieldNameIfExist(Object entity, String fieldName) {
        if (PrivateStringUtils.isNotEmpty(fieldName)) {
            if (BeanInvokeUtils.hasGetter(entity.getClass(), fieldName)) {
                return (Serializable) BeanInvokeUtils.getValueByFieldName(entity, fieldName);
            } else {
                logger.warn("getFieldValueByFieldName can't find field,entityClass:{},fieldName:{}",
                        entity.getClass().getSimpleName(), fieldName);
                return null;
            }
        } else {
            return null;
        }
    }
    
    /**
     * 根据 字段名 设置 字段值
     *
     * @param entity
     * @param fieldName
     * @return
     */
    public static void setValueByFieldNameIfExist(Object entity, String fieldName, Object value) {
        if (PrivateStringUtils.isNotEmpty(fieldName)) {
            if (BeanInvokeUtils.hasSetter(entity.getClass(), fieldName)) {
                BeanInvokeUtils.setValueByFieldName(entity, fieldName, value);
            } else {
                logger.warn("setFieldValueByFieldName can't find field,entityClass:{},fieldName:{}",
                        entity.getClass().getSimpleName(), fieldName);
            }
        }
    }
}
