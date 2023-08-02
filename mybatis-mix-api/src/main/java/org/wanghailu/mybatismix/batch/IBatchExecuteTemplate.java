package org.wanghailu.mybatismix.batch;

/**
 * Mybatis批处理模版
 * @author cdhuang
 * @date 2023/7/31
 */
public interface IBatchExecuteTemplate {
    
    /**
     * 以批处理模式执行
     *
     * @param batchExecuteFunction
     * @return
     */
    <T> T executeOnBatchMode(BatchExecuteFunction<T> batchExecuteFunction);
    
    /**
     * 以批处理模式执行
     *
     * @param batchExecuteContext
     * @param batchExecuteFunction
     * @param <T>
     * @return
     */
    <T> T executeOnBatchMode(IBatchExecuteContext batchExecuteContext, BatchExecuteFunction<T> batchExecuteFunction);
}
