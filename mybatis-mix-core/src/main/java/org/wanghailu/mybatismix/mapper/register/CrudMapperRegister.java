package org.wanghailu.mybatismix.mapper.register;

import org.apache.ibatis.session.SqlSession;
import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.annotation.OrderedItem;
import org.wanghailu.mybatismix.mapper.IBaseCrudMapper;
import org.wanghailu.mybatismix.mapper.ICrudMapper;
import org.wanghailu.mybatismix.mapper.IMapper;
import org.wanghailu.mybatismix.mapper.impl.BaseMapperImpl;
import org.wanghailu.mybatismix.mapper.impl.CrudMapperImpl;
import org.wanghailu.mybatismix.provider.CrudMapperSqlProvider;
import org.wanghailu.mybatismix.provider.MapperSqlProvider;

import java.util.Arrays;
import java.util.Collection;

/**
 * crud方法的注册器
 * @author cdhuang
 * @date 2023/1/16
 */
@OrderedItem
public class CrudMapperRegister implements IMapperRegister{
    
    @Override
    public Collection<Class<? extends IMapper>> supportMapper() {
        return Arrays.asList(IBaseCrudMapper.class, ICrudMapper.class);
    }
    
    @Override
    public <Entity> BaseMapperImpl<Entity> getAutoMapper(MybatisMixConfiguration configuration, SqlSession sqlSession,
            Class<Entity> entityClass) {
        return new CrudMapperImpl(configuration,sqlSession,entityClass);
    }
    
    @Override
    public Class<? extends MapperSqlProvider> mapperSqlProviderType() {
        return CrudMapperSqlProvider.class;
    }
    
    @Override
    public MapperSqlProvider getMapperSqlProvider(MybatisMixConfiguration configuration, Class entityClass) {
        return new CrudMapperSqlProvider(entityClass);
    }
    
}
