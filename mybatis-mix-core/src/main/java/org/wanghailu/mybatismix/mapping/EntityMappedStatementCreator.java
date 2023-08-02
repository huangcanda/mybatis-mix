package org.wanghailu.mybatismix.mapping;

import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.annotation.NamespaceAlias;
import org.wanghailu.mybatismix.annotation.UseGeneratedKey;
import org.wanghailu.mybatismix.exception.MybatisMixException;
import org.wanghailu.mybatismix.mapper.MapperManager;
import org.wanghailu.mybatismix.provider.ExtProviderSqlSource;
import org.wanghailu.mybatismix.provider.MapperSqlProvider;
import org.wanghailu.mybatismix.util.PrivateStringUtils;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.wanghailu.mybatismix.constant.ConfigurationKeyConstant.mapperShortNamespace;

/**
 * Entity的CURD对应的MappedStatement创建器
 */
public class EntityMappedStatementCreator extends MappedStatementCreator {
    
    Logger logger = LoggerFactory.getLogger(EntityMappedStatementCreator.class);
    
    private static Map<Class, String> classNamespaceMap = new HashMap<>();
    
    public static String commonPrefix = null;
    
    public static String shortPrefix = null;
    
    public static EntityMappedStatementCreator instance;
    
    /**
     * 定义StatementId的生成规则
     *
     * @param clazz
     * @return
     */
    public static String getNamespaceKey(Class clazz) {
        String namespaceKey = classNamespaceMap.get(clazz);
        if (namespaceKey == null) {
            synchronized (classNamespaceMap) {
                namespaceKey = classNamespaceMap.get(clazz);
                if (namespaceKey == null) {
                    namespaceKey = instance.initNamespaceMapAndEntityStatement(clazz);
                }
            }
        }
        return namespaceKey;
    }
    
    
    public EntityMappedStatementCreator(MybatisMixConfiguration configuration) {
        super(configuration);
        instance = this;
    }
    
    public void init(Collection<Class<?>> classSet) {
        List<Class<?>> list = classSet.stream().sorted(Comparator.comparing(Class::getName))
                .collect(Collectors.toList());
        synchronized (classNamespaceMap) {
            if (configuration.getBoolProperty(mapperShortNamespace, true)) {
                initCommonFactorKey(list);
            }
            for (Class<?> entityClass : list) {
                initNamespaceMapAndEntityStatement(entityClass);
            }
        }
    }
    
    /**
     * 根据转入的entity的class，生成对应的CURD的MappedStatement
     *
     * @param entityClass
     */
    public void loadEntityMappedStatement(Class<?> entityClass) {
        for (Class<? extends MapperSqlProvider> providerClass : MapperManager.getProviderMap().keySet()) {
            createMappedStatementBySqlProvider(entityClass, providerClass);
        }
    }
    
    /**
     * 给SqlProvider的所有public修饰且参数个数为1个的方法，都生成一个MappedStatement
     *
     * @param entityClass
     * @param sqlProviderClass
     */
    private void createMappedStatementBySqlProvider(Class<?> entityClass, Class sqlProviderClass) {
        for (Method providerMethod : sqlProviderClass.getDeclaredMethods()) {
            if (Modifier.isPublic(providerMethod.getModifiers()) && providerMethod.getParameterCount() == 1) {
                String methodName = providerMethod.getName();
                SqlCommandType sqlCommandType;
                if (methodName.startsWith("insert")) {
                    sqlCommandType = SqlCommandType.INSERT;
                } else if (methodName.startsWith("update")) {
                    sqlCommandType = SqlCommandType.UPDATE;
                } else if (methodName.startsWith("delete")) {
                    sqlCommandType = SqlCommandType.DELETE;
                } else if (methodName.startsWith("select") || methodName.startsWith("count")) {
                    sqlCommandType = SqlCommandType.SELECT;
                } else {
                    throw new MybatisMixException("不合理的方法名" + methodName);
                }
                createMappedStatement(entityClass, providerMethod, sqlCommandType);
            }
        }
    }
    
