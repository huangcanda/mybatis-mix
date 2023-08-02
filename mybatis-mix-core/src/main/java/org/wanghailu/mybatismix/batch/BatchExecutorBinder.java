package org.wanghailu.mybatismix.batch;

import org.apache.ibatis.executor.BatchExecutor;

/**
 * batch执行器上下文（线程变量绑定）
 * Created by cd_huang on 2023/7/30.
 */
public class BatchExecutorBinder {

	private static final ThreadLocal<BatchExecutor> batchExecutorBinder = new ThreadLocal<>();
	
	public static void bindBatchExecutor(BatchExecutor batchExecutor){
		batchExecutorBinder.set(batchExecutor);
	}
	
	public static BatchExecutor getBatchExecutor(){
		return batchExecutorBinder.get();
	}
	
	public static void clean(){
		batchExecutorBinder.remove();
	}
}
