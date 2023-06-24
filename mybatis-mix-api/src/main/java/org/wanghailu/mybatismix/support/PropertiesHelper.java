package org.wanghailu.mybatismix.support;

import org.wanghailu.mybatismix.util.TruckUtils;

/**
 * 配置读取辅助类
 * @author cdhuang
 * @date 2022/12/26
 */
public interface PropertiesHelper {
    
    String getProperty(String key);
    
    /**
     * 根据 name 获取 String 类型值（包括 应用级别、环境级别），否则返回 默认值
     *
     * @param key
     * @param defaultValue 默认值
     * @return
     */
    default String getProperty(String key, String defaultValue) {
        return TruckUtils.isNotEmpty(getProperty(key)) ? getProperty(key) : defaultValue;
    }
    
    /**
     * 根据 name 获取 Boolean 类型值（包括 应用级别、环境级别）
     *
     * @param key
     * @return
     */
     default Boolean getBoolProperty(String key) {
        String intProperty = getProperty(key);
        if (TruckUtils.isEmpty(intProperty)) {
            return null;
        } else {
            return Boolean.parseBoolean(intProperty);
        }
    }
    
    /**
     * 根据 name 获取 Boolean 类型的值（包括 应用级别、环境级别），否则返回 默认值
     *
     * @param key
     * @param defaultValue
     * @return
     */
    default boolean getBoolProperty(String key, boolean defaultValue) {
        Boolean boolProperty = getBoolProperty(key);
        return (null != boolProperty) ? boolProperty : defaultValue;
    }
    
    /**
     * 根据 name 获取 Integer 类型值（包括 应用级别、环境级别）
     *
     * @param key
     * @return
     */
    default Integer getIntProperty(String key) {
        String intProperty = getProperty(key);
        if (TruckUtils.isEmpty(intProperty)) {
            return null;
        } else {
            return Integer.parseInt(intProperty);
        }
    }
    
    /**
     * 根据 name 获取 Integer 类型的值（包括 应用级别、环境级别），否则返回 默认值
     *
     * @param key
     * @param defaultValue
     * @return
     */
    default int getIntProperty(String key, int defaultValue) {
        Integer intProperty = getIntProperty(key);
        return (null != intProperty) ? intProperty : defaultValue;
    }
    
    /**
     * 根据 name 获取 Float 类型值（包括 应用级别、环境级别）
     *
     * @param key
     * @return
     */
    default Float getFloatProperty(String key) {
        String floatProperty = getProperty(key);
        if (TruckUtils.isEmpty(floatProperty)) {
            return null;
        } else {
            return Float.parseFloat(floatProperty);
        }
    }
    
    /**
     * 根据 name 获取 Float 类型的值（包括 应用级别、环境级别），否则返回 默认值
     *
     * @param key
     * @param defaultValue
     * @return
     */
    default float getFloatProperty(String key, float defaultValue) {
        Float floatProperty = getFloatProperty(key);
        return (null != floatProperty) ? floatProperty : defaultValue;
    }
    
    /**
     * 根据 name 获取 Long 类型值（包括 应用级别、环境级别）
     *
     * @param key
     * @return
     */
    default Long getLongProperty(String key) {
        String longProperty = getProperty(key);
        if (TruckUtils.isEmpty(longProperty)) {
            return null;
        } else {
            return Long.parseLong(longProperty);
        }
    }
    
    /**
     * 根据 name 获取 Long 类型的值（包括 应用级别、环境级别），否则返回 默认值
     *
     * @param key
     * @param defaultValue
     * @return
     */
    default long getLongProperty(String key, long defaultValue) {
        Long longProperty = getLongProperty(key);
        return (null != longProperty) ? longProperty : defaultValue;
    }
}
