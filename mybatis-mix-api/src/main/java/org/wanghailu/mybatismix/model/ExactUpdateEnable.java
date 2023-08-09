package org.wanghailu.mybatismix.model;

import java.util.Collection;

/**
 * 可进行精准更新
 * @author cdhuang
 * @date 2023/1/28
 */
public interface ExactUpdateEnable {
    
    /**
     * 添加字段的index
     * @param fieldNameIndex
     */
    void updateFieldAdd(int fieldNameIndex);
    /**
     * 获得记录
     * @return
     */
    Collection<String> updateFieldsSelect();
    /**
     * 清除记录
     * @return
     */
    void updateFieldsClear();

}
