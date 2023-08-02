package org.wanghailu.mybatismix.example.criteria;

import java.io.Serializable;
import java.util.List;

/**
 * 条件表达式
 */
public class Criterion implements Serializable {

    private String columnName;

    private String condition;

    private Object value;

    private Object secondValue;

    private boolean noValue;

    private boolean singleValue;

    private boolean betweenValue;

    private boolean listValue;

    private BaseCriteria nestCriteria;

    public BaseCriteria getNestCriteria() {
        return nestCriteria;
    }
    
    public String getColumnName() {
        return columnName;
    }
    
    public String getCondition() {
        return condition;
    }

    public Object getValue() {
        return value;
    }

    public Object getSecondValue() {
        return secondValue;
    }

    public boolean isNoValue() {
        return noValue;
    }

    public boolean isSingleValue() {
        return singleValue;
    }

    public boolean isBetweenValue() {
        return betweenValue;
    }

    public boolean isListValue() {
        return listValue;
    }


    public Criterion(BaseCriteria nestCriteria) {
        this.nestCriteria = nestCriteria;
    }

    public Criterion(String columnName,String condition) {
        super();
        this.columnName = columnName;
        this.condition = condition;
        this.noValue = true;
    }


    public Criterion(String columnName,String condition, Object value) {
        super();
        this.columnName = columnName;
        this.condition = condition;
        this.value = value;
        if (value instanceof List<?>) {
            this.listValue = true;
        } else {
            this.singleValue = true;
        }
    }

    public Criterion(String columnName,String condition, Object value, Object secondValue) {
        super();
        this.columnName = columnName;
        this.condition = condition;
        this.value = value;
        this.secondValue = secondValue;
        this.betweenValue = true;
    }
}
