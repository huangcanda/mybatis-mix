package org.wanghailu.mybatismix.example;

import org.wanghailu.mybatismix.constant.SqlSymbolConstant;
import org.wanghailu.mybatismix.example.criteria.BaseCriteria;
import org.wanghailu.mybatismix.example.criteria.Criterion;
import org.wanghailu.mybatismix.exception.MybatisMixException;
import org.wanghailu.mybatismix.model.AdditionalParameters;
import org.wanghailu.mybatismix.support.EntityPropertyDescriptor;
import org.wanghailu.mybatismix.util.EntityUtils;
import org.wanghailu.mybatismix.util.PrivateStringUtils;
import org.wanghailu.mybatismix.util.TruckUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author cdhuang
 * @date 2023/3/20
 */
public class ExampleHelper {
    
    public static <S> S getSetEntityByUpdateExample(BaseUpdateExample<S, ?> example) {
        return example.setEntity;
    }
    
    public static int getUpdateModelByUpdateExample(BaseUpdateExample example) {
        return example.updateModel;
    }
    
    public static String getSelectStr(BaseQueryExample example) {
        List<String> selectList = example.getSelectList();
        if (selectList == null || selectList.size() == 0) {
            return "*";
        }
        List<String> selectResultList = new ArrayList<>();
        Map<String, EntityPropertyDescriptor> fieldPropertyDescriptorMap = EntityUtils
                .getEntityDescriptor(example.getEntityClass()).getFieldPropertyDescriptorMap();
        String selectColumnStr = PrivateStringUtils.join(selectList, ",");
        for (String column : selectColumnStr.split(",")) {
            EntityPropertyDescriptor propertyDescriptor = fieldPropertyDescriptorMap.get(column);
            if (propertyDescriptor == null) {
                selectResultList.add(column);
            } else {
                selectResultList.add(propertyDescriptor.getColumnName());
            }
        }
        return convertFieldNameToColumnName(selectColumnStr, example.getEntityClass(), false);
    }
    
    /**
     * 字段转列名，严谨的方式当然是需要sql解析，这里先只是简单处理
     *
     * @param selectColumnStr
     * @param entityClass
     * @param isOrderStr
     * @return
     */
    protected static String convertFieldNameToColumnName(String selectColumnStr, Class entityClass,
            boolean isOrderStr) {
        List<String> resultList = new ArrayList<>();
        Map<String, EntityPropertyDescriptor> fieldPropertyDescriptorMap = EntityUtils.getEntityDescriptor(entityClass)
                .getFieldPropertyDescriptorMap();
        for (String column : selectColumnStr.split(",")) {
            if (PrivateStringUtils.isEmpty(column)) {
                throw new MybatisMixException("错误的sql语句解析：" + selectColumnStr);
            }
            column = column.trim();
            String fieldName = column;
            boolean isDesc = false;
            if (isOrderStr) {
                if (fieldName.toUpperCase().endsWith(" ASC")) {
                    fieldName = fieldName.substring(0, fieldName.length() - 4).trim();
                } else if (fieldName.toUpperCase().endsWith(" DESC")) {
                    fieldName = fieldName.substring(0, fieldName.length() - 5).trim();
                    isDesc = true;
                }
            }
            String columnName = fieldName;
            EntityPropertyDescriptor propertyDescriptor = fieldPropertyDescriptorMap.get(fieldName);
            if (propertyDescriptor != null) {
                columnName = propertyDescriptor.getColumnName();
            }
            if (isOrderStr) {
                if (isDesc) {
                    columnName = columnName + " DESC";
                } else {
                    columnName = columnName + " ASC";
                }
            }
            resultList.add(columnName.toUpperCase());
        }
        return PrivateStringUtils.join(resultList, ",");
    }
    
    public static String getGroupByStr(BaseQueryExample example) {
        List<String> groupByList = example.getGroupByList();
        if (groupByList == null || groupByList.size() == 0) {
            return "";
        }
        String stringJoin = PrivateStringUtils.join(groupByList, ",");
        stringJoin = convertFieldNameToColumnName(stringJoin, example.getEntityClass(), false);
        return " GROUP BY " + stringJoin;
    }
    
    public static String getOrderByStr(BaseQueryExample example) {
        List<String> orderByList = example.getOrderByList();
        if (orderByList == null || orderByList.size() == 0) {
            return "";
        }
        String stringJoin = PrivateStringUtils.join(orderByList, ",");
        stringJoin = convertFieldNameToColumnName(stringJoin, example.getEntityClass(), true);
        return " ORDER BY " + stringJoin;
    }
    
    public static String getForUpdateStr(BaseQueryExample example) {
        if (example.isForUpdate()) {
            return "for update";
        } else {
            return "";
        }
    }
    
    /**
     * 构造where条件
     *
     * @param baseExample
     * @return
     */
    public static String getWhereCondition(BaseExample baseExample) {
        if (baseExample.getWhereCondition() == null) {
            return "";
        } else {
            return " WHERE " + getWhereCondition(baseExample.getAdditionalParameters(),
                    baseExample.getWhereCondition());
        }
    }
    
    /**
     * 构造where条件
     *
     * @param criteria
     * @return
     */
    public static String getWhereCondition(AdditionalParameters additionalParameters, BaseCriteria criteria) {
        String separator = criteria.isOrLogic() ? SqlSymbolConstant.OR : SqlSymbolConstant.AND;
        List<Criterion> criterionList = criteria.getAllCriteria();
        if (!criteria.isValid()) {
            return " 1=1 ";
        }
        List<String> criterionStrList = new ArrayList<>();
        for (Criterion criterion : criterionList) {
            if (criterion.getNestCriteria() != null) {
                String nestStr = getWhereCondition(additionalParameters, criterion.getNestCriteria());
                if (TruckUtils.isNotEmpty(nestStr)) {
                    criterionStrList.add(" (" + nestStr + ")");
                }
            } else if (criterion.isNoValue()) {
                criterionStrList.add(criterion.getColumnName() + " " + criterion.getCondition());
            } else if (criterion.isSingleValue()) {
                criterionStrList.add(criterion.getColumnName() + " " + criterion.getCondition() + additionalParameters
                        .setParam(criterion.getValue()));
            } else if (criterion.isBetweenValue()) {
                criterionStrList.add(criterion.getColumnName() + " " + criterion.getCondition() + additionalParameters
                        .setParam(criterion.getValue()) + " and " + additionalParameters
                        .setParam(criterion.getSecondValue()));
            } else if (criterion.isListValue()) {
                List<?> list = (List<?>) criterion.getValue();
                String values = list.stream().map(additionalParameters::setParam).collect(Collectors.joining(","));
                criterionStrList.add(criterion.getColumnName() + " " + criterion.getCondition() + "(" + values + ")");
            }
        }
        return PrivateStringUtils.join(criterionStrList, separator);
    }
    
}
