package org.wanghailu.mybatismix.model;


import javax.persistence.Transient;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 基础类，保存实体中set过的字段，用于在更新实体时只更新set过的字段。
 */
public abstract class BaseExactUpdateRecordSimplified implements ExactUpdateEnable {
    
    public static final int $$FIELD_LENGTH_FOR_FIELD_INDEX = 0;
    
    static {
        FieldIndexUtils.putFieldNames(BaseExactUpdateRecordViewable.class, new String[0]);
    }
    
    /**
     * 要更新的字段
     */
    @Transient
    private long updateFields;
    
    /**
     * 添加需要更新字段
     *
     * @param fieldNameIndex
     */
    @Override
    public void updateFieldAdd(int fieldNameIndex) {
        updateFields |= (1L << fieldNameIndex);
    }
    
    /**
     * 获得需要更新的字段集合
     *
     * @return
     */
    @Override
    public Collection<String> updateFieldsSelect() {
        String[] fieldNames = FieldIndexUtils.getFieldNames(this.getClass());
        int fieldLength = fieldNames.length;
        Set<String> result = new HashSet<>(fieldLength);
        for (int index = 0; index < fieldLength; index++) {
            if ((updateFields & (1L << index)) != 0) {
                result.add(fieldNames[index]);
            }
        }
        return result;
    }
    
    /**
     * 撤销，重置updatedFields
     */
    @Override
    public void updateFieldsClear() {
        updateFields = 0;
    }
    
}
