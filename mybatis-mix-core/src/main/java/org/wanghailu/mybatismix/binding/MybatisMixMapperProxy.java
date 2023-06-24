package org.wanghailu.mybatismix.binding;

import org.apache.ibatis.binding.MapperProxy;
import org.apache.ibatis.session.SqlSession;
import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.exception.MybatisMixException;
import org.wanghailu.mybatismix.mapper.IMapper;
import org.wanghailu.mybatismix.mapper.IMapperMethodInvoker;
import org.wanghailu.mybatismix.mapper.MapperManager;
import org.wanghailu.mybatismix.mapper.register.IMapperMethodRegister;
import org.wanghailu.mybatismix.mapper.register.IMapperRegister;
import org.wanghailu.mybatismix.mapping.EntityMappedStatementCreator;
import org.wanghailu.mybatismix.util.ExceptionUtils;
import org.wanghailu.mybatismix.util.TruckUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * 重写MapperProxy支持AutoMapper
 *
 * @author cdhuang
 * @date 2023/1/13
 */
public class MybatisMixMapperProxy<T> extends MapperProxy<T> {
    
    protected Map<Class<?>, Object> autoMapperMap;
    
    protected Map<Method, IMapperMethodInvoker> mapperMethodInvokerMap;
    
    public MybatisMixMapperProxy(MybatisMixConfiguration configuration, SqlSession sqlSession, Class mapperInterface,
            Map methodCache) {
        super(sqlSession, mapperInterface, methodCache);
        initAutoMapperMap(configuration, sqlSession, mapperInterface);
        initAutoMapperMethod(configuration, mapperInterface);
    }
    
    /**
     * 初始化autoMapperMap
     *
     * @param sqlSession
     * @param mapperInterface
     */
    protected void initAutoMapperMap(MybatisMixConfiguration configuration, SqlSession sqlSession,
            Class mapperInterface) {
        List<Class> allInterfaces = TruckUtils.getAllInterfaces(mapperInterface);
        for (Class anInterface : new HashSet<>(allInterfaces)) {
            for (Map.Entry<Class<? extends IMapper>, IMapperRegister> entry : MapperManager.getMapperRegisterMap()
                    .entrySet()) {
                if (entry.getKey().getName().equals(anInterface.getName())) {
                    Type entityType = TruckUtils.getMapperGenericType(mapperInterface, anInterface);
                    if (entityType instanceof Class == false) {
                        throw new MybatisMixException("无法确定接口" + anInterface.getSimpleName() + "映射的Entity类型");
                    }
                    Class entityClass = (Class) entityType;
                    EntityMappedStatementCreator.checkEntityClass(entityClass);
                    if (autoMapperMap == null) {
                        autoMapperMap = new HashMap<>(8);
                    }
                    autoMapperMap
                            .put(anInterface, entry.getValue().getAutoMapper(configuration, sqlSession, entityClass));
                }
            }
        }
    }
    
    /**
     * 初始化自动绑定的方法
     *
     * @param configuration
     * @param mapperInterface
     */
    protected void initAutoMapperMethod(MybatisMixConfiguration configuration, Class mapperInterface) {
        Method[] methods = mapperInterface.getMethods();
        for (Method method : methods) {
            if (MapperManager.getMapperRegisterMap().containsKey(method.getDeclaringClass())) {
                continue;
            }
            for (IMapperMethodRegister mapperMethodRegister : MapperManager.getMapperMethodRegisterMap().values()) {
                IMapperMethodInvoker mapperMethodInvoker = mapperMethodRegister
                        .supportMethod(mapperInterface, method, configuration);
                if (mapperMethodInvoker != null) {
                    if (mapperMethodInvokerMap == null) {
                        mapperMethodInvokerMap = new HashMap<>(8);
                    }
                    mapperMethodInvokerMap.put(method, mapperMethodInvoker);
                }
            }
        }
    }
    
    /**
     * 重写方法，autoMapper走对应的autoMapper实现进行处理
     *
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (autoMapperMap != null) {
            Object mapper = autoMapperMap.get(method.getDeclaringClass());
            if (mapper != null) {
                return method.invoke(mapper, args);
            }
        }
        if (mapperMethodInvokerMap != null) {
            IMapperMethodInvoker mapperMethodInvoker = mapperMethodInvokerMap.get(method);
            if (mapperMethodInvoker != null) {
                return mapperMethodInvoker.doMethodInvoke(proxy, method, args, () -> superInvoke(proxy, method, args));
            }
        }
        return superInvoke(proxy, method, args);
    }
    
    public Object superInvoke(Object proxy, Method method, Object[] args) {
        try {
            return super.invoke(proxy, method, args);
        } catch (Throwable throwable) {
            ExceptionUtils.throwException(throwable);
            return null;
        }
    }
    
    
}
