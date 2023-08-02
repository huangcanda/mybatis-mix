package org.wanghailu.mybatismix.batch;

import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.executor.ExecutorTypeContext;
import org.wanghailu.mybatismix.transaction.TransactionRunner;

/**
 * @author cdhuang
 * @date 2023/7/31
 */
public class BatchExecuteTemplate implements IBatchExecuteTemplate {
    
    private MybatisMixConfiguration configuration;
    
    public BatchExecuteTemplate(MybatisMixConfiguration configuration) {
        this.configuration = configuration;
    }
    
    @Override
    public <T> T executeOnBatchMode(BatchExecuteFunction<T> batchExecuteFunction) {
        DefaultBatchExecuteContext context = new DefaultBatchExecuteContext();
        return executeOnBatchMode(context, batchExecuteFunction);
    }
    
    @Override
    public <T> T executeOnBatchMode(IBatchExecuteContext context, BatchExecuteFunction<T> batchExecuteFunction) {
        return TransactionRunner.forceRunInTransaction(configuration, () -> {
            if (ExecutorTypeContext.isBatchExecutorMode()) {
                try {
                    BatchExecuteContextBinder.bindBatchExecutor(context);
                    return batchExecuteFunction.batchExecute(context);
                } finally {
                    context.doFlush();
                    BatchExecuteContextBinder.clean();
                }
            } else {
                try {
                    BatchExecuteContextBinder.bindBatchExecutor(context);
                    ExecutorTypeContext.openBatchExecutorMode();
                    return batchExecuteFunction.batchExecute(context);
                } finally {
                    context.doFlush();
                    ExecutorTypeContext.closeExecutorMode();
                    BatchExecuteContextBinder.clean();
                }
            }
        });
    }
    
}
