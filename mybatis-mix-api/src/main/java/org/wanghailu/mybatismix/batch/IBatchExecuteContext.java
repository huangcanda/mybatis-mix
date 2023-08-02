package org.wanghailu.mybatismix.batch;

/**
 * Mybatis批量执行的上下文抽象
 * @author cdhuang
 * @date 2023/1/17
 */
public interface IBatchExecuteContext {
    
    int doFlush();
    
    int getEffectiveRecordCount();
    
    void addEffectiveRecordCount(int count);
    
    void setEffectiveRecordCount(int count);
}