    /**
     * 给SqlProvider的一个方法，生成一个MappedStatement
     *
     * @param entityClass
     * @param providerMethod
     * @param sqlCommandType
     */
    private void createMappedStatement(Class<?> entityClass, Method providerMethod, SqlCommandType sqlCommandType) {
        String statementId = getNamespaceKey(entityClass) + "." + providerMethod.getName();
        Class paramType = providerMethod.getParameterTypes()[0];
        SqlSource sqlSource = new ExtProviderSqlSource(providerMethod, entityClass, configuration);
        MappedStatement mappedStatement;
        if (SqlCommandType.SELECT == sqlCommandType) {
            mappedStatement = createMappedStatement(statementId, sqlSource, sqlCommandType,
                    setParameterMap(statementId, paramType), setReturnType(statementId,
                            providerMethod.getName().startsWith("count") ? Integer.class : entityClass));
        } else if (SqlCommandType.INSERT.equals(sqlCommandType) && entityClass
                .isAnnotationPresent(UseGeneratedKey.class)) {
            mappedStatement = createMappedStatement(statementId, sqlSource, sqlCommandType,
                    setParameterMap(statementId, paramType), setKeyGenerator(Jdbc3KeyGenerator.INSTANCE));
        } else {
            mappedStatement = createMappedStatement(statementId, sqlSource, sqlCommandType,
                    setParameterMap(statementId, paramType));
        }
        configuration.addMappedStatement(mappedStatement);
        logger.trace("初始化MappedStatement：" + statementId);
    }
    
    protected Consumer<MappedStatement.Builder> setKeyGenerator(KeyGenerator keyGenerator) {
        return (x) -> x.keyGenerator(keyGenerator);
    }
    
    
    protected String initNamespaceMapAndEntityStatement(Class<?> entityClass) {
        checkEntityClass(entityClass);
        String namespace = getNamespace(entityClass);
        if (commonPrefix != null && namespace.startsWith(commonPrefix)) {
            namespace = PrivateStringUtils.replaceOnce(namespace, commonPrefix, shortPrefix);
        }
        namespace = namespace + ".AutoMapper";
        String old = classNamespaceMap.put(entityClass, namespace);
        if (old != null) {
            throw new MybatisMixException("mybatis的namespace冲突，无法生成对应的mappedStatement");
        }
        loadEntityMappedStatement(entityClass);
        return namespace;
    }
    
    public static void checkEntityClass(Class clazz) {
        if (clazz.getAnnotation(Entity.class) == null) {
            throw new MybatisMixException("没有Entity注解无法进行增删改查操作！");
        }
        if (clazz.getAnnotation(Table.class) == null) {
            throw new MybatisMixException("没有Table注解无法进行增删改查操作！");
        }
    }
    
    /**
     * 找到最优公共包前缀
     */
    protected void initCommonFactorKey(List<Class<?>> list) {
        Map<String, String[]> packagePathMap = new LinkedHashMap<>();
        for (Class<?> aClass : list) {
            String namespace = getNamespace(aClass);
            String[] packagePath = getNamespace(aClass).split("\\.");
            packagePathMap.put(namespace, packagePath);
        }
        int allSize = list.size();
        String factorKey = null;
        int index = 0;
        while (true) {
            Map<String, Integer> countMap = new HashMap<>(packagePathMap.size());
            for (Map.Entry<String, String[]> entry : packagePathMap.entrySet()) {
                String[] strings = entry.getValue();
                if (index + 2 > strings.length) {
                    continue;
                }
                String nextFactorKey = factorKey == null ? strings[index] : (factorKey + "." + strings[index]);
                if (entry.getKey().startsWith(nextFactorKey)) {
                    countMap.put(nextFactorKey, countMap.getOrDefault(nextFactorKey, 0) + 1);
                }
            }
            boolean findFactor = false;
            for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
                if (entry.getValue() * 100 / allSize > 50) {
                    findFactor = true;
                    factorKey = entry.getKey();
                    continue;
                }
            }
            if (findFactor) {
                index++;
                countMap.clear();
            } else {
                break;
            }
        }
        commonPrefix = factorKey;
        if (commonPrefix != null) {
            shortPrefix = Arrays.stream(commonPrefix.split("\\.")).map(x -> x.substring(0, 1).toUpperCase())
                    .collect(Collectors.joining(""));
        }
    }
    
    private static String getNamespace(Class<?> aClass) {
        NamespaceAlias alias = aClass.getAnnotation(NamespaceAlias.class);
        if (alias != null) {
            return alias.value();
        } else {
            return aClass.getName();
        }
    }
}
