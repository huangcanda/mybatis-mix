package org.wanghailu.mybatismix.mapping;

import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.mapping.MappedStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wanghailu.mybatismix.common.BaseManager;
import org.wanghailu.mybatismix.constant.ConfigurationKeyConstant;
import org.wanghailu.mybatismix.logging.MainControlLogger;
import org.wanghailu.mybatismix.page.mapping.CountMappedStatementSupplier;
import org.wanghailu.mybatismix.util.ReflectUtils;

import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 对mybatis的MappedStatement进行自定义管理，目前主要逻辑是进行sql日志控制，以及关闭一级缓存
 *
 * @author cdhuang
 * @date 2020/7/14
 */
public class MappedStatementManager extends BaseManager {
    
    private static final Logger logger = LoggerFactory.getLogger(MappedStatementManager.class);
    
    public static final String DYNAMIC_RETURN_TYPE_CLASS = "dynamicReturnTypeClass";
    
    protected boolean closeLocalCache;
    
    protected boolean mapperHotDeploy;
    
    protected EntityMappedStatementCreator mappedStatementCreator;
    
    /**
     * 如果使用xml文件热加载热部署功能，则需要一把读写锁来保证configuration内的线程安全。
     */
    public ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    
    @Override
    public void initAfterSetProperties() {
        this.mappedStatementCreator = new EntityMappedStatementCreator(configuration);
        this.closeLocalCache = configuration.getBoolProperty(ConfigurationKeyConstant.closeLocalCache, false);
        this.mapperHotDeploy = configuration.getBoolProperty(ConfigurationKeyConstant.hotDeploy$enable, false);
    }
    
    public MappedStatement routeMappedStatement(MappedStatement ms, Object parameter) {
        if (parameter instanceof Map && parameter instanceof MapperMethod.ParamMap == false) {
            Map parameterMap = (Map) parameter;
            try {
                Class resultType = (Class) parameterMap.get(MappedStatementManager.DYNAMIC_RETURN_TYPE_CLASS);
                if (resultType != null && !ms.getId().endsWith(CountMappedStatementSupplier.statementSuffix)) {
                    parameterMap.remove(MappedStatementManager.DYNAMIC_RETURN_TYPE_CLASS);
                    ReturnTypeMappedStatementSupplier countMappedStatementSupplier = new ReturnTypeMappedStatementSupplier(resultType, ms);
                    return mappedStatementCreator.getMappedStatement(ms, countMappedStatementSupplier);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return ms;
    }
    
    public MappedStatement getMappedStatement(MappedStatement oldMappedStatement, BaseMappedStatementSupplier function) {
        return mappedStatementCreator.getMappedStatement(oldMappedStatement, function);
    }
    
    public MappedStatement getMappedStatement(String id, boolean validateIncompleteStatements) {
        if (mapperHotDeploy) {
            try {
                readWriteLock.readLock().lock();
                return getMappedStatementOrCreate(id, validateIncompleteStatements);
            } finally {
                readWriteLock.readLock().unlock();
            }
        } else {
            return getMappedStatementOrCreate(id, validateIncompleteStatements);
        }
    }
    
    protected MappedStatement getMappedStatementOrCreate(String id, boolean validateIncompleteStatements) {
        return configuration.getMappedStatementSuper(id, validateIncompleteStatements);
    }
    
    public void addMappedStatement(MappedStatement mappedStatement) {
        if (closeLocalCache) {
            ReflectUtils.setFieldValue(mappedStatement, "flushCacheRequired", true);
        }
        ReflectUtils.setFieldValue(mappedStatement, "statementLog",
                new MainControlLogger(mappedStatement.getId(), configuration));
        configuration.addMappedStatementSuper(mappedStatement);
    }
    
    public EntityMappedStatementCreator getEntityMappedStatementCreator() {
        return mappedStatementCreator;
    }
}
