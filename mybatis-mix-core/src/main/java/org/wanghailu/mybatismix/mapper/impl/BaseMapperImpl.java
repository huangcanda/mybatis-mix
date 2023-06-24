package org.wanghailu.mybatismix.mapper.impl;

import org.apache.ibatis.session.SqlSession;
import org.wanghailu.mybatismix.MybatisMixConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cdhuang
 * @date 2023/1/16
 */
public abstract class BaseMapperImpl<Entity> {
    
    protected MybatisMixConfiguration configuration;
    
    protected SqlSession sqlSession;
    
    protected Class<Entity> entityClass;
    
    public BaseMapperImpl(MybatisMixConfiguration configuration,SqlSession sqlSession, Class<Entity> entityClass) {
        this.configuration = configuration;
        this.sqlSession = sqlSession;
        this.entityClass = entityClass;
    }
    
    public SqlSession getSqlSession() {
        return sqlSession;
    }
    
    public Class<Entity> getEntityClass() {
        return entityClass;
    }
    
    protected Map<String, Object> buildMap(Object... args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("参数个数必须为偶数，当前参数个数为" + args.length);
        }
        Map<String, Object> map = new HashMap<>(args.length);
        for (int index = 0; index < args.length; index = index + 2) {
            String key = (String) args[index];
            Object value = args[index + 1];
            map.put(key, value);
        }
        return map;
    }
}
