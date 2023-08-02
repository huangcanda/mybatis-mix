package org.wanghailu.mybatismix.keygenerator;

import org.wanghailu.mybatismix.common.BaseManager;
import org.wanghailu.mybatismix.exception.MybatisMixException;
import org.wanghailu.mybatismix.support.EntityPropertyDescriptor;
import org.wanghailu.mybatismix.support.TwoTuple;
import org.wanghailu.mybatismix.util.EntityUtils;
import org.wanghailu.mybatismix.util.SpiExtensionLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 主键生成管理
 * @author cdhuang
 * @date 2023/4/17
 */
public class KeyGeneratorManager extends BaseManager {
    
    protected final List<BaseKeyGenerator> keyGeneratorList = new ArrayList<>();
    
    protected final Map<TwoTuple<Class, Class>, BaseKeyGenerator> keyGeneratorMap = new HashMap<>();
    
    @Override
    public void initAfterSetProperties() {
        SpiExtensionLoader<BaseKeyGenerator> serviceLoader = SpiExtensionLoader.load(BaseKeyGenerator.class);
        for (BaseKeyGenerator keyGenerator : serviceLoader) {
            keyGenerator.setConfiguration(configuration);
            keyGeneratorList.add(keyGenerator);
        }
    }
    
    protected BaseKeyGenerator getKeyGenerator(Class keyType, Class entityType) {
        TwoTuple<Class, Class> key = TwoTuple.of(keyType, entityType);
        BaseKeyGenerator keyGenerator = keyGeneratorMap.get(key);
        if (keyGenerator == null) {
            synchronized (keyGeneratorMap) {
                keyGenerator = keyGeneratorMap.get(key);
                if (keyGenerator == null) {
                    keyGenerator = findKeyGenerator(keyType, entityType);
                    keyGeneratorMap.put(key, keyGenerator);
                }
            }
        }
        return keyGenerator;
    }
    
    protected BaseKeyGenerator findKeyGenerator(Class keyType, Class entityType) {
        for (BaseKeyGenerator baseKeyGenerator : keyGeneratorList) {
            if (baseKeyGenerator.supportKeyType(keyType, entityType)) {
                return baseKeyGenerator;
            }
        }
        throw new MybatisMixException("不支持生成主键！主键类型：" + keyType.getName() + ",实体类型：" + entityType.getName());
    }
    
    @Override
    public void initAfterMybatisInit() {
        super.initAfterMybatisInit();
    }
    
    public Object generateKey(Object entity) {
        return generateKey(entity, EntityUtils.getPrimaryKeyFieldName(entity.getClass()));
    }
    
    public Object generateKey(Object entity, String fieldName) {
        Class entityType = entity.getClass();
        EntityPropertyDescriptor entityPropertyDescriptor = EntityUtils
                .getPropertyDescriptorByFieldName(entityType, fieldName);
        Class keyType = entityPropertyDescriptor.getGetMethod().getReturnType();
        BaseKeyGenerator keyGenerator = getKeyGenerator(keyType, entityType);
        return keyGenerator.generateKey(entityType, fieldName, keyType);
    }
}
