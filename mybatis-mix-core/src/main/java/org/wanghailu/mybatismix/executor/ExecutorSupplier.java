package org.wanghailu.mybatismix.executor;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.transaction.Transaction;
import org.wanghailu.mybatismix.exception.MybatisMixException;
import org.wanghailu.mybatismix.support.SpiExtension;

/**
 * 定义如何提供一个Executor
 */
public interface ExecutorSupplier extends SpiExtension {
    
    
    /**
     * 如何提供一个Executor
     * @param configuration
     * @param transaction
     * @return
     */
    Executor newExecutor(Configuration configuration, Transaction transaction);
    
    /**
     * 指定executorType，为了方便使用lambda，搞一个默认实现
     * @return
     */
    default String executorType(){
        throw new MybatisMixException("必须指定executorType");
    }

    @Override
    default String name(){
        return executorType();
    }
}
