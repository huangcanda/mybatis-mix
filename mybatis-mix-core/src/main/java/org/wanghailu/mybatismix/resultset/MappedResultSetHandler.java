package org.wanghailu.mybatismix.resultset;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

/**
 *
 * @author cdhuang
 * @date 2022/12/1
 */
@Deprecated
public class MappedResultSetHandler extends ExtResultSetHandler {
    
    public MappedResultSetHandler(Executor executor, MappedStatement mappedStatement, ParameterHandler parameterHandler,
            ResultHandler<?> resultHandler, BoundSql boundSql, RowBounds rowBounds) {
        super(executor, mappedStatement, parameterHandler, resultHandler, boundSql, rowBounds);
    }
}
