package org.wanghailu.mybatismix.support;

/**
 * 批量执行的上下文
 * @author cdhuang
 * @date 2023/1/17
 */
public interface IBatchContext {
    
    void doFlush();
    
    int getEffectiveRecordCount();
}
