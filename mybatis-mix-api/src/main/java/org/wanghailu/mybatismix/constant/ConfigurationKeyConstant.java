package org.wanghailu.mybatismix.constant;

/**
 * 统一管理配置项
 * @author cdhuang
 * @date 2023/1/17
 */
@SuppressWarnings("ALL")
public interface ConfigurationKeyConstant {

    String entityPackages = "entity-packages";
    
    String closeLocalCache = "close-local-cache";
    
    String defaultExecutorType = "default-executor-type";
    
    String defaultBatchExecutorType = "default-batch-executor-type";
    
    String logging$logSql = "logging.log-sql";
    
    String logging$logFullSql = "logging.log-full-sql";
    
    String logging$formatFullSql = "logging.format-full-sql";
    
    String logging$noLogMappers = "logging.no-log-mappers";
    
    String hotDeploy$enable = "hot-deploy.enable";
    
    String workerId = "key-generator.snowflake.worker-id";

    String fillField$idDefaultStrategy = "fill-field.id-default-strategy";
}
