package org.wanghailu.mybatismix.model;

import java.util.Collection;

/**
 * 可进行精准更新
 * @author cdhuang
 * @date 2023/1/28
 */
public interface ExactUpdateEnable {
    
    /**
     *
     * @param fieldName
     */
    void addUpdateField(String fieldName);
    /**
     * 获得记录
     * @return
     */
    Collection<String> gainUpdateFields();
    /**
     * 清除记录
     * @return
     */
    void wipeUpdateFields();

}
