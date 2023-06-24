package org.wanghailu.mybatismix.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wanghailu.mybatismix.MybatisMixConfiguration;

/**
 * mybatis事务模版
 */
public class TransactionRunner {
    
    public static Logger logger = LoggerFactory.getLogger(TransactionRunner.class);
    
    public static <T> T forceRunInTransaction(MybatisMixConfiguration configuration, TransactionCall<T> callback) {
        TransactionalSessionManager transactionalSessionManager = configuration
                .getManager(TransactionalSessionManager.class);
        if (transactionalSessionManager != null) {
            return transactionalSessionManager.forceRunInTransaction(callback);
        } else {
            logger.warn("找不到TransactionalSessionManager，以无事务方式执行！");
            return callback.execute();
        }
    }
    
    public static void forceRunInTransaction(MybatisMixConfiguration configuration, Runnable runnable) {
        TransactionalSessionManager transactionalSessionManager = configuration
                .getManager(TransactionalSessionManager.class);
        if (transactionalSessionManager != null) {
            transactionalSessionManager.forceRunInTransaction(() -> {
                runnable.run();
                return null;
            });
        } else {
            logger.warn("找不到TransactionalSessionManager，以无事务方式执行！");
            runnable.run();
        }
    }
}
