package org.wanghailu.mybatismix.keygenerator;

import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.support.NamedSpiExtension;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 主键生成器基类
 * @author cdhuang
 * @date 2023/4/17
 */
public abstract class BaseKeyGenerator<TYPE> implements NamedSpiExtension {
    
    protected MybatisMixConfiguration configuration;
    
    protected Class<TYPE> supportType;
    
    public BaseKeyGenerator() {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        supportType = (Class<TYPE>) actualTypeArguments[0];
    }
    
    public boolean supportKeyType(Class keyType,Class entityType) {
        return supportType.isAssignableFrom(keyType);
    }
    
    /**
     * 生成key动作
     * @param entityType
     * @param fieldName
     * @param keyType
     * @return
     */
    public abstract TYPE generateKey(Class entityType, String fieldName, Class keyType);
    
    public void setConfiguration(MybatisMixConfiguration configuration) {
        this.configuration = configuration;
    }
}
