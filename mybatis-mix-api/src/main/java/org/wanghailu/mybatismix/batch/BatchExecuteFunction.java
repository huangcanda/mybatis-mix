package org.wanghailu.mybatismix.batch;

/**
 * 批量执行动作抽象
 * @author cdhuang
 * @date 2023/1/17
 */
public interface BatchExecuteFunction<T> {
    
    T batchExecute(IBatchExecuteContext context);
}
