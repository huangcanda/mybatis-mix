package org.wanghailu.mybatismix.mapper;

import org.wanghailu.mybatismix.constant.UpdateModeEnum;

import java.util.List;

public interface ICrudMapper<Entity> extends IBaseCrudMapper<Entity>{
    
    int batchInsert(List<Entity> entities);
    
    int batchInsertList(List<Entity> entities);
    
    int batchUpdate(List<Entity> entities);

    int batchUpdate(List<Entity> entities, UpdateModeEnum updateMode);
    
    int batchDelete(List<Entity> entities);
    
    int batchInsertOrUpdate(List<Entity> entities);
}
