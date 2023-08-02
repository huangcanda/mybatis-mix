package org.wanghailu.mybatismix.executor;

import org.wanghailu.mybatismix.constant.ConfigurationKeyConstant;
import org.wanghailu.mybatismix.util.MybatisContext;

/**
 * mybatis执行器类型上下文
 */
public class ExecutorTypeContext {
    
    private static final ThreadLocal<String> currentExecutorType = new ThreadLocal<>();
    
    public static void setExecutorType(String executorType) {
        currentExecutorType.set(executorType);
    }
    
    public static void openBatchExecutorMode() {
        setExecutorType(MybatisContext.getConfiguration().getProperty(
                ConfigurationKeyConstant.defaultBatchExecutorType, "BATCH"));
    }
    
    public static boolean isBatchExecutorMode() {
        String executorType = currentExecutorType.get();
        return isBatchExecutorMode(executorType);
    }
    
    protected static boolean isBatchExecutorMode(String executorType){
        return executorType!=null && executorType.startsWith("BATCH");
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
}
