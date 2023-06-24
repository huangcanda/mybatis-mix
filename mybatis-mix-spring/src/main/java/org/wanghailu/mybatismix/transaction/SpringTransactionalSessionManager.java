package org.wanghailu.mybatismix.transaction;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.wanghailu.mybatismix.annotation.OrderedItem;
import org.wanghailu.mybatismix.common.BaseManager;
import org.wanghailu.mybatismix.util.SpringUtils;

import java.util.Optional;

/**
 * Spring进行事务控制
 *
 * @author cdhuang
 * @date 2023/3/24
 */
@OrderedItem
public class SpringTransactionalSessionManager extends TransactionalSessionManager {
    
    private TransactionTemplate transactionTemplate;
    
    private TransactionTemplate getTransactionTemplate() {
        if (transactionTemplate == null) {
            TransactionTemplate template = new TransactionTemplate();
            template.setTransactionManager(SpringUtils.getBean(PlatformTransactionManager.class));
            template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            transactionTemplate = template;
        }
        return transactionTemplate;
    }
    
    @Override
    public void checkTransactionSessionSupport() {
    }
    
    /**
     * 判断当前是否在spring的事务控制中
     *
     * @return
     */
    @Override
    public boolean inTransaction() {
        return TransactionSynchronizationManager.hasResource(configuration.getSqlSessionFactory())
                || TransactionSynchronizationManager.hasResource(
                Optional.ofNullable(configuration).map(Configuration::getEnvironment).map(Environment::getDataSource)
                        .orElseGet(null));
    }
    
    @Override
    public <T> T forceRunInTransaction(TransactionCall<T> callback) {
        if (inTransaction()) {
            return callback.execute();
        } else {
            return getTransactionTemplate().execute(x -> callback.execute());
        }
    }
    
    @Override
    public boolean agreeLoad() {
        return SpringUtils.isSpringApplicationContextInit();
    }
    
    @Override
    public Class<? extends BaseManager> managerType() {
        return TransactionalSessionManager.class;
    }
}
