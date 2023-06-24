package org.wanghailu.mybatismix.mapper;

import org.wanghailu.mybatismix.example.BaseDeleteExample;
import org.wanghailu.mybatismix.example.BaseQueryExample;
import org.wanghailu.mybatismix.example.BaseUpdateExample;

import java.util.List;

public interface IExampleMapper<Entity> extends IMapper{

    List<Entity> selectByExample(BaseQueryExample<Entity,? extends BaseQueryExample> example);
    
    Entity selectOneByExample(BaseQueryExample<Entity,? extends BaseQueryExample> example);

    int countByExample(BaseQueryExample<Entity,? extends BaseQueryExample> example);

    int deleteByExample(BaseDeleteExample<Entity> example);

    int updateByExample(BaseUpdateExample example);
}
