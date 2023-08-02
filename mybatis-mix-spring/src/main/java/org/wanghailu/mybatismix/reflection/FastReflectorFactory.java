package org.wanghailu.mybatismix.reflection;

import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.Reflector;
import org.apache.ibatis.util.MapUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 重写反射工厂，使用FastReflector
 */
public class FastReflectorFactory extends DefaultReflectorFactory {
    
    private final ConcurrentMap<Class<?>, FastReflector> fastReflectorMap = new ConcurrentHashMap<>();
    
    @Override
    public Reflector findForClass(Class<?> type) {
        if (isClassCacheEnabled()) {
            // synchronized (type) removed see issue #461
            return MapUtil.computeIfAbsent(fastReflectorMap, type, FastReflector::new);
        } else {
            return new FastReflector(type);
        }
    }
}
