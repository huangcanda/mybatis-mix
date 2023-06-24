package org.wanghailu.mybatismix.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wanghailu.mybatismix.annotation.Comment;
import org.wanghailu.mybatismix.annotation.LogicDelete;
import org.wanghailu.mybatismix.support.EntityDescriptor;
import org.wanghailu.mybatismix.support.EntityPropertyDescriptor;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Entity 工具类
 */
public class EntityUtils {
    
    private static Logger logger = LoggerFactory.getLogger(EntityUtils.class);
    
    /**
     * 实体所有属性描述（字段、set方法、get方法、其它方法）
     */
    private static Map<Class<?>, EntityDescriptor> entityDescriptorMap = new HashMap<>();
    
    /**
     * 获取 实体类 表名
     *
     * @param entityClass
     * @return
     */
    public static String getTableName(Class<?> entityClass) {
        return getEntityDescriptor(entityClass).getTableName();
    }
    
    /**
     * 获取 主键 字段名
     *
     * @param entityClass
     * @return
     */
    public static EntityPropertyDescriptor getPrimaryKeyPropertyDescriptor(Class<?> entityClass) {
        return getEntityDescriptor(entityClass).getPrimaryKeyPropertyDescriptor();
    }
    
    /**
     * 获取 主键 字段名
     *
     * @param entityClass
     * @return
     */
    public static String getPrimaryKeyFieldName(Class<?> entityClass) {
        EntityPropertyDescriptor entityPropertyDescriptor = getPrimaryKeyPropertyDescriptor(entityClass);
        return entityPropertyDescriptor == null ? null : entityPropertyDescriptor.getFieldName();
    }
    
    /**
     * 获取 主键 字段列名
     *
     * @param entityClass
     * @return
     */
    public static String getPrimaryKeyColumnName(Class<?> entityClass) {
        EntityPropertyDescriptor entityPropertyDescriptor = getPrimaryKeyPropertyDescriptor(entityClass);
        return entityPropertyDescriptor == null ? null : entityPropertyDescriptor.getColumnName();
    }
    
    /**
     * 获取 版本 字段名
     *
     * @param entityClass
     * @return
     */
    public static EntityPropertyDescriptor getVersionPropertyDescriptor(Class<?> entityClass) {
        return getEntityDescriptor(entityClass).getVersionPropertyDescriptor();
    }
    
    /**
     * 获取 版本 字段名
     *
     * @param entityClass
     * @return
     */
    public static String getVersionFieldName(Class<?> entityClass) {
        EntityPropertyDescriptor entityPropertyDescriptor = getVersionPropertyDescriptor(entityClass);
        return entityPropertyDescriptor == null ? null : entityPropertyDescriptor.getFieldName();
    }
    
    /**
     * 获取 实体 所有 字段属性描述
     *
     * @param clazz
     * @return
     */
    public static List<EntityPropertyDescriptor> getEntityPropertyDescriptor(Class<?> clazz) {
        return EntityUtils.getEntityDescriptor(clazz).getEntityPropertyDescriptorList();
    }
    
    public static List<EntityPropertyDescriptor> getEntityPropertyDescriptorOnUpdateAndSet(Class<?> clazz) {
        List<EntityPropertyDescriptor> list = EntityUtils.getEntityDescriptor(clazz).getEntityPropertyDescriptorList();
        // 主键字段
        EntityPropertyDescriptor primaryKeyPropertyDescriptor = EntityUtils.getPrimaryKeyPropertyDescriptor(clazz);
        list = list.stream().filter(x -> x.getFieldName() != null && !x.getFieldName()
                .equals(primaryKeyPropertyDescriptor.getFieldName())).collect(Collectors.toList());
        return list;
    }
    
    
    /**
     * 根据 字段名 返回 字段列名
     *
     * @param clazz
     * @param fieldName
     * @return
     */
    public static String getColumnNameByFieldName(Class<?> clazz, String fieldName) {
        EntityPropertyDescriptor entityPropertyDescriptor = EntityUtils.getEntityDescriptor(clazz)
                .getFieldPropertyDescriptorMap().get(fieldName);
        if (entityPropertyDescriptor == null) {
            return fieldName;
        } else {
            return entityPropertyDescriptor.getColumnName();
        }
    }
    
    /**
     * 根据给定的类，获取所有字段名
     *
     * @param clazz
     * @return
     */
    public static String getSelectAllColumnName(Class<?> clazz) {
        return EntityUtils.getEntityPropertyDescriptor(clazz).stream().map(EntityPropertyDescriptor::getColumnName)
                .collect(Collectors.joining(","));
    }
    
