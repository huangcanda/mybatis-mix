package org.wanghailu.mybatismix.mapper.register;

import org.apache.ibatis.session.SqlSession;
import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.annotation.OrderedItem;
import org.wanghailu.mybatismix.mapper.IExampleMapper;
import org.wanghailu.mybatismix.mapper.IMapper;
import org.wanghailu.mybatismix.mapper.impl.BaseMapperImpl;
import org.wanghailu.mybatismix.mapper.impl.ExampleMapperImpl;
import org.wanghailu.mybatismix.provider.ExampleMapperSqlProvider;
import org.wanghailu.mybatismix.provider.MapperSqlProvider;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author cdhuang
 * @date 2023/1/16
 */
@OrderedItem
public class ExampleMapperRegister implements IMapperRegister{
    
    @Override
    public Collection<Class<? extends IMapper>> supportMapper() {
        return Arrays.asList(IExampleMapper.class);
    }
    
    @Override
    public <Entity> BaseMapperImpl<Entity> getAutoMapper(MybatisMixConfiguration configuration, SqlSession sqlSession,
            Class<Entity> entityClass) {
        return new ExampleMapperImpl<>(configuration,sqlSession,entityClass);
    }
    
    @Override
    public Class<? extends MapperSqlProvider> mapperSqlProviderType() {
        return ExampleMapperSqlProvider.class;
    }
    
    private static final ExampleMapperSqlProvider INSTANCE = new ExampleMapperSqlProvider();
    
    @Override
    public MapperSqlProvider getMapperSqlProvider(MybatisMixConfiguration configuration, Class entityClass) {
        return INSTANCE;
    }
    
}
