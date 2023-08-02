package org.wanghailu.mybatismix.mapper.jpaquery;

import org.apache.ibatis.mapping.MappedStatement;
import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.annotation.JpaQuery;
import org.wanghailu.mybatismix.exception.MybatisMixException;
import org.wanghailu.mybatismix.mapper.IBaseCrudMapper;
import org.wanghailu.mybatismix.mapper.IMapperMethodInvoker;
import org.wanghailu.mybatismix.mapper.register.IMapperMethodRegister;
import org.wanghailu.mybatismix.mapping.MappedStatementCreator;
import org.wanghailu.mybatismix.mapping.MappedStatementManager;
import org.wanghailu.mybatismix.util.TruckUtils;

import javax.persistence.Entity;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Jpa语法支持的注册器
 * @author cdhuang
 */
public class JpaQueryMapperMethodRegister implements IMapperMethodRegister {
    
    @Override
    public IMapperMethodInvoker supportMethod(Class mapperClass, Method method, MybatisMixConfiguration configuration) {
        JpaQuery jpaQuery = method.getDeclaredAnnotation(JpaQuery.class);
        if (jpaQuery == null) {
            return null;
        }
        Class entityClass = jpaQuery.value();
        if (entityClass == Object.class) {
            Type genericType = TruckUtils.getMapperGenericType(mapperClass, IBaseCrudMapper.class);
            if (genericType instanceof Class) {
                entityClass = (Class) genericType;
            }
        }
        if (entityClass == Object.class) {
            throw new MybatisMixException("无法获取JpaQuery对应的实体类，请在@JpaQuery注解上添加实体类信息！");
        }
        if (!entityClass.isAnnotationPresent(Entity.class)) {
            throw new MybatisMixException("使用JpaQuery的实体必须有@Entity注解！");
        }
        PartTree partTree = new PartTree(method.getName(), entityClass);
        JpaQuerySqlSource sqlSource = new JpaQuerySqlSource(partTree, configuration);
        MappedStatementManager mappedStatementManager = configuration.getManager(MappedStatementManager.class);
        MappedStatementCreator mappedStatementCreator = mappedStatementManager.getEntityMappedStatementCreator();
        MappedStatement mappedStatement = mappedStatementCreator
                .createMappedStatement(mapperClass, method, sqlSource, partTree.getSqlCommandType());
        sqlSource.setMappedStatement(mappedStatement);
        sqlSource.setLanguageDriver(mappedStatement.getLang());
        configuration.addMappedStatement(mappedStatement);
        return null;
    }
}
