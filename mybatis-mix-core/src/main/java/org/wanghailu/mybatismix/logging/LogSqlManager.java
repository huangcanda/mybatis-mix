package org.wanghailu.mybatismix.logging;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.mapping.MappedStatement;
import org.wanghailu.mybatismix.common.BaseManager;
import org.wanghailu.mybatismix.constant.ConfigurationKeyConstant;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 日志打印的 集中管理器
 *
 * @author cdhuang
 * @date 2021/9/8
 */
public class LogSqlManager extends BaseManager {
    
    public static LogSqlManager instance;
    
    protected static ThreadLocal<Set<String>> noLogMapperList = new ThreadLocal<>();
    
    protected static ThreadLocal<Map<String, Log>> statementLogMap = new ThreadLocal<>();
    
    protected Set<String> noLogMappersInProperty = new HashSet<>();
    
    protected static Map<String, Log> getCurrentStatementLogMap() {
        return statementLogMap.get();
    }
    
    protected LogFullSqlProcessor logFullSqlProcessor = new LogFullSqlProcessor();
    
    public LogSqlManager() {
        instance = this;
    }
    
    @Override
    public void initAfterSetProperties() {
        String noLogMappers = configuration.getProperty(ConfigurationKeyConstant.logging$noLogMappers);
        if (noLogMappers != null) {
            String[] strings = noLogMappers.split(",");
            for (String str : strings) {
                noLogMappersInProperty.add(str);
            }
        }
    }
    
    /**
     * 是否有当前线程的日志打印配置
     *
     * @param statementId
     * @return
     */
    protected boolean containStateLogMap(String statementId) {
        Map<String, Log> map = statementLogMap.get();
        return map == null ? false : map.containsKey(statementId);
    }
    
    /**
     * 临时添加需要关闭sql日志打印的 mapper，只对当前线程上下文生效
     *
     * @param mapper
     */
    public void addNoLogMapperOnTemp(String mapper) {
        Set<String> set = noLogMapperList.get();
        if (set == null) {
            set = new HashSet<>();
            set.addAll(noLogMappersInProperty);
            noLogMapperList.set(set);
        }
        set.add(mapper);
        resetStatementLogMap();
    }
    
    
   
    
    /**
     * 移除所有临时的设置
     */
    public void removeAllTempNoLogMapper() {
        noLogMapperList.remove();
        statementLogMap.remove();
    }
    
    
    /**
     * 重置mappedStatement的日志打印控制
     */
    protected void resetStatementLogMap() {
        Map<String, Log> map = statementLogMap.get();
        if (map == null) {
            map = new HashMap<>(configuration.getMappedStatementSize());
            statementLogMap.set(map);
        } else {
            map.clear();
        }
        for (Object statement : configuration.getMappedStatements()) {
            if (statement instanceof MappedStatement) {
                MappedStatement mappedStatement = (MappedStatement) statement;
                Log log = mappedStatement.getStatementLog();
                if (log instanceof MainControlLogger) {
                    map.put(mappedStatement.getId(), null);
                }
            }
        }
    }
    
    /**
     * 全局重置日志打印
     */
    protected void resetLogger() {
        for (Object statement : configuration.getMappedStatements()) {
            if (statement instanceof MappedStatement) {
                MappedStatement mappedStatement = (MappedStatement) statement;
                Log log = mappedStatement.getStatementLog();
                if (log instanceof MainControlLogger) {
                    ((MainControlLogger) log).resetLogger();
                }
            }
        }
    }
    
    protected boolean doCloseLogSql(String mappedStatementId, boolean isThreadLocal) {
        if (configuration.getBoolProperty(ConfigurationKeyConstant.logging$logSql,true)) {
            Set<String> noLogMapperSet =
                    isThreadLocal ? noLogMapperList.get() : noLogMappersInProperty;
            boolean isCloseSqlMapper = false;
            if (noLogMapperSet != null) {
                for (String s : noLogMapperSet) {
                    if (mappedStatementId.startsWith(s)) {
                        isCloseSqlMapper = true;
                        break;
                    }
                }
            }
            return isCloseSqlMapper;
        } else {
            return true;
        }
    }
    
    public LogFullSqlProcessor getLogFullSqlProcessor() {
        return logFullSqlProcessor;
    }
}
