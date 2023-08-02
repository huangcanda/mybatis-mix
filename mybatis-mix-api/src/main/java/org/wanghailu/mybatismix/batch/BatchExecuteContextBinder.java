package org.wanghailu.mybatismix.batch;

import java.util.ArrayList;
import java.util.List;

/**
 * 在Mybatis批处理中，获得IBatchExecuteContext对象，进行自定义的flush操作
 * 由于Batch模式存在多重嵌套的情况，所以使用链表记录
 * @author cdhuang
 * @date 2023/8/1
 */
public class BatchExecuteContextBinder {
    
    private static final ThreadLocal<List<IBatchExecuteContext>> batchExecuteContextBinder = new ThreadLocal<List<IBatchExecuteContext>>();
    
    public static void bindBatchExecutor(IBatchExecuteContext batchExecutor){
        List<IBatchExecuteContext> list = batchExecuteContextBinder.get();
        if(list==null){
            list = new ArrayList<>();
            batchExecuteContextBinder.set(list);
        }
        list.add(batchExecutor);
    }
    
    public static IBatchExecuteContext getBatchExecutor(){
        List<IBatchExecuteContext> list = batchExecuteContextBinder.get();
        if(list == null || list.size()==0){
            return null;
        }
        return list.get(list.size()-1);
    }
    
    public static void clean(){
        List<IBatchExecuteContext> list = batchExecuteContextBinder.get();
        if(list.size()>1){
            IBatchExecuteContext removeContext = list.remove(list.size()-1);
            int effectiveRecordCount = removeContext.getEffectiveRecordCount();
            for (IBatchExecuteContext batchExecuteContext : list) {
                batchExecuteContext.addEffectiveRecordCount(effectiveRecordCount);
            }
        }else{
            batchExecuteContextBinder.remove();
        }
    }
}
