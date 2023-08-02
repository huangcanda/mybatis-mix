package org.wanghailu.mybatismix.mapper.register;

import org.apache.ibatis.session.SqlSession;
import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.mapper.IMapper;
import org.wanghailu.mybatismix.mapper.impl.BaseMapperImpl;
import org.wanghailu.mybatismix.provider.MapperSqlProvider;
import org.wanghailu.mybatismix.support.NamedSpiExtension;

import java.util.Collection;

public interface IMapperRegister extends NamedSpiExtension {
    
    Collection<Class<? extends IMapper>> supportMapper();
    
    /**
     *
     * @param sqlSession
     * @param entityClass
     * @param <Entity>
     * @return
     */
    <Entity> BaseMapperImpl<Entity> getAutoMapper(MybatisMixConfiguration configuration,SqlSession sqlSession, Class<Entity> entityClass);
    
    /**
     * 定义需要添加的MapperSqlProvider
     * @return
     */
    Class<? extends MapperSqlProvider> mapperSqlProviderType();
    
    /**
     * 定义如何实例化MapperSqlProvider
     * @param configuration
     * @param entityClass
     * @return
     */
    MapperSqlProvider getMapperSqlProvider(MybatisMixConfiguration configuration,Class entityClass);
}
