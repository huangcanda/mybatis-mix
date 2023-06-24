package org.wanghailu.mybatismix.provider;

import org.wanghailu.mybatismix.constant.UpdateModeEnum;
import org.wanghailu.mybatismix.exception.MybatisMixException;
import org.wanghailu.mybatismix.model.ExactUpdateEnable;
import org.wanghailu.mybatismix.support.EntityPropertyDescriptor;
import org.wanghailu.mybatismix.util.BeanInvokeUtils;
import org.wanghailu.mybatismix.util.EntityUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cdhuang
 * @date 2023/3/20
 */
public class SqlProviderHelper {
    
    public static String insertLogicDeleteTable(Class<?> entityClass) {
        List<EntityPropertyDescriptor> entityPropertyDescriptorList = EntityUtils
                .getEntityPropertyDescriptor(entityClass);
        entityPropertyDescriptorList = entityPropertyDescriptorList.stream()
                .filter(x -> x.getFieldName() != null && x.getColumnName() != null).collect(Collectors.toList());
        String columnNames = entityPropertyDescriptorList.stream().map(EntityPropertyDescriptor::getColumnName)
                .collect(Collectors.joining(","));
        StringBuilder sb = new StringBuilder("INSERT INTO ");
        sb.append(EntityUtils.getEntityDescriptor(entityClass).getLogicDeleteTable());
        sb.append("(");
        sb.append(columnNames);
        sb.append(") SELECT ");
        sb.append(columnNames);
        sb.append(" FROM ");
        sb.append(EntityUtils.getTableName(entityClass));
        return sb.toString();
    }
    
    
    /**
     * 获得update的set语句
     *
     * @param entity     要更新的实体
     * @param updateMode 更新模式，即对字段进行过滤的方式
     * @return
     */
    public static String getUpdateSetSqlByUpdateMode(Object entity, int updateMode, Class entityClass,
            String paramNamePrefix) {
        ExactUpdateEnable exactUpdateRecord = null;
        boolean isExactUpdateRecord = false;
        if (entity instanceof ExactUpdateEnable) {
            exactUpdateRecord = (ExactUpdateEnable) entity;
        }
        if (UpdateModeEnum.DEFAULT.getValue() == updateMode && exactUpdateRecord != null) {
            isExactUpdateRecord = true;
        }
        if (UpdateModeEnum.EXACT.getValue() == updateMode) {
            if (exactUpdateRecord == null) {
                throw new IllegalArgumentException("exact update model object must extend BaseExactUpdateRecord!");
            }
            isExactUpdateRecord = true;
        }
        // 版本字段
        EntityPropertyDescriptor versionPropertyDescriptor = EntityUtils.getVersionPropertyDescriptor(entityClass);
        List<EntityPropertyDescriptor> entityPropertyDescriptors = EntityUtils
                .getEntityPropertyDescriptorOnUpdateAndSet(entityClass);
        // 组装update set 的设值部分
        StringBuilder setSql = new StringBuilder();
        String comma = "";
        for (EntityPropertyDescriptor currentModelPropertyDescriptor : entityPropertyDescriptors) {
            Object currentModelPropertyValue = null;
            boolean isUpdateField = false;
            if (isExactUpdateRecord) {
                if (exactUpdateRecord.gainUpdateFields().contains(currentModelPropertyDescriptor.getFieldName())) {
                    isUpdateField = true;
                }
            } else if (UpdateModeEnum.ALL.getValue() == updateMode) {
                isUpdateField = true;
            } else if (UpdateModeEnum.NOT_NULL.getValue() == updateMode) {
                currentModelPropertyValue = BeanInvokeUtils
                        .getValueByFieldName(entity, currentModelPropertyDescriptor.getFieldName());
                if (currentModelPropertyValue != null) {
                    isUpdateField = true;
                }
            }
            if (versionPropertyDescriptor != null && versionPropertyDescriptor.getFieldName().equals(currentModelPropertyDescriptor.getFieldName())) {
                if(isExactUpdateRecord && isUpdateField){
                     //精准更新版本号才会使用版本号字段的值进行更新，否则直接+1
                }else{
                    setSql.append(comma);
                    setSql.append(versionPropertyDescriptor.getColumnName());
                    setSql.append("=(");
                    setSql.append(versionPropertyDescriptor.getColumnName());
                    setSql.append("+1)");
                    isUpdateField = false;
                    comma = ",";
                }
            }
            if (isUpdateField) {
                //第二个开始的set字段后，都添加逗号
                setSql.append(comma);
                setSql.append(currentModelPropertyDescriptor.getColumnName());
                setSql.append("=#{");
                setSql.append(paramNamePrefix);
                setSql.append(currentModelPropertyDescriptor.getFieldName());
                setSql.append('}');
                comma = ",";
            }
        }
        if (setSql.length() == 0) {
            throw new MybatisMixException("update语句中找不到set的字段！");
        }
        return setSql.toString();
    }
}
