package org.wanghailu.mybatismix.model;


import javax.persistence.Transient;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 基础类，保存实体中set过的字段，用于在更新实体时只更新set过的字段。
 */
public abstract class BaseExactUpdateRecord implements ExactUpdateEnable {
    
    public static final int $$FIELD_LENGTH_FOR_FIELD_INDEX = 0;
    
    static {
        FieldIndexUtils.putFieldNames(BaseExactUpdateRecord.class, new String[0]);
    }
    
    private final static int ADDRESS_BITS_PER_WORD = 6;
    
    /**
     * 要更新的字段
     */
    @Transient
    private long[] updateFields = new long[wordIndex(FieldIndexUtils.getFieldNames(this.getClass()).length - 1) + 1];
    
    
    private static int wordIndex(int bitIndex) {
        return bitIndex >> ADDRESS_BITS_PER_WORD;
    }
    
    /**
     * 添加需要更新字段
     *
     * @param fieldNameIndex
     */
    @Override
    public void updateFieldAdd(int fieldNameIndex) {
        if (fieldNameIndex < 0) {
            throw new IndexOutOfBoundsException("fieldNameIndex < 0: " + fieldNameIndex);
        }
        int wordIndex = wordIndex(fieldNameIndex);
        updateFields[wordIndex] |= (1L << fieldNameIndex);
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
            int wordIndex = wordIndex(index);
            if ((updateFields[wordIndex] & (1L << index)) != 0) {
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
        for (int index = 0; index < updateFields.length; index++) {
            updateFields[index] = 0;
        }
    }
    
}
