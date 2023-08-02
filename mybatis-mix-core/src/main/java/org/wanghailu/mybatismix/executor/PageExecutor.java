package org.wanghailu.mybatismix.executor;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.mapping.MappedStatementManager;
import org.wanghailu.mybatismix.model.Pageable;
import org.wanghailu.mybatismix.page.PageHelper;
import org.wanghailu.mybatismix.page.mapping.CountMappedStatementSupplier;
import org.wanghailu.mybatismix.page.mapping.PageMappedStatementSupplier;
import org.wanghailu.mybatismix.util.TruckUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分页路由分发执行器
 */
public class PageExecutor extends ExecutorDelegateWrapper {
    
    private static final Map<String, Object> EMPTY_OBJECT = new HashMap<>();
    
    public PageExecutor(Executor delegate,MybatisMixConfiguration configuration) {
        super(delegate,configuration);
    }
    
    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler,
            CacheKey cacheKey, BoundSql boundSql) throws SQLException {
        return doPageQuery(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
    }
    
    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler)
            throws SQLException {
        return doPageQuery(ms, parameter, rowBounds, resultHandler, null, null);
    }
    
    /**
     * 针对分页的处理
     * @param mappedStatement
     * @param parameter
     * @param rowBounds
     * @param resultHandler
     * @param cacheKey
     * @param boundSql
     * @param <E>
     * @return
     * @throws SQLException
     */
    protected <E> List<E> doPageQuery(MappedStatement mappedStatement, Object parameter, RowBounds rowBounds,
            ResultHandler resultHandler, CacheKey cacheKey, BoundSql boundSql) throws SQLException {
        Pageable localPage = PageHelper.getLocalPage();
        if (localPage != null) {
            try {
                MappedStatementManager mappedStatementManager = configuration.getManager(MappedStatementManager.class);
                if (localPage.isSelectCount()) {
                    MappedStatement countMappedStatement = mappedStatementManager
                            .getMappedStatement(mappedStatement, new CountMappedStatementSupplier(mappedStatement));
                    List countResult = realDoQuery(countMappedStatement, parameter, rowBounds, resultHandler, cacheKey,
                            null);
                    long countSize = TruckUtils.getCountSizeByResultList(countResult);
                    localPage.setCountSize(countSize);
                    if(countSize==0){
                        return new ArrayList<>();
                    }
                }
                MappedStatement pageMappedStatement = mappedStatementManager
                        .getMappedStatement(mappedStatement, new PageMappedStatementSupplier(mappedStatement));
                return realDoQuery(pageMappedStatement, parameter == null ? EMPTY_OBJECT : parameter, rowBounds,
                        resultHandler, cacheKey, null);
            } finally {
                PageHelper.clearPage();
            }
        } else {
            return realDoQuery(mappedStatement, parameter, rowBounds, resultHandler, cacheKey, boundSql);
        }
    }
    
    public <E> List<E> realDoQuery(MappedStatement ms, Object parameter, RowBounds rowBounds,
            ResultHandler resultHandler, CacheKey cacheKey, BoundSql boundSql) throws SQLException {
        if (cacheKey == null && boundSql == null) {
            return super.query(ms, parameter, rowBounds, resultHandler);
        } else {
            if(boundSql==null){
                boundSql = ms.getBoundSql(parameter);
            }
            return super.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
        }
    }
}
