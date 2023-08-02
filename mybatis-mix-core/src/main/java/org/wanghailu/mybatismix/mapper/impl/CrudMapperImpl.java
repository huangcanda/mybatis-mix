package org.wanghailu.mybatismix.mapper.impl;

import org.apache.ibatis.session.SqlSession;
import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.batch.BatchExecuteTemplateBinder;
import org.wanghailu.mybatismix.batch.DefaultBatchExecuteContext;
import org.wanghailu.mybatismix.constant.UpdateModeEnum;
import org.wanghailu.mybatismix.mapper.ICrudMapper;
import org.wanghailu.mybatismix.util.TruckUtils;

import java.util.Arrays;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * @author cdhuang
 * @date 2023/1/3
 */
public class CrudMapperImpl<Entity> extends BaseCrudMapperImpl<Entity> implements ICrudMapper<Entity> {
    
    private static int defaultBatchFlushDataSize = 1000;
    
    public CrudMapperImpl(MybatisMixConfiguration configuration, SqlSession sqlSession, Class aClass) {
        super(configuration, sqlSession, aClass);
    }
    
    @Override
    public int batchInsert(List<Entity> entities) {
        return newBatchExecuteTemplateForList(entities, entity -> insert(entity), defaultBatchFlushDataSize);
    }
    
    @Override
    public int batchInsertList(List<Entity> entities) {
        if (entities == null || entities.isEmpty()) {
            return 0;
        }
        List[] listArray = TruckUtils.averageSplit(entities, defaultBatchFlushDataSize);
        return newBatchExecuteTemplateForList(Arrays.asList(listArray), list -> insertList(list),
                defaultBatchFlushDataSize);
    }
    
    @Override
    public int batchUpdate(List<Entity> entities) {
        return batchUpdate(entities, UpdateModeEnum.DEFAULT);
    }
    
    @Override
    public int batchUpdate(List<Entity> entities, UpdateModeEnum updateMode) {
        return newBatchExecuteTemplateForList(entities, entity -> update(entity, updateMode),
                defaultBatchFlushDataSize);
    }
    
    @Override
    public int batchDelete(List<Entity> entities) {
        return newBatchExecuteTemplateForList(entities, entity -> delete(entity), defaultBatchFlushDataSize);
    }
    
    @Override
    public int batchInsertOrUpdate(List<Entity> entities) {
        return newBatchExecuteTemplateForList(entities, entity -> insertOrUpdate(entity), defaultBatchFlushDataSize);
    }
    
    public <T> int newBatchExecuteTemplateForList(List<T> list, ToIntFunction<T> consumer, int batchFlushDataSize) {
        if (TruckUtils.isEmpty(list)) {
            logger.warn("批处理的list是空的，没有被影响的数据！");
            return 0;
        }
        if (list.size() == 1) {
            return consumer.applyAsInt(list.get(0));
        }
        DefaultBatchExecuteContext batchExecuteContext = new DefaultBatchExecuteContext();
        BatchExecuteTemplateBinder.getTemplate().executeOnBatchMode(batchExecuteContext, (context) -> {
            List<T>[] listArray = TruckUtils.averageSplit(list, batchFlushDataSize);
            int arrayLength = listArray.length;
            for (int i = 0; i < arrayLength; i++) {
                for (T t : listArray[i]) {
                    consumer.applyAsInt(t);
                }
                if (i != arrayLength - 1) {
                    context.doFlush();
                }
            }
            return null;
        });
        return batchExecuteContext.getEffectiveRecordCount();
    }
}
