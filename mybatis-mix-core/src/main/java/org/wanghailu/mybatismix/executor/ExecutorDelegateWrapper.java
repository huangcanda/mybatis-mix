package org.wanghailu.mybatismix.executor;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wanghailu.mybatismix.MybatisMixConfiguration;

import java.sql.SQLException;
import java.util.List;

/**
 * 基于适配器的执行器
 */
public class ExecutorDelegateWrapper implements Executor {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected Executor delegate;
    
    protected MybatisMixConfiguration configuration;

    public ExecutorDelegateWrapper(Executor delegate,MybatisMixConfiguration configuration) {
        this.delegate = delegate;
        this.configuration = configuration;
    }

    public Executor getDelegate() {
        return delegate;
    }

    @Override
    public int update(MappedStatement ms, Object parameter) throws SQLException {
        return getDelegate().update(ms, parameter);
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey cacheKey, BoundSql boundSql) throws SQLException {
        return getDelegate().query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
        return getDelegate().query(ms, parameter, rowBounds, resultHandler);
    }

    @Override
    public <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException {
        return getDelegate().queryCursor(ms, parameter, rowBounds);
    }

    @Override
    public List<BatchResult> flushStatements() throws SQLException {
        return getDelegate().flushStatements();
    }

    @Override
    public void commit(boolean required) throws SQLException {
        getDelegate().commit(required);
    }

    @Override
    public void rollback(boolean required) throws SQLException {
        getDelegate().rollback(required);
    }

    @Override
    public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
        return getDelegate().createCacheKey(ms, parameterObject, rowBounds, boundSql);
    }

    @Override
    public boolean isCached(MappedStatement ms, CacheKey key) {
        return getDelegate().isCached(ms, key);
    }

    @Override
    public void clearLocalCache() {
        getDelegate().clearLocalCache();
    }

    @Override
    public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType) {
        getDelegate().deferLoad(ms,resultObject,property,key,targetType);
    }

    @Override
    public Transaction getTransaction() {
        return getDelegate().getTransaction();
    }

    @Override
    public void close(boolean forceRollback) {
        getDelegate().close(forceRollback);
    }

    @Override
    public boolean isClosed() {
        return getDelegate().isClosed();
    }

    @Override
    public void setExecutorWrapper(Executor executor) {
        getDelegate().setExecutorWrapper(executor);
    }
}
