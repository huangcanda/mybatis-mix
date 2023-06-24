package org.wanghailu.mybatismix.binding;

import org.apache.ibatis.binding.MapperProxy;
import org.apache.ibatis.binding.MapperProxyFactory;
import org.apache.ibatis.session.SqlSession;
import org.wanghailu.mybatismix.MybatisMixConfiguration;

/**
 * 重写MapperProxyFactory使用MybatisMixMapperProxy
 *
 * @author cdhuang
 * @date 2023/1/13
 */
public class MybatisMixMapperProxyFactory<T> extends MapperProxyFactory<T> {
    
    private MybatisMixConfiguration configuration;
    
    public MybatisMixMapperProxyFactory(MybatisMixConfiguration configuration, Class mapperInterface) {
        super(mapperInterface);
        this.configuration = configuration;
    }
    
    @Override
    public T newInstance(SqlSession sqlSession) {
        final MapperProxy<T> mapperProxy = getMapperProxy(sqlSession);
        return newInstance(mapperProxy);
    }
    
    protected MapperProxy<T> getMapperProxy(SqlSession sqlSession) {
        return new MybatisMixMapperProxy(configuration, sqlSession, getMapperInterface(), getMethodCache());
    }
}
