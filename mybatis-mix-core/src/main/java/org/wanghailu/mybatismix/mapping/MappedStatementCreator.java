package org.wanghailu.mybatismix.mapping;

import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.builder.IncompleteElementException;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMap;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.reflection.TypeParameterResolver;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.util.PrivateStringUtils;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author cdhuang
 * @date 2023/3/31
 */
public class MappedStatementCreator {
    
    protected MybatisMixConfiguration configuration;
    
    public MappedStatementCreator(MybatisMixConfiguration configuration) {
        this.configuration = configuration;
    }
    
    public Consumer<MappedStatement.Builder> setReturnType(String statementId, Class resultType) {
        ResultMap inlineResultMap = new ResultMap.Builder(configuration, statementId + "-Inline", resultType,
                new ArrayList<>(), null).build();
        return (x) -> x.resultMaps(Arrays.asList(inlineResultMap));
    }
    
    public Consumer<MappedStatement.Builder> setParameterMap(String statementId, Class paramType) {
        ParameterMap parameterMap = new ParameterMap.Builder(configuration, statementId + "-Inline", paramType,
                new ArrayList<>()).build();
        return (x) -> x.parameterMap(parameterMap);
    }
    
    public MappedStatement createMappedStatement(String statementId, SqlSource sqlSource,
            SqlCommandType sqlCommandType, Consumer<MappedStatement.Builder>... builderConsumer) {
        
        MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration, statementId, sqlSource,
                sqlCommandType).resource(statementId).fetchSize(null).timeout(null)
                .statementType(StatementType.PREPARED).keyGenerator(NoKeyGenerator.INSTANCE).keyProperty(null)
                .keyColumn(null).databaseId(null).lang(configuration.getLanguageDriver(null)).resultOrdered(false)
                .flushCacheRequired(sqlCommandType != SqlCommandType.SELECT).useCache(sqlCommandType == SqlCommandType.SELECT).cache(null);
        if (builderConsumer != null) {
            for (Consumer<MappedStatement.Builder> consumer : builderConsumer) {
                consumer.accept(statementBuilder);
            }
        }
        return statementBuilder.build();
    }

    /**
     * 根据方法进行创建一个MappedStatement，会处理方法的相关注解
     * @param type
     * @param method
     * @param sqlSource
     * @param sqlCommandType
     * @param builderConsumer
     * @return
     */
    public MappedStatement createMappedStatement(Class type, Method method, SqlSource sqlSource,
                                                 SqlCommandType sqlCommandType, Consumer<MappedStatement.Builder>... builderConsumer) {
        String mappedStatementId = type.getName() + "." + method.getName();
        LanguageDriver languageDriver = getLanguageDriver(method);
        final Options options = method.getAnnotation(Options.class);
        final KeyGenerator keyGenerator;
        String keyProperty = null;
        String keyColumn = null;
        if (SqlCommandType.INSERT.equals(sqlCommandType) || SqlCommandType.UPDATE.equals(sqlCommandType)) {
            SelectKey selectKey = method.getAnnotation(SelectKey.class);
            if (selectKey != null) {
                keyGenerator = handleSelectKeyAnnotation(selectKey, mappedStatementId, getParameterType(method),
                        languageDriver);
                keyProperty = selectKey.keyProperty();
            } else if (options == null) {
                keyGenerator = configuration.isUseGeneratedKeys() ? Jdbc3KeyGenerator.INSTANCE : NoKeyGenerator.INSTANCE;
            } else {
                keyGenerator = options.useGeneratedKeys() ? Jdbc3KeyGenerator.INSTANCE : NoKeyGenerator.INSTANCE;
                keyProperty = options.keyProperty();
                keyColumn = options.keyColumn();
            }
        } else {
            keyGenerator = NoKeyGenerator.INSTANCE;
        }
        Integer fetchSize = null;
        Integer timeout = null;
        StatementType statementType = StatementType.PREPARED;
        ResultSetType resultSetType = configuration.getDefaultResultSetType();
        boolean isSelect = sqlCommandType == SqlCommandType.SELECT;
        boolean flushCache = !isSelect;
        boolean useCache = isSelect;
        if (options != null) {
            if (Options.FlushCachePolicy.TRUE.equals(options.flushCache())) {
                flushCache = true;
            } else if (Options.FlushCachePolicy.FALSE.equals(options.flushCache())) {
                flushCache = false;
            }
            useCache = options.useCache();
            fetchSize = options.fetchSize() > -1 || options.fetchSize() == Integer.MIN_VALUE ? options.fetchSize() : null;
            timeout = options.timeout() > -1 ? options.timeout() : null;
            statementType = options.statementType();
            if (options.resultSetType() != ResultSetType.DEFAULT) {
                resultSetType = options.resultSetType();
            }
        }
        String resultMapId = null;
        if (isSelect) {
            org.apache.ibatis.annotations.ResultMap resultMapAnnotation = method.getAnnotation(org.apache.ibatis.annotations.ResultMap.class);
            if (resultMapAnnotation != null) {
                resultMapId = String.join(",", resultMapAnnotation.value());
            } else {
                resultMapId = generateResultMapName(type,method);
            }
        }
        MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration, mappedStatementId, sqlSource,
                sqlCommandType).resource(mappedStatementId).fetchSize(fetchSize).timeout(timeout)
                .statementType(statementType).keyGenerator(keyGenerator).keyProperty(keyProperty)
                .keyColumn(keyColumn).databaseId(null).lang(languageDriver).resultOrdered(false).resultSetType(resultSetType)
                .flushCacheRequired(flushCache).useCache(useCache).resultMaps(getStatementResultMaps(resultMapId, getReturnType(method,type), mappedStatementId));
        if (builderConsumer != null) {
            for (Consumer<MappedStatement.Builder> consumer : builderConsumer) {
                consumer.accept(statementBuilder);
            }
        }
        return statementBuilder.build();
    }
    
    /**
     * 根据现有的 oldMappedStatement 创建一个新的MappedStatement
     *
     * @param oldMappedStatement
     * @param function
     * @return
     */
    protected MappedStatement newMappedStatement(MappedStatement oldMappedStatement, BaseMappedStatementSupplier function) {
        MappedStatement.Builder builder = new MappedStatement.Builder(oldMappedStatement.getConfiguration(),
                function.getId(), function.getSqlSource(), function.getSqlCommandType());
        builder.resource(oldMappedStatement.getResource());
        builder.fetchSize(oldMappedStatement.getFetchSize());
        builder.statementType(oldMappedStatement.getStatementType());
        builder.keyGenerator(oldMappedStatement.getKeyGenerator());
        if (oldMappedStatement.getKeyProperties() != null) {
            builder.keyProperty(PrivateStringUtils.join(Arrays.asList(oldMappedStatement.getKeyProperties()), ","));
            
        }
        builder.timeout(oldMappedStatement.getTimeout());
        builder.parameterMap(oldMappedStatement.getParameterMap());
        builder.resultMaps(oldMappedStatement.getResultMaps());
        builder.resultSetType(oldMappedStatement.getResultSetType());
        builder.cache(oldMappedStatement.getCache());
        builder.flushCacheRequired(oldMappedStatement.isFlushCacheRequired());
        builder.useCache(oldMappedStatement.isUseCache());
        function.apply(builder);
        return builder.build();
    }
    
    /**
     * 根据返回值类型获得ms
     *
     * @param oldMappedStatement
     * @param function
     * @return
     */
    public MappedStatement getMappedStatement(MappedStatement oldMappedStatement, BaseMappedStatementSupplier function) {
        String newMsId = function.getId();
        Configuration configuration = oldMappedStatement.getConfiguration();
        MappedStatement result = getMappedStatementNoThrowExction(configuration, newMsId);
        if (result == null) {
            synchronized (configuration) {
                result = getMappedStatementNoThrowExction(configuration, newMsId);
                if (result == null) {
                    result = newMappedStatement(oldMappedStatement, function);
                    configuration.addMappedStatement(result);
                }
            }
        }
        return result;
    }
    
    /**
     * 根据msId获得Ms对象，不抛异常
     *
     * @param configuration
     * @param msId
     * @return
     */
    protected MappedStatement getMappedStatementNoThrowExction(Configuration configuration, String msId) {
        MappedStatement result = null;
        try {
            result = configuration.getMappedStatement(msId);
        } catch (RuntimeException e) {
        }
        return result;
    }

    private LanguageDriver getLanguageDriver(Method method) {
        Lang lang = method.getAnnotation(Lang.class);
        Class<? extends LanguageDriver> langClass = null;
        if (lang != null) {
            langClass = lang.value();
        }
        return configuration.getLanguageDriver(langClass);
    }

    private String generateResultMapName(Class type, Method method) {
        Results results = method.getAnnotation(Results.class);
        if (results != null && !results.id().isEmpty()) {
            return type.getName() + "." + results.id();
        }
        return null;
    }

    private Class<?> getParameterType(Method method) {
        Class<?> parameterType = null;
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (Class<?> currentParameterType : parameterTypes) {
            if (!RowBounds.class.isAssignableFrom(currentParameterType)
                    && !ResultHandler.class.isAssignableFrom(currentParameterType)) {
                if (parameterType == null) {
                    parameterType = currentParameterType;
                } else {
                    parameterType = MapperMethod.ParamMap.class;
                }
            }
        }
        return parameterType;
    }

    private KeyGenerator handleSelectKeyAnnotation(SelectKey selectKeyAnnotation, String baseStatementId,
                                                   Class<?> parameterTypeClass, LanguageDriver languageDriver) {
        String id = baseStatementId + SelectKeyGenerator.SELECT_KEY_SUFFIX;
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, String.join(" ", selectKeyAnnotation.statement()).trim(), parameterTypeClass);
        SqlCommandType sqlCommandType = SqlCommandType.SELECT;
        Class<?> resultTypeClass = selectKeyAnnotation.resultType();
        StatementType statementType = selectKeyAnnotation.statementType();
        String keyProperty = selectKeyAnnotation.keyProperty();
        String keyColumn = selectKeyAnnotation.keyColumn();
        String databaseId = selectKeyAnnotation.databaseId().isEmpty() ? null : selectKeyAnnotation.databaseId();
        Consumer<MappedStatement.Builder> consumer = builder -> {
            builder.useCache(false);
            builder.statementType(statementType);
            builder.keyProperty(keyProperty);
            builder.keyColumn(keyColumn);
            builder.databaseId(databaseId);
            builder.dirtySelect(false);
            builder.lang(languageDriver);
        };
        MappedStatement keyStatement = createMappedStatement(id,sqlSource,sqlCommandType,consumer,setReturnType(id,resultTypeClass));
       SelectKeyGenerator answer = new SelectKeyGenerator(keyStatement, selectKeyAnnotation.before());
        configuration.addKeyGenerator(id, answer);
        return answer;
    }

    private static Class<?> getReturnType(Method method, Class<?> type) {
        Class<?> returnType = method.getReturnType();
        Type resolvedReturnType = TypeParameterResolver.resolveReturnType(method, type);
        if (resolvedReturnType instanceof Class) {
            returnType = (Class<?>) resolvedReturnType;
            if (returnType.isArray()) {
                returnType = returnType.getComponentType();
            }
            // gcode issue #508
            if (void.class.equals(returnType)) {
                ResultType rt = method.getAnnotation(ResultType.class);
                if (rt != null) {
                    returnType = rt.value();
                }
            }
        } else if (resolvedReturnType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) resolvedReturnType;
            Class<?> rawType = (Class<?>) parameterizedType.getRawType();
            if (Collection.class.isAssignableFrom(rawType) || Cursor.class.isAssignableFrom(rawType)) {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments != null && actualTypeArguments.length == 1) {
                    Type returnTypeParameter = actualTypeArguments[0];
                    if (returnTypeParameter instanceof Class<?>) {
                        returnType = (Class<?>) returnTypeParameter;
                    } else if (returnTypeParameter instanceof ParameterizedType) {
                        // (gcode issue #443) actual type can be a also a parameterized type
                        returnType = (Class<?>) ((ParameterizedType) returnTypeParameter).getRawType();
                    } else if (returnTypeParameter instanceof GenericArrayType) {
                        Class<?> componentType = (Class<?>) ((GenericArrayType) returnTypeParameter).getGenericComponentType();
                        // (gcode issue #525) support List<byte[]>
                        returnType = Array.newInstance(componentType, 0).getClass();
                    }
                }
            } else if (method.isAnnotationPresent(MapKey.class) && Map.class.isAssignableFrom(rawType)) {
                // (gcode issue 504) Do not look into Maps if there is not MapKey annotation
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments != null && actualTypeArguments.length == 2) {
                    Type returnTypeParameter = actualTypeArguments[1];
                    if (returnTypeParameter instanceof Class<?>) {
                        returnType = (Class<?>) returnTypeParameter;
                    } else if (returnTypeParameter instanceof ParameterizedType) {
                        // (gcode issue 443) actual type can be a also a parameterized type
                        returnType = (Class<?>) ((ParameterizedType) returnTypeParameter).getRawType();
                    }
                }
            } else if (Optional.class.equals(rawType)) {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                Type returnTypeParameter = actualTypeArguments[0];
                if (returnTypeParameter instanceof Class<?>) {
                    returnType = (Class<?>) returnTypeParameter;
                }
            }
        }

        return returnType;
    }

    private List<ResultMap> getStatementResultMaps(String resultMap, Class<?> resultType, String statementId) {
        List<ResultMap> resultMaps = new ArrayList<>();
        if (resultMap != null) {
            String[] resultMapNames = resultMap.split(",");
            for (String resultMapName : resultMapNames) {
                try {
                    resultMaps.add(configuration.getResultMap(resultMapName.trim()));
                } catch (IllegalArgumentException e) {
                    throw new IncompleteElementException(
                            "Could not find result map '" + resultMapName + "' referenced from '" + statementId + "'", e);
                }
            }
        } else if (resultType != null) {
            ResultMap inlineResultMap = new ResultMap.Builder(configuration, statementId + "-Inline", resultType,
                    new ArrayList<>(), null).build();
            resultMaps.add(inlineResultMap);
        }
        return resultMaps;
    }
}
