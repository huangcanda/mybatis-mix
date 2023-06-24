package org.wanghailu.mybatismix.support;


import org.wanghailu.mybatismix.util.TruckUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 缓存的实体相关信息
 * Created by cdhuang on 2020/5/27.
 */
public class EntityDescriptor {

    private Class<?> entityClass;

    private String tableName;

    private String tableComment;
    
    private EntityPropertyDescriptor primaryKeyPropertyDescriptor;

    private EntityPropertyDescriptor versionPropertyDescriptor;

    private List<EntityPropertyDescriptor> entityPropertyDescriptorList;

    private Map<String, EntityPropertyDescriptor> fieldPropertyDescriptorMap;

    private Map<String, String> columnFieldCacheMap;
    
    private String logicDeleteTable;
    
    public Map<String, EntityPropertyDescriptor> getFieldPropertyDescriptorMap() {
        if(fieldPropertyDescriptorMap==null){
            Map<String, EntityPropertyDescriptor> cache = new LinkedHashMap<>();
            for (EntityPropertyDescriptor descriptor : entityPropertyDescriptorList) {
                if(TruckUtils.isNotEmpty(descriptor.getFieldName())&& TruckUtils.isNotEmpty(descriptor.getColumnName())){
                    cache.put(descriptor.getFieldName(),descriptor);
                }
            }
            fieldPropertyDescriptorMap = cache;
        }
        return fieldPropertyDescriptorMap;
    }
    
    public Map<String, String> getColumnFieldCacheMap() {
        if(columnFieldCacheMap==null){
            Map<String, String> cache = new LinkedHashMap<>();
            for (EntityPropertyDescriptor descriptor : entityPropertyDescriptorList) {
                if(TruckUtils.isNotEmpty(descriptor.getFieldName())&& TruckUtils.isNotEmpty(descriptor.getColumnName())){
                    cache.put(descriptor.getColumnName(),descriptor.getFieldName());
                }
            }
            columnFieldCacheMap = cache;
        }
        return columnFieldCacheMap;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableComment() {
        return tableComment;
    }

    public void setTableComment(String tableComment) {
        this.tableComment = tableComment;
    }
    
    public EntityPropertyDescriptor getPrimaryKeyPropertyDescriptor() {
        return primaryKeyPropertyDescriptor;
    }

    public void setPrimaryKeyPropertyDescriptor(EntityPropertyDescriptor primaryKeyPropertyDescriptor) {
        this.primaryKeyPropertyDescriptor = primaryKeyPropertyDescriptor;
    }

    public EntityPropertyDescriptor getVersionPropertyDescriptor() {
        return versionPropertyDescriptor;
    }

    public void setVersionPropertyDescriptor(EntityPropertyDescriptor versionPropertyDescriptor) {
        this.versionPropertyDescriptor = versionPropertyDescriptor;
    }

    public List<EntityPropertyDescriptor> getEntityPropertyDescriptorList() {
        return entityPropertyDescriptorList;
    }

    public void setEntityPropertyDescriptorList(List<EntityPropertyDescriptor> entityPropertyDescriptorList) {
        this.entityPropertyDescriptorList = entityPropertyDescriptorList;
    }
    
    public String getLogicDeleteTable() {
        return logicDeleteTable;
    }
    
    public void setLogicDeleteTable(String logicDeleteTable) {
        this.logicDeleteTable = logicDeleteTable;
    }
}
