package org.wanghailu.mybatismix.support;


import org.wanghailu.mybatismix.util.TruckUtils;

import javax.persistence.Column;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 实体属性描述类（实体属性包括了：属性字段、set方法、get方法）
 */
public class EntityPropertyDescriptor {
    
    private String columnName;
    
    private String fieldName;
    
    private String columnComment;
    
    private boolean isPrimaryKey = false;
    
    private boolean isVersion = false;
    
    private boolean isTransient = false;
    
    private Method setMethod = null;
    
    private Method getMethod = null;
    
    private Field field = null;
    
    private Column column = null;
    
    public EntityPropertyDescriptor() {
    }
    
    public EntityPropertyDescriptor(String fieldName, Method setMethod, Method getMethod, Field field) {
        this.fieldName = fieldName;
        this.setMethod = setMethod;
        this.getMethod = getMethod;
        this.field = field;
    }
    
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        if (field != null && field.isAnnotationPresent(annotationClass)) {
            return true;
        }
        if (getMethod != null && getMethod.isAnnotationPresent(annotationClass)) {
            return true;
        }
        return false;
    }
    
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        T annotation = null;
        if (field != null) {
            annotation = field.getAnnotation(annotationClass);
        }
        if (annotation == null && getMethod != null) {
            annotation = getMethod.getAnnotation(annotationClass);
        }
        return annotation;
    }
    
    public Field getField() {
        return field;
    }
    
    public void setField(Field field) {
        this.field = field;
    }
    
    /**
     * 字段列名
     *
     * @return
     */
    public String getColumnName() {
        if (columnName == null) {
            if (column != null) {
                columnName = column.name();
            } else if (!isTransient) {
                columnName = TruckUtils.camelToUnderline(fieldName).toUpperCase();
            }
        }
        return columnName;
    }
    
    public Column getColumn() {
        return column;
    }
    
    public void setColumn(Column column) {
        this.column = column;
    }
    
    /**
     * java字段名
     *
     * @return
     */
    public String getFieldName() {
        return fieldName;
    }
    
    /**
     * java字段名
     *
     * @param fieldName
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    
    public String getColumnComment() {
        return columnComment;
    }
    
    public void setColumnComment(String columnComment) {
        this.columnComment = columnComment;
    }
    
    /**
     * 是否为主键字段
     *
     * @return
     */
    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }
    
    /**
     * 是否为主键字段
     *
     * @param isPrimaryKey
     */
    public void setPrimaryKey(boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
    }
    
    /**
     * 是否为版本字段
     *
     * @return
     */
    public boolean isVersion() {
        return isVersion;
    }
    
    /**
     * 是否为版本字段
     *
     * @param isVersion
     */
    public void setVersion(boolean isVersion) {
        this.isVersion = isVersion;
    }
    
    /**
     * 是否为非持久化（瞬时）字段
     *
     * @return
     */
    public boolean isTransient() {
        return isTransient;
    }
    
    /**
     * 是否为非持久化（瞬时）字段
     *
     * @param isTransient
     */
    public void setTransient(boolean isTransient) {
        this.isTransient = isTransient;
    }
    
    /**
     * 字段的set方法
     *
     * @return
     */
    public Method getSetMethod() {
        return setMethod;
    }
    
    /**
     * 字段set方法
     *
     * @param setMethod
     */
    public void setSetMethod(Method setMethod) {
        this.setMethod = setMethod;
    }
    
    /**
     * 字段get方法
     *
     * @return
     */
    public Method getGetMethod() {
        return getMethod;
    }
    
    /**
     * 字段get方法
     *
     * @param getMethod
     */
    public void setGetMethod(Method getMethod) {
        this.getMethod = getMethod;
    }
    
    
}
