package org.wanghailu.mybatismix.provider;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.common.BaseEntitySqlSource;
import org.wanghailu.mybatismix.mapper.MapperManager;
import org.wanghailu.mybatismix.util.ExceptionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author cdhuang
 * @date 2022/6/30
 */
public class ExtProviderSqlSource extends BaseEntitySqlSource {
    
    private Method providerMethod;
    
    private MapperSqlProvider provider;
    
    private MybatisMixConfiguration configuration;
    
    public ExtProviderSqlSource(Method providerMethod, Class<?> entityClass, MybatisMixConfiguration configuration) {
        super(entityClass);
        this.providerMethod = providerMethod;
        if (!Modifier.isStatic(providerMethod.getModifiers())) {
            provider = MapperManager.getProviderMap().get(providerMethod.getDeclaringClass())
                    .getMapperSqlProvider(configuration, entityClass);
        }
        this.configuration = configuration;
    }
    
    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        try {
            CharSequence sql = (CharSequence) providerMethod.invoke(provider, parameterObject);
            String str = sql != null ? sql.toString() : null;
            Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
            SqlSource sqlSource = configuration.getLanguageDriver(null)
                    .createSqlSource(configuration, str, parameterType);
            return sqlSource.getBoundSql(parameterObject);
        } catch (Exception e) {
            ExceptionUtils.throwException(e);
            return null;
        }
    }
    
    public Method getProviderMethod() {
        return providerMethod;
    }
    
    public MapperSqlProvider getProvider() {
        return provider;
    }
    
    @Override
    public String getSqlSourceName() {
        return providerMethod.getName();
    }
}
