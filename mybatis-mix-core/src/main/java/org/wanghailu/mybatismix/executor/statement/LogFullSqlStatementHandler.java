package org.wanghailu.mybatismix.executor.statement;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.logging.LogSqlManager;
import org.wanghailu.mybatismix.page.mapping.CountMappedStatementSupplier;
import org.wanghailu.mybatismix.util.TruckUtils;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 打印完整sql语句
 *
 * @author cdhuang
 * @date 2023/1/19
 */
public class LogFullSqlStatementHandler extends StatementHandlerDelegateWrapper {
    
    private static Logger logger = LoggerFactory.getLogger(LogFullSqlStatementHandler.class);
    
    protected MappedStatement mappedStatement;
    
    protected LogSqlManager logSqlManager;
    
    public LogFullSqlStatementHandler(StatementHandler delegate, MappedStatement mappedStatement) {
        super(delegate);
        this.mappedStatement = mappedStatement;
        MybatisMixConfiguration configuration = (MybatisMixConfiguration) mappedStatement.getConfiguration();
        this.logSqlManager = configuration.getManager(LogSqlManager.class);
    }
    
    @Override
    public void batch(Statement statement) throws SQLException {
        long startTime = System.currentTimeMillis();
        try {
            super.batch(statement);
        } finally {
            logFullSql(startTime, -1);
        }
        
    }
    
    @Override
    public int update(Statement statement) throws SQLException {
        long startTime = System.currentTimeMillis();
        int result = 0;
        try {
            result = super.update(statement);
            return result;
        } finally {
            logFullSql(startTime, result);
        }
    }
    
    protected boolean isCountSql() {
        String statementId = mappedStatement.getId();
        if (statementId.endsWith(CountMappedStatementSupplier.statementSuffix)) {
            return true;
        }
        int index = statementId.lastIndexOf(".");
        String name = index == -1 ? statementId : statementId.substring(index + 1);
        if (name.startsWith("count")) {
            Class resultType = mappedStatement.getResultMaps().get(0).getType();
            if (int.class.equals(resultType) || Integer.class.equals(resultType)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
        long startTime = System.currentTimeMillis();
        long result = 0;
        try {
            List<E> resultList = super.query(statement, resultHandler);
            if (isCountSql()) {
                result = TruckUtils.getCountSizeByResultList(resultList);
            } else {
                result = resultList.size();
            }
            return resultList;
        } finally {
            logFullSql(startTime, result);
        }
    }
    
    protected void logFullSql(long startTime, long result) {
        try {
            logSqlManager.getLogFullSqlProcessor()
                    .logFullSql(mappedStatement, startTime, delegate.getBoundSql(), result);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }
}
