package org.wanghailu.mybatismix.executor;

import org.apache.ibatis.executor.BatchExecutor;

/**
 * batch执行器上下文（线程变量绑定）
 * Created by cd_huang on 2019/4/30.
 */
 class BatchExecutorContext {

	private static final ThreadLocal<BatchExecutor> batchExecutorContext = new ThreadLocal<>();

	protected static void bindBatchExecutor(BatchExecutor batchExecutor){
		batchExecutorContext.set(batchExecutor);
	}
	
	protected static BatchExecutor getBatchExecutor(){
		return batchExecutorContext.get();
	}
	
	protected static void clean(){
		batchExecutorContext.remove();
	}
}
