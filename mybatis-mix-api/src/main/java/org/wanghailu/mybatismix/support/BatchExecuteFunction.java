package org.wanghailu.mybatismix.support;

/**
 * 批量执行动作
 * @author cdhuang
 * @date 2023/1/17
 */
public interface BatchExecuteFunction {
    
    void batchExecute(IBatchContext context);
}
