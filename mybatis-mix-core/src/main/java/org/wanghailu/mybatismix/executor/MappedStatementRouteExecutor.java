package org.wanghailu.mybatismix.executor;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.mapping.MappedStatementManager;

import java.sql.SQLException;
import java.util.List;

/**
 * @author cdhuang
 * @date 2022/1/27
 */
public class MappedStatementRouteExecutor extends ExecutorDelegateWrapper {

    public MappedStatementRouteExecutor(Executor delegate,MybatisMixConfiguration configuration) {
        super(delegate,configuration);
    }
    
    @Override
    public int update(MappedStatement ms, Object parameter) throws SQLException {
        ms = getMappedStatementManager().routeMappedStatement(ms, parameter);
        return super.update(ms, parameter);
    }
    
    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey cacheKey, BoundSql boundSql) throws SQLException {
        ms = getMappedStatementManager().routeMappedStatement(ms, parameter);
        return super.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
        ms = getMappedStatementManager().routeMappedStatement(ms, parameter);
        return super.query(ms, parameter, rowBounds, resultHandler);
    }

    @Override
    public <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException {
        ms = getMappedStatementManager().routeMappedStatement(ms, parameter);
        return super.queryCursor(ms, parameter, rowBounds);
    }
    
    protected MappedStatementManager getMappedStatementManager(){
        return configuration.getManager(MappedStatementManager.class);
    }
}
