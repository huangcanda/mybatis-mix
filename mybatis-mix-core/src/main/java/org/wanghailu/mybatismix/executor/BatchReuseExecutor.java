package org.wanghailu.mybatismix.executor;

import org.apache.ibatis.executor.BatchExecutor;
import org.apache.ibatis.executor.BatchExecutorException;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;
import org.wanghailu.mybatismix.support.TwoTuple;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 修改BatchExecutor类，
 * Batch执行器结合Reuse执行器
 *
 * @author cdhuang
 * @date 2020/11/23
 */
public class BatchReuseExecutor extends BatchExecutor {

    public static final String EXECUTOR_TYPE = "BATCH-REUSE";
    
    /**
     * mybatis原生batch模式需要相同的sql一起执行，
     * 但实际代码运行时，经常存在相同的sql间隔执行，
     * 此时不能复用statement，造成资源浪费。
     * 所以这里用map对statement进行复用
     * 注意：这里会对sql执行顺序造成破坏，可能会影响业务逻辑
     */
    private final Map<TwoTuple<String, MappedStatement>, TwoTuple<Statement, BatchResult>> statementMap = new LinkedHashMap<>();

    public BatchReuseExecutor(Configuration configuration, Transaction transaction) {
        super(configuration, transaction);
    }

    @Override
    public int doUpdate(MappedStatement ms, Object parameterObject) throws SQLException {
        final Configuration configuration = ms.getConfiguration();
        final StatementHandler handler = configuration.newStatementHandler(this, ms, parameterObject, RowBounds.DEFAULT, null, null);
        final BoundSql boundSql = handler.getBoundSql();
        final String sql = boundSql.getSql();
        final Statement stmt;
        TwoTuple<String, MappedStatement> statementKey = new TwoTuple<>(sql, ms);
        TwoTuple<Statement, BatchResult> statementTwoTuple = statementMap.get(statementKey);
        if (statementTwoTuple != null) {
            stmt = statementTwoTuple.getFirst();
            BatchResult batchResult = statementTwoTuple.getSecond();
            batchResult.addParameterObject(parameterObject);
            applyTransactionTimeout(stmt);
        } else {
            Connection connection = getConnection(ms.getStatementLog());
            stmt = handler.prepare(connection,transaction.getTimeout());
            statementMap.put(statementKey, new TwoTuple<>(stmt, new BatchResult(ms, sql, parameterObject)));
        }
        handler.parameterize(stmt);
        handler.batch(stmt);
        return BATCH_UPDATE_RETURN_VALUE;
    }

    @Override
    public List<BatchResult> doFlushStatements(boolean isRollback) throws SQLException {
        try {
            List<BatchResult> results = new ArrayList<BatchResult>();
            if (isRollback) {
                return Collections.emptyList();
            } else {
                for (Map.Entry<TwoTuple<String, MappedStatement>, TwoTuple<Statement, BatchResult>> twoTupleTwoTupleEntry : statementMap.entrySet()) {
                    TwoTuple<Statement, BatchResult> value = twoTupleTwoTupleEntry.getValue();
                    Statement stmt = value.getFirst();
                    BatchResult batchResult = value.getSecond();
                    try {
                        batchResult.setUpdateCounts(stmt.executeBatch());
                        MappedStatement ms = batchResult.getMappedStatement();
                        List<Object> parameterObjects = batchResult.getParameterObjects();
                        KeyGenerator keyGenerator = ms.getKeyGenerator();
                        if (Jdbc3KeyGenerator.class.equals(keyGenerator.getClass())) {
                            Jdbc3KeyGenerator jdbc3KeyGenerator = (Jdbc3KeyGenerator) keyGenerator;
                            jdbc3KeyGenerator.processBatch(ms, stmt, parameterObjects);
                        } else if (!NoKeyGenerator.class.equals(keyGenerator.getClass())) {
                            for (Object parameter : parameterObjects) {
                                keyGenerator.processAfter(this, ms, stmt, parameter);
                            }
                        }
                        closeStatement(stmt);
                    } catch (BatchUpdateException e) {
                        StringBuilder message = new StringBuilder();
                        message.append(batchResult.getMappedStatement().getId())
                                .append(" (batch execute failed. sql:" + twoTupleTwoTupleEntry.getKey().getFirst());
                        throw new BatchExecutorException(message.toString(), e, results, batchResult);
                    }
                    results.add(batchResult);
                }
                return results;
            }
        } finally {
            for (TwoTuple<Statement, BatchResult> value : statementMap.values()) {
                closeStatement(value.getFirst());
            }
            statementMap.clear();
        }
    }

    @Override
    public void close(boolean forceRollback) {
        super.close(forceRollback);
    }
}
