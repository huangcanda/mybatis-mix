package org.wanghailu.mybatismix.mapper;

import org.apache.ibatis.session.SqlSession;
import org.wanghailu.mybatismix.common.BaseManager;
import org.wanghailu.mybatismix.mapper.register.IMapperMethodRegister;
import org.wanghailu.mybatismix.mapper.register.IMapperRegister;
import org.wanghailu.mybatismix.provider.MapperSqlProvider;
import org.wanghailu.mybatismix.util.SpiExtensionLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cdhuang
 * @date 2023/1/16
 */
public class MapperManager extends BaseManager {
    
    private static Map<Class<? extends IMapper>, IMapperRegister> mapperRegisterMap = new HashMap<>();
    
    private static Map<Class<? extends MapperSqlProvider>, IMapperRegister> mapperSqlProviderMap = new HashMap<>();
    
    private static Map<String, IMapperMethodRegister> mapperMethodRegisterMap;
    
    static {
        SpiExtensionLoader<IMapperRegister> serviceLoader = SpiExtensionLoader.load(IMapperRegister.class);
        for (IMapperRegister register : serviceLoader) {
            for (Class<? extends IMapper> aClass : register.supportMapper()) {
                mapperRegisterMap.putIfAbsent(aClass, register);
            }
            Class<? extends MapperSqlProvider> providerType = register.mapperSqlProviderType();
            if (providerType != null) {
                mapperSqlProviderMap.putIfAbsent(providerType, register);
            }
        }
        mapperMethodRegisterMap = SpiExtensionLoader.loadSpiExtensionMap(IMapperMethodRegister.class);
    }
    
    public static Map<Class<? extends IMapper>, IMapperRegister> getMapperRegisterMap() {
        return mapperRegisterMap;
    }
    
    public static Map<Class<? extends MapperSqlProvider>, IMapperRegister> getProviderMap() {
        return mapperSqlProviderMap;
    }
    
    public static Map<String, IMapperMethodRegister> getMapperMethodRegisterMap() {
        return mapperMethodRegisterMap;
    }
    
    public <T> T getAutoMapper(Class<T> mapperClass, Class<?> entityClass) {
        return (T) mapperRegisterMap
                .get(mapperClass).getAutoMapper(configuration, configuration.getMainSqlSession(), entityClass);
    }
    
    public <T> T getAutoMapper(Class<T> mapperClass, SqlSession sqlSession, Class<?> entityClass) {
        return (T) mapperRegisterMap.get(mapperClass).getAutoMapper(configuration, sqlSession, entityClass);
    }
}
