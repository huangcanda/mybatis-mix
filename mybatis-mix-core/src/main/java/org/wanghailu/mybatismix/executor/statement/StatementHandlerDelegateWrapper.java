package org.wanghailu.mybatismix.executor.statement;

import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.ResultHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author cdhuang
 * @date 2023/1/19
 */
public class StatementHandlerDelegateWrapper implements StatementHandler {
    
    protected StatementHandler delegate;
    
    public StatementHandlerDelegateWrapper(StatementHandler delegate) {
        this.delegate = delegate;
    }
    
    public StatementHandler getDelegate() {
        return delegate;
    }
    
    @Override
    public Statement prepare(Connection connection, Integer transactionTimeout) throws SQLException {
        return getDelegate().prepare(connection, transactionTimeout);
    }
    
    @Override
    public void parameterize(Statement statement) throws SQLException {
        getDelegate().parameterize(statement);
    }
    
    @Override
    public void batch(Statement statement) throws SQLException {
        getDelegate().batch(statement);
    }
    
    @Override
    public int update(Statement statement) throws SQLException {
        return getDelegate().update(statement);
    }
    
    @Override
    public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
        return getDelegate().query(statement, resultHandler);
    }
    
    @Override
    public <E> Cursor<E> queryCursor(Statement statement) throws SQLException {
        return getDelegate().queryCursor(statement);
    }
    
    @Override
    public BoundSql getBoundSql() {
        return getDelegate().getBoundSql();
    }
    
    @Override
    public ParameterHandler getParameterHandler() {
        return getDelegate().getParameterHandler();
    }
}
