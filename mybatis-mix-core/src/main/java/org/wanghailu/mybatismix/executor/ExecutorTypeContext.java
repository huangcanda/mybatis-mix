package org.wanghailu.mybatismix.executor;

import org.apache.ibatis.executor.BatchExecutor;
import org.apache.ibatis.executor.BatchExecutorException;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.session.ExecutorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wanghailu.mybatismix.constant.ConfigurationKeyConstant;
import org.wanghailu.mybatismix.util.MybatisContext;

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * mybatis执行器上下文 Created by cd_huang on 2019/4/24.
 */
public class ExecutorTypeContext {
    
    private static Logger logger = LoggerFactory.getLogger(ExecutorTypeContext.class);
    
    private static final ThreadLocal<String> currentExecutorType = new ThreadLocal<>();
    
    public static void setExecutorType(String executorType) {
        currentExecutorType.set(executorType);
    }
    
    public static void openBatchExecutorMode() {
        currentExecutorType.set(MybatisContext.getConfiguration().getProperty(
                ConfigurationKeyConstant.defaultBatchExecutorType, ExecutorType.BATCH.name()));
    }
    
    public static boolean isBatchExecutorMode() {
        String executorType = currentExecutorType.get();
        return isBatchExecutorMode(executorType);
    }
    
    protected static boolean isBatchExecutorMode(String executorType){
        return executorType!=null && executorType.startsWith("BATCH");
    }
    
    /**
     * 使用批处理模式时，进行批量提交操作
     *
     * @return
     */
    public static List<BatchResult> doFlushBatchStatements() {
        BatchExecutor batchExecutor = BatchExecutorContext.getBatchExecutor();
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
    
    public static void closeExecutorMode() {
        currentExecutorType.remove();
    }
    
    public static String getCurrentExecutorType() {
        return currentExecutorType.get();
    }
    
    public static void clean() {
        currentExecutorType.remove();
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