    /**
     * 根据 字段名 返回 实体字段属性描述
     *
     * @param clazz
     * @param fieldName
     * @return
     */
    public static EntityPropertyDescriptor getPropertyDescriptorByFieldName(Class<?> clazz, String fieldName) {
        return getEntityDescriptor(clazz).getFieldPropertyDescriptorMap().get(fieldName);
    }
    
    public static boolean isLogicDelete(Class<?> entityClass) {
        return PrivateStringUtils.isNotEmpty(getEntityDescriptor(entityClass).getLogicDeleteTable());
    }
    
    /**
     * 获取实体所有描述
     *
     * @param clazz
     * @return
     */
    public static EntityDescriptor getEntityDescriptor(Class<?> clazz) {
        EntityDescriptor entityDescriptor = entityDescriptorMap.get(clazz);
        if (entityDescriptor == null) {
            synchronized (EntityUtils.class) {
                entityDescriptor = entityDescriptorMap.get(clazz);
                if (entityDescriptor == null) {
                    entityDescriptor = initEntityPropertyDescriptors(clazz);
                    //装入静态map
                    entityDescriptorMap.put(clazz, entityDescriptor);
                }
            }
        }
        return entityDescriptor;
    }
    
    /**
     * 初始化实体所有属性描述
     *
     * @param clazz
     * @return
     */
    private static EntityDescriptor initEntityPropertyDescriptors(Class<?> clazz) {
        EntityDescriptor entityDescriptor = new EntityDescriptor();
        entityDescriptor.setEntityClass(clazz);
        if (clazz != null && clazz.isAnnotationPresent(Table.class)) {
            entityDescriptor.setTableName(clazz.getAnnotation(Table.class).name());
        }
        if (clazz != null && clazz.isAnnotationPresent(Comment.class)) {
            entityDescriptor.setTableComment(clazz.getAnnotation(Comment.class).value());
        }
        //是否为逻辑删除标记字段
        if (clazz.isAnnotationPresent(LogicDelete.class)) {
            LogicDelete logicDelete = clazz.getAnnotation(LogicDelete.class);
            String logicDeleteTable =
                    TruckUtils.isEmpty(logicDelete.value()) ? entityDescriptor.getTableName() + "_deleted"
                            : logicDelete.value();
            entityDescriptor.setLogicDeleteTable(logicDeleteTable);
        }
        List<EntityPropertyDescriptor> propertyDescriptors = new ArrayList<>();
        entityDescriptor.setEntityPropertyDescriptorList(propertyDescriptors);
        //处理实体（有set，get方法，field字段）的字段属性
        for (PropertyDescriptor propertyDescriptor : ReflectUtils.getBeanPropertyDescriptorsHaveGetSetMethod(clazz)) {
            Method readMethod = propertyDescriptor.getReadMethod();
            Method writeMethod = propertyDescriptor.getWriteMethod();
            Field field = ReflectUtils.getField(clazz, propertyDescriptor.getName());
            //判断是否为可读属性
            if (readMethod == null || writeMethod == null || field == null) {
                continue;
            }
            //字段属性描述
            EntityPropertyDescriptor entityPropertyDescriptor = new EntityPropertyDescriptor(
                    propertyDescriptor.getName(), writeMethod, readMethod, field);
            if (entityPropertyDescriptor.isAnnotationPresent(Transient.class)) {
                //非持久化字段
                continue;
            } else {
                //判断是否有@Column注解的get方法
                if (entityPropertyDescriptor.isAnnotationPresent(Column.class)) {
                    Column column = entityPropertyDescriptor.getAnnotation(Column.class);
                    entityPropertyDescriptor.setColumn(column);
                }
                //判断是否有注释注解
                if (entityPropertyDescriptor.isAnnotationPresent(Comment.class)) {
                    entityPropertyDescriptor
                            .setColumnComment(entityPropertyDescriptor.getAnnotation(Comment.class).value());
                }
                //是否为主键字段
                if (entityPropertyDescriptor.isAnnotationPresent(Id.class)) {
                    entityPropertyDescriptor.setPrimaryKey(true);
                    entityDescriptor.setPrimaryKeyPropertyDescriptor(entityPropertyDescriptor);
                }
                //是否为版本字段
                if (entityPropertyDescriptor.isAnnotationPresent(Version.class)) {
                    entityPropertyDescriptor.setVersion(true);
                    entityDescriptor.setVersionPropertyDescriptor(entityPropertyDescriptor);
                }
            }
            propertyDescriptors.add(entityPropertyDescriptor);
            
        }
        return entityDescriptor;
    }
    
    
}
