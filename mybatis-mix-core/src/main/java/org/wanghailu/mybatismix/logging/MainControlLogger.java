package org.wanghailu.mybatismix.logging;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.util.PrivateStringUtils;

/**
 * mybatis 的 sql日志打印控制。根据LoggingManager的配置判断是否打印sql日志。
 */
public class MainControlLogger extends DelegateLogger {
    
    private String mappedStatementId;
    
    private LoggingManager loggingManager;

    private String logPrefix;
    
    public MainControlLogger(String mappedStatementId,MybatisMixConfiguration configuration) {
        super(null);
        this.mappedStatementId = mappedStatementId;
        this.loggingManager = configuration.getManager(LoggingManager.class);
        this.logPrefix = configuration.getLogPrefix();
    }
    
    @Override
    public Log getDelegate() {
        if (loggingManager.containStateLogMap(mappedStatementId)) {
            Log local = LoggingManager.getCurrentStatementLogMap().get(mappedStatementId);
            if (local == null) {
                if (loggingManager.doCloseLogSql(mappedStatementId, true)) {
                    local = new EmptyLogger();
                } else {
                    local = createLog();
                }
                LoggingManager.getCurrentStatementLogMap().put(mappedStatementId, local);
            }
            return local;
        }
        if (delegate == null) {
            synchronized (this) {
                if (delegate == null) {
                    if (loggingManager.doCloseLogSql(mappedStatementId, false)) {
                        delegate = new EmptyLogger();
                    } else {
                        delegate = createLog();
                    }
                }
            }
        }
        return delegate;
    }

    protected Log createLog(){
        String id = mappedStatementId;
        if(PrivateStringUtils.isNotEmpty(logPrefix)){
            id = logPrefix + id;
        }
        return LogFactory.getLog(id);
    }
    
    
    public void resetLogger() {
        delegate = null;
    }
}
