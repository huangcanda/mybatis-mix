package org.wanghailu.mybatismix.executor;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.fillfield.FillFieldManager;

import java.sql.SQLException;
import java.util.List;

/**
 * @author cdhuang
 * @date 2022/12/28
 */
public class FillFieldExecutor extends ExecutorDelegateWrapper {

    protected FillFieldManager fillFieldManager;
    
    public FillFieldExecutor(Executor delegate, MybatisMixConfiguration configuration) {
        super(delegate, configuration);
        fillFieldManager = configuration.getManager(FillFieldManager.class);
    }
    
    @Override
    public int update(MappedStatement ms, Object parameter) throws SQLException {
        fillFieldManager.fillFieldBeforeInvoke(ms, parameter);
        Integer result = null;
        try{
            return result = super.update(ms, parameter);
        }finally {
            fillFieldManager.fillFieldAfterInvoke(ms, parameter, result);
        }
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey cacheKey, BoundSql boundSql) throws SQLException {
        fillFieldManager.fillFieldBeforeInvoke(ms, parameter);
        List<E> result = null;
        try{
            return result = super.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
        }finally {
            fillFieldManager.fillFieldAfterInvoke(ms, parameter, result);
        }
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
        fillFieldManager.fillFieldBeforeInvoke(ms, parameter);
        List<E> result = null;
        try{
            return result = super.query(ms, parameter, rowBounds, resultHandler);
        }finally {
            fillFieldManager.fillFieldAfterInvoke(ms, parameter, result);
        }
    }

    @Override
    public <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException {
        fillFieldManager.fillFieldBeforeInvoke(ms, parameter);
        Cursor<E> result = null;
        try{
            return result = super.queryCursor(ms, parameter, rowBounds);
        }finally {
            fillFieldManager.fillFieldAfterInvoke(ms, parameter, result);
        }
    }
}