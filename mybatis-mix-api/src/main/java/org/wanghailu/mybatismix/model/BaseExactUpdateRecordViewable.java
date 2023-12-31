package org.wanghailu.mybatismix.model;


import org.wanghailu.mybatismix.support.ConcurrentHashSet;

import javax.persistence.Transient;
import java.util.Collection;
import java.util.HashSet;

/**
 * 基础类，保存实体中set过的字段，用于在更新实体时只更新set过的字段。
 */
public abstract class BaseExactUpdateRecordViewable implements ExactUpdateEnable {
    
    public static final int $$FIELD_LENGTH_FOR_FIELD_INDEX = 0;
    
    static {
        FieldIndexUtils.putFieldNames(BaseExactUpdateRecordViewable.class, new String[0]);
    }
    
    /**
     * 要更新的字段
     */
    @Transient
    private Collection<String> updateFields;
    
    public BaseExactUpdateRecordViewable() {
        this(false);
    }
    
    /**
     * 正常对象set字段时不会存在并发，如有特殊情况，则concurrentMode设置为true,使updatedFields集合 具备并发资格。
     *
     * @param concurrentMode
     */
    public BaseExactUpdateRecordViewable(boolean concurrentMode) {
        if (concurrentMode) {
            updateFields = new ConcurrentHashSet<>();
        } else {
            updateFields = new HashSet<>();
        }
    }
    
    
    /**
     * 添加设值字段
     *
     * @param fieldNameIndex
     */
    @Override
    public void updateFieldAdd(int fieldNameIndex) {
        updateFields.add(FieldIndexUtils.getFieldName(this.getClass(),fieldNameIndex));
    }
    
    /**
     * 获得updatedFields，方法名故意不设置成getUpdatedFields
     *
     * @return
     */
    @Override
    public Collection<String> updateFieldsSelect() {
        return updateFields;
    }
    
    /**
     * 撤销，重置updatedFields
     *
     */
    @Override
    public void updateFieldsClear() {
        updateFields.clear();
    }
    
}
