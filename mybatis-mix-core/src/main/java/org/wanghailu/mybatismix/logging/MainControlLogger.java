package org.wanghailu.mybatismix.logging;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.util.PrivateStringUtils;

/**
 * mybatis 的 sql日志打印控制。根据PrintSqlManager的配置判断是否打印sql日志。
 *
 * @author cdhuang
 * @date 2021/9/26
 */
public class MainControlLogger extends DelegateLogger {
    
    private String mappedStatementId;
    
    private LogSqlManager logSqlManager;

    private String logPrefix;
    
    public MainControlLogger(String mappedStatementId,MybatisMixConfiguration configuration) {
        super(null);
        this.mappedStatementId = mappedStatementId;
        this.logSqlManager = configuration.getManager(LogSqlManager.class);
        this.logPrefix = configuration.getLogPrefix();
    }
    
    @Override
    public Log getDelegate() {
        if (logSqlManager.containStateLogMap(mappedStatementId)) {
            Log local = LogSqlManager.getCurrentStatementLogMap().get(mappedStatementId);
            if (local == null) {
                if (logSqlManager.doCloseLogSql(mappedStatementId, true)) {
                    local = new EmptyLogger();
                } else {
                    local = createLog();
                }
                LogSqlManager.getCurrentStatementLogMap().put(mappedStatementId, local);
            }
            return local;
        }
        if (delegate == null) {
            synchronized (this) {
                if (delegate == null) {
                    if (logSqlManager.doCloseLogSql(mappedStatementId, false)) {
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
