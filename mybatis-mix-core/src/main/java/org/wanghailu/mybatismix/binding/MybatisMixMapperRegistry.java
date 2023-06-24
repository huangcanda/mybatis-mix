package org.wanghailu.mybatismix.binding;

import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.binding.MapperProxyFactory;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.builder.annotation.MapperAnnotationBuilder;
import org.apache.ibatis.session.Configuration;
import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.util.ReflectUtils;

import java.util.Map;

/**
 * 重写MybatisMixMapperRegistry使用MybatisMixMapperProxyFactory
 *
 * @author cdhuang
 * @date 2023/1/13
 */
public class MybatisMixMapperRegistry extends MapperRegistry {
    
    protected final Configuration config;
    
    Map<Class<?>, MapperProxyFactory<?>> mappers;
    
    public MybatisMixMapperRegistry(Configuration config) {
        super(config);
        this.config = config;
    }
    
    protected Map<Class<?>, MapperProxyFactory<?>> getKnownMappers() {
        if (mappers == null) {
            mappers = (Map<Class<?>, MapperProxyFactory<?>>) ReflectUtils.getFieldValue(this, "knownMappers");
        }
        return mappers;
    }
    
    protected <T> MapperProxyFactory getMapperProxyFactory(Class<T> type) {
        return new MybatisMixMapperProxyFactory<>((MybatisMixConfiguration) config, type);
    }
    
    protected <T> MapperAnnotationBuilder getMapperAnnotationBuilder(Class<T> type) {
        return new MapperAnnotationBuilder(config, type);
    }
    
    @Override
    public <T> void addMapper(Class<T> type) {
        if (type.isInterface()) {
            if (hasMapper(type)) {
                throw new BindingException("Type " + type + " is already known to the MapperRegistry.");
            }
            boolean loadCompleted = false;
            try {
                getKnownMappers().put(type, getMapperProxyFactory(type));
                // It's important that the type is added before the parser is run
                // otherwise the binding may automatically be attempted by the
                // mapper parser. If the type is already known, it won't try.
                MapperAnnotationBuilder parser = getMapperAnnotationBuilder(type);
                parser.parse();
                loadCompleted = true;
            } finally {
                if (!loadCompleted) {
                    getKnownMappers().remove(type);
                }
            }
        }
    }
}
