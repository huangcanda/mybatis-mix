package org.wanghailu.mybatismix.executor;

import org.apache.ibatis.executor.BatchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wanghailu.mybatismix.support.IBatchContext;

import java.util.List;

/**
 * 开启batch模式时的上下文对象
 */
public class DefaultBatchContext implements IBatchContext {

    private static Logger logger = LoggerFactory.getLogger(DefaultBatchContext.class);

    private int effectiveRecordCount = 0;

    @Override
    public void doFlush() {
        long startTime = System.currentTimeMillis();
        int currentEffectiveRecordCount = 0;
        try {
            List<BatchResult> batchResults = ExecutorTypeContext.doFlushBatchStatements();
            for (BatchResult batchResult : batchResults) {
                for (int updateCount : batchResult.getUpdateCounts()) {
                    if (updateCount < 0) {
                        updateCount = 1;
                    }
                    currentEffectiveRecordCount = currentEffectiveRecordCount + updateCount;
                }
            }
            effectiveRecordCount += currentEffectiveRecordCount;
        } finally {
            long cost = System.currentTimeMillis() - startTime;
            logger.debug("batch模式doFlushStatements耗时为" + cost + "ms，本次flush影响记录数为" + currentEffectiveRecordCount);
        }

    }
    
    @Override
    public int getEffectiveRecordCount() {
        return effectiveRecordCount;
    }
}
