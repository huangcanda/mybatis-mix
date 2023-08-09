package org.wanghailu.mybatismix.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 根据字段索引寻找字段名信息
 *
 * @author cdhuang
 * @date 2023/8/3
 */
public class FieldIndexUtils {
    
    private static Map<Class<?>, String[]> fieldNamesMap = new HashMap<>();
    
    public static String getFieldName(Class<?> entityClass, int fieldIndex) {
        return fieldNamesMap.get(entityClass)[fieldIndex];
    }
    
    public static String[] getFieldNames(Class<?> entityClass) {
        return fieldNamesMap.get(entityClass);
    }
    
    public static void putFieldNames(Class<?> entityClass, String[] fieldNames) {
        fieldNamesMap.put(entityClass, fieldNames);
    }
    
}
