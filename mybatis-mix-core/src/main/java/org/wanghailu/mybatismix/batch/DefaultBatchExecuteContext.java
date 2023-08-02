package org.wanghailu.mybatismix.batch;

import org.apache.ibatis.executor.BatchExecutor;
import org.apache.ibatis.executor.BatchExecutorException;
import org.apache.ibatis.executor.BatchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 开启batch模式时的上下文对象
 */
public class DefaultBatchExecuteContext implements IBatchExecuteContext {
    
    private static Logger logger = LoggerFactory.getLogger(DefaultBatchExecuteContext.class);
    
    private int effectiveRecordCount = 0;
    
    @Override
    public int doFlush() {
        long startTime = System.currentTimeMillis();
        int currentEffectiveRecordCount = 0;
        try {
            currentEffectiveRecordCount = doFlushBatchStatementsWithEffectiveRecordCount();
            effectiveRecordCount += currentEffectiveRecordCount;
            return currentEffectiveRecordCount;
        } finally {
            long cost = System.currentTimeMillis() - startTime;
            logger.debug("batch模式doFlushStatements耗时为" + cost + "ms，本次flush影响记录数为" + currentEffectiveRecordCount);
        }
    }
    
    @Override
    public int getEffectiveRecordCount() {
        return effectiveRecordCount;
    }
    
    @Override
    public void addEffectiveRecordCount(int count) {
        effectiveRecordCount += count;
    }
    
    @Override
    public void setEffectiveRecordCount(int count) {
        effectiveRecordCount = count;
    }
    
    protected static int doFlushBatchStatementsWithEffectiveRecordCount() {
        int currentEffectiveRecordCount = 0;
        List<BatchResult> batchResults = doFlushBatchStatements();
        for (BatchResult batchResult : batchResults) {
            for (int updateCount : batchResult.getUpdateCounts()) {
                if (updateCount < 0) {
                    updateCount = 1;
                }
                currentEffectiveRecordCount = currentEffectiveRecordCount + updateCount;
            }
        }
        return currentEffectiveRecordCount;
    }
    
    /**
     * 使用批处理模式时，进行批量提交操作
     *
     * @return
     */
    private static List<BatchResult> doFlushBatchStatements() {
        BatchExecutor batchExecutor = BatchExecutorBinder.getBatchExecutor();
        if (batchExecutor == null) {
            logger.error("batchExecutor is null,not execute any sql !!");
            return new ArrayList<>();
        } else {
            try {
                return batchExecutor.doFlushStatements(false);
            } catch (BatchExecutorException e) {
                if (e.getCause() instanceof BatchUpdateException) {
                    BatchUpdateException batchUpdateException = (BatchUpdateException) e.getCause();
                    for (Throwable throwable : batchUpdateException) {
                        logger.error(throwable.getMessage(), throwable);
                    }
                }
                throw e;
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }
    
    
    /**
     * 校验批处理结果的默认机制(默认更新影响记录数为0的时候logger打印警告信息)
     */
    public static boolean checkBatchResult(List<BatchResult> results) {
        for (BatchResult result : results) {
            for (int i = 0; i < result.getUpdateCounts().length; i++) {
                if (result.getUpdateCounts()[i] == 0) {
                    logger.warn(" sql statementId " + result.getMappedStatement().getId() + "," + result
                            .getMappedStatement().getSqlCommandType().name() + " effect 0 rows ! ");
                }
            }
        }
        return true;
    }
}
