package org.wanghailu.mybatismix.executor;

import org.apache.ibatis.executor.BatchExecutor;
import org.apache.ibatis.executor.CachingExecutor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ReuseExecutor;
import org.apache.ibatis.executor.SimpleExecutor;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.transaction.Transaction;
import org.wanghailu.mybatismix.batch.BatchExecuteTemplate;
import org.wanghailu.mybatismix.batch.BatchExecuteTemplateBinder;
import org.wanghailu.mybatismix.batch.BatchExecutorBinder;
import org.wanghailu.mybatismix.common.BaseManager;
import org.wanghailu.mybatismix.util.SpiExtensionLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * 定义如何创建一个基础的执行器
 *
 * @author cdhuang
 * @date 2022/12/28
 */
public class ExecutorManager extends BaseManager {
    
    private static Map<String, ExecutorSupplier> supplierMap = new HashMap<>();
    
    static {
        supplierMap.put(ExecutorType.SIMPLE.name(),
                (configuration, transaction) -> new SimpleExecutor(configuration, transaction));
        supplierMap.put(ExecutorType.BATCH.name(),
                (configuration, transaction) -> new BatchExecutor(configuration, transaction));
        supplierMap.put(ExecutorType.REUSE.name(),
                (configuration, transaction) -> new ReuseExecutor(configuration, transaction));
        supplierMap.put(BatchReuseExecutor.EXECUTOR_TYPE,
                (configuration, transaction) -> new BatchReuseExecutor(configuration, transaction));
        SpiExtensionLoader<ExecutorSupplier> serviceLoader = SpiExtensionLoader.load(ExecutorSupplier.class);
        for (ExecutorSupplier supplier : serviceLoader) {
            supplierMap.put(supplier.executorType(), supplier);
        }
        supplierMap.putAll(SpiExtensionLoader.loadSpiExtensionMap(ExecutorSupplier.class));
    }
    
    @Override
    public void initAfterSetProperties() {
        super.initAfterSetProperties();
        BatchExecuteTemplateBinder.setTemplate(new BatchExecuteTemplate(configuration));
    }
    
    public Executor newOriginalExecutor(Transaction transaction, String executorType) {
        Executor executor = supplierMap.get(executorType).newExecutor(configuration, transaction);
        if (executor instanceof BatchExecutor && ExecutorTypeContext.isBatchExecutorMode(executorType)) {
            BatchExecutorBinder.bindBatchExecutor((BatchExecutor) executor);
        }
        return executor;
    }
    
    public Executor newExecutor(Transaction transaction, ExecutorType executorType) {
        executorType = executorType == null ? configuration.getDefaultExecutorType() : executorType;
        executorType = executorType == null ? ExecutorType.SIMPLE : executorType;
        Executor executor = newOriginalExecutor(transaction, executorType.name());
        executor = new MultiTypeExecutor(configuration, executor, executorType.name());
        if (configuration.isCacheEnabled()) {
            executor = new CachingExecutor(executor);
        }
        executor = new PageExecutor(executor, configuration);
        executor = new FillFieldExecutor(executor, configuration);
        executor = (Executor) configuration.getInterceptorChain().pluginAll(executor);
        executor = new MappedStatementRouteExecutor(executor, configuration);
        return executor;
    }
}
