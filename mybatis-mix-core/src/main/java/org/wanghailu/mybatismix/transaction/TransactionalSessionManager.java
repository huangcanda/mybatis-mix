package org.wanghailu.mybatismix.transaction;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.wanghailu.mybatismix.common.BaseManager;
import org.wanghailu.mybatismix.exception.MybatisMixException;

/**
 * Mybatis自带的Session管理进行事务控制
 */
public class TransactionalSessionManager extends BaseManager{

    private SqlSessionManager sqlSessionManager;
    
    @Override
    public void initAfterMybatisInit() {
        checkTransactionSessionSupport();
        super.initAfterMybatisInit();
    }
    
    protected void checkTransactionSessionSupport(){
        SqlSession mainSqlSession = configuration.getMainSqlSession();
        if(configuration.getEnvironment().getTransactionFactory() instanceof JdbcTransactionFactory == false){
            throw new MybatisMixException("使用Mybatis自带的Session管理，则必须直接使用Jdbc事务！");
        }
        if(mainSqlSession instanceof SqlSessionManager == false){
            mainSqlSession = SqlSessionManager.newInstance(configuration.getSqlSessionFactory());
            configuration.setMainSqlSession(mainSqlSession);
        }
        sqlSessionManager = (SqlSessionManager) mainSqlSession;
    }
    
    public boolean inTransaction() {
        return sqlSessionManager.isManagedSessionStarted();
    }
    
    public <T> T forceRunInTransaction(TransactionCall<T> callback) {
        if (inTransaction()) {
            return callback.execute();
        } else {
            sqlSessionManager.startManagedSession();
            try {
                return callback.execute();
            } finally {
                sqlSessionManager.close();
            }
        }
    }
}
