package org.wanghailu.mybatismix;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.InterceptorChain;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.Transaction;
import org.wanghailu.mybatismix.binding.MybatisMixMapperRegistry;
import org.wanghailu.mybatismix.common.BaseManager;
import org.wanghailu.mybatismix.constant.ConfigurationKeyConstant;
import org.wanghailu.mybatismix.constant.ConfigurationStateConstant;
import org.wanghailu.mybatismix.executor.ExecutorManager;
import org.wanghailu.mybatismix.executor.statement.LogFullSqlStatementHandler;
import org.wanghailu.mybatismix.hotdeploy.MapperHotDeployManager;
import org.wanghailu.mybatismix.mapping.MappedStatementManager;
import org.wanghailu.mybatismix.resultset.ExtResultSetHandler;
import org.wanghailu.mybatismix.support.PropertiesHelper;
import org.wanghailu.mybatismix.util.MybatisContext;
import org.wanghailu.mybatismix.util.ReflectUtils;
import org.wanghailu.mybatismix.util.SpiExtensionLoader;
import org.wanghailu.mybatismix.util.TruckUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 重写Configuration,实现MybatisMix逻辑的管理 Created by cd_huang on 2019/4/30.
 */
public class MybatisMixConfiguration extends Configuration implements PropertiesHelper {
    
    static {
        ReflectUtils.modifyFinalField(Configuration.class, "mapperRegistry");
    }
    
    protected AtomicInteger state = new AtomicInteger(ConfigurationStateConstant.NEW);
    
    protected Map<Class<? extends BaseManager>, BaseManager> managerMap = new LinkedHashMap<>();
    
    protected SqlSessionFactory sqlSessionFactory;
    
    protected SqlSession mainSqlSession;
    
    public MybatisMixConfiguration(Environment environment) {
        super(environment);
        init();
    }
    
    public MybatisMixConfiguration() {
        super();
        init();
    }
    
    protected void init() {
        mapUnderscoreToCamelCase = true;
        //TODO 可直接用unsafe的方式写，绕过final限制
        ReflectUtils.setFieldValue(this, "mapperRegistry", new MybatisMixMapperRegistry(this));
        
        SpiExtensionLoader<BaseManager> managers = SpiExtensionLoader.load(BaseManager.class);
        for (BaseManager manager : managers) {
            manager.setConfiguration(this);
            managerMap.putIfAbsent(manager.managerType(), manager);
        }
        MybatisContext.initMybatisContext(this);
    }
    
    public void initAfterSetProperties() {
        if (state.compareAndSet(ConfigurationStateConstant.NEW, ConfigurationStateConstant.SET_PROPERTIES)) {
            for (BaseManager manager : managerMap.values()) {
                manager.initAfterSetProperties();
            }
        }
    }
    
    public void initAfterMybatisInit() {
        if (state.compareAndSet(ConfigurationStateConstant.SET_PROPERTIES, ConfigurationStateConstant.INITIALIZED)) {
            for (BaseManager manager : managerMap.values()) {
                manager.initAfterMybatisInit();
            }
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                close();
            }));
        }
    }
    
    public void close() {
        if (state.compareAndSet(ConfigurationStateConstant.INITIALIZED, ConfigurationStateConstant.CLOSED)) {
            for (BaseManager manager : managerMap.values()) {
                manager.close();
            }
        }
    }
    
    public <T extends BaseManager> T getManager(Class<T> managerClass) {
        return (T) managerMap.get(managerClass);
    }
    
    protected void setManager(Class<? extends BaseManager> managerClass, BaseManager manager) {
        managerMap.put(managerClass, manager);
    }
    
    @Override
    public Executor newExecutor(Transaction transaction, ExecutorType executorType) {
        return getManager(ExecutorManager.class).newExecutor(transaction, executorType);
    }
    
    @Override
    public StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement,
            Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        StatementHandler handler = super
                .newStatementHandler(executor, mappedStatement, parameterObject, rowBounds, resultHandler, boundSql);
        if (getBoolProperty(ConfigurationKeyConstant.logging$logFullSql, false)) {
            handler = new LogFullSqlStatementHandler(handler, mappedStatement);
        }
        return handler;
    }
    
    @Override
    public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement, RowBounds rowBounds,
            ParameterHandler parameterHandler, ResultHandler resultHandler, BoundSql boundSql) {
        ResultSetHandler resultSetHandler = new ExtResultSetHandler(executor, mappedStatement, parameterHandler,
                resultHandler, boundSql, rowBounds);
        resultSetHandler = (ResultSetHandler) interceptorChain.pluginAll(resultSetHandler);
        return resultSetHandler;
    }
    
    @Override
    public MappedStatement getMappedStatement(String id, boolean validateIncompleteStatements) {
        return getManager(MappedStatementManager.class).getMappedStatement(id, validateIncompleteStatements);
    }
    
    @Override
    public void addMappedStatement(MappedStatement ms) {
        getManager(MappedStatementManager.class).addMappedStatement(ms);
    }
    
    public MappedStatement getMappedStatementSuper(String id, boolean validateIncompleteStatements) {
        return super.getMappedStatement(id, validateIncompleteStatements);
    }
    
    public void addMappedStatementSuper(MappedStatement ms) {
        super.addMappedStatement(ms);
    }
    
    @Override
    public String getProperty(String key) {
        if (variables == null) {
            return null;
        }
        return variables.getProperty(key);
    }
    
    @Override
    public void addLoadedResource(String resource) {
        super.addLoadedResource(resource);
        getManager(MapperHotDeployManager.class).addXmlFileResource(resource);
    }
    
    public int getMappedStatementSize() {
        return mappedStatements.size();
    }
    
    public void removeLoadedResource(String resource) {
        loadedResources.remove(resource);
    }
    
    public InterceptorChain getInterceptorChain() {
        return interceptorChain;
    }
    
    public SqlSessionFactory getSqlSessionFactory() {
        return sqlSessionFactory;
    }
    
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }
    
    public SqlSession getMainSqlSession() {
        return mainSqlSession;
    }
    
    public void setMainSqlSession(SqlSession mainSqlSession) {
        this.mainSqlSession = mainSqlSession;
    }
    
    @Override
    public void addInterceptor(Interceptor interceptor) {
        super.addInterceptor(interceptor);
        reOrderInterceptor();
    }
    
    private List<Interceptor> interceptors = (List<Interceptor>) ReflectUtils
            .getFieldValue(interceptorChain, "interceptors");
    
    protected void reOrderInterceptor() {
        TruckUtils.listSort(interceptors);
    }
}
