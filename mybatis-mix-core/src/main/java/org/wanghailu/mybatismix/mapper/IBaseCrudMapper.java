package org.wanghailu.mybatismix.mapper;

import org.wanghailu.mybatismix.constant.UpdateModeEnum;

import java.io.Serializable;
import java.util.List;

public interface IBaseCrudMapper<Entity> extends IMapper {
    
    int insert(Entity entity);
    
    int insertList(List<Entity> entities);
    
    List<Entity> selectAll();
    
    Entity selectById(Serializable primaryKeyValue);
    
    List<Entity> selectListByIds(Serializable... primaryKeyValues);
    
    int update(Entity entity);

    int update(Entity entity, UpdateModeEnum updateMode);
    
    int delete(Entity entity);
    
    int insertOrUpdate(Entity entity);
}
