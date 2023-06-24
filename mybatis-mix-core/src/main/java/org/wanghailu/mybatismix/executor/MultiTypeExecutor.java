package org.wanghailu.mybatismix.executor;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.transaction.Transaction;
import org.wanghailu.mybatismix.MybatisMixConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 拓展原生mybatis，支持一个事务里同时使用SimpleExecutor和BatchExecutor等多种执行类型执行器
 * Created by cd_huang on 2019/4/24.
 */
public class MultiTypeExecutor extends ExecutorDelegateWrapper {
    
    protected Executor defaultExecutor;

    protected Map<String, Executor> executorMap = new HashMap<>();

    private String defaultExecutorType;


    public MultiTypeExecutor(MybatisMixConfiguration configuration, Executor defaultExecutor, String defaultExecutorType) {
        super(defaultExecutor,configuration);
        this.defaultExecutor = defaultExecutor;
        this.defaultExecutorType = defaultExecutorType;
    }

    @Override
    public Executor getDelegate() {
        return getCurrentExecutor();
    }

    protected Executor getCurrentExecutor() {
        String currentExecutorType = ExecutorTypeContext.getCurrentExecutorType();
        if (currentExecutorType != null && !currentExecutorType.equals(defaultExecutorType)) {
            Executor executor = executorMap.get(currentExecutorType);
            if (executor == null) {
                executor = getNewExecutor(currentExecutorType);
                executorMap.put(currentExecutorType, executor);
            }
            return executor;
        }
        return defaultExecutor;
    }

    protected Executor getNewExecutor(String executorType) {
        Transaction transaction = new DelegateTransaction(defaultExecutor.getTransaction());
        Executor executor = configuration.getManager(ExecutorManager.class).newOriginalExecutor(transaction,executorType);
        return executor;
    }

    protected Collection<Executor> getExecutors() {
        return executorMap.values();
    }

    @Override
    public void commit(boolean required) throws SQLException {
        for (Executor executor : getExecutors()) {
            executor.commit(required);
        }
        defaultExecutor.commit(required);
    }

    @Override
    public void rollback(boolean required) throws SQLException {
        for (Executor executor : getExecutors()) {
            executor.rollback(required);
        }
        defaultExecutor.rollback(required);
    }

    @Override
    public void clearLocalCache() {
        for (Executor executor : getExecutors()) {
            executor.clearLocalCache();
        }
        defaultExecutor.clearLocalCache();
    }

    @Override
    public void close(boolean forceRollback) {
        for (Executor executor : getExecutors()) {
            executor.close(forceRollback);
        }
        defaultExecutor.close(forceRollback);
        ExecutorTypeContext.clean();
    }

    /**
     * 使用装饰类，除了默认的Executor，其他Executor不会对Transaction进行commit,rollback,close的处理
     */
    private class DelegateTransaction implements Transaction {

        private Transaction delegate;

        public DelegateTransaction(Transaction delegate) {
            this.delegate = delegate;
        }

        @Override
        public Connection getConnection() throws SQLException {
            return delegate.getConnection();
        }

        @Override
        public void commit() {}

        @Override
        public void rollback(){}

        @Override
        public void close() {}

        @Override
        public Integer getTimeout() throws SQLException {
            return delegate.getTimeout();
        }
    }
}
