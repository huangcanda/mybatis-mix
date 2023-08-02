package org.wanghailu.mybatismix.resultset;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.result.DefaultResultHandler;
import org.apache.ibatis.executor.resultset.DefaultResultSetHandler;
import org.apache.ibatis.executor.resultset.ResultSetWrapper;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 对结果集进行后置处理
 * @author cdhuang
 * @date 2022/3/25
 */
public class ExtResultSetHandler extends DefaultResultSetHandler {
    
    public ExtResultSetHandler(Executor executor, MappedStatement mappedStatement, ParameterHandler parameterHandler,
            ResultHandler<?> resultHandler, BoundSql boundSql, RowBounds rowBounds) {
        super(executor, mappedStatement, parameterHandler, resultHandler, boundSql, rowBounds);
    }
    
    @Override
    public List<Object> handleResultSets(Statement stmt) throws SQLException {
        List<Object> result = super.handleResultSets(stmt);
        boolean haveNullValue = false;
        for (Object obj : result) {
            if (obj == null) {
                haveNullValue = true;
            }
        }
        if (haveNullValue) {
            result = result.stream().filter(x -> x != null).collect(Collectors.toList());
        }
        return result;
    }
    
    @Override
    public void handleRowValues(ResultSetWrapper rsw, ResultMap resultMap, ResultHandler<?> resultHandler,
            RowBounds rowBounds, ResultMapping parentMapping) throws SQLException {
        if (resultHandler instanceof DefaultResultHandler) {
            super.handleRowValues(rsw, resultMap, resultHandler, rowBounds, parentMapping);
            for (Object obj : ((DefaultResultHandler) resultHandler).getResultList()) {
                HandleResultValue.handle(obj);
            }
        } else {
            super.handleRowValues(rsw, resultMap,
                    resultHandler == null ? null : new DelegateResultHandler(resultHandler), rowBounds, parentMapping);
        }
    }
    
    
}
