package org.wanghailu.mybatismix.test.base;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.BeforeClass;
import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.test.init.DbInitializer;
import org.wanghailu.mybatismix.util.ConfigurationUtils;

/**
 * @author cdhuang
 * @date 2023/3/23
 */
public class BaseTestNoSpring {
    
    protected static MybatisMixConfiguration configuration;
    
    @BeforeClass
    public static void startup() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:testdb");
        dataSource.setUsername("test");
        dataSource.setPassword("test");
        DbInitializer.initTable(dataSource);
        Environment environment = new Environment("mybatisMix", new JdbcTransactionFactory(), dataSource);
        configuration = ConfigurationUtils.createConfiguration("");
        configuration.setEnvironment(environment);
        ConfigurationUtils.putConfigurationProperties(configuration, null);
        SqlSessionFactory sqlSessionFactory = new DefaultSqlSessionFactory(configuration);
        SqlSessionManager sqlSessionManager = SqlSessionManager.newInstance(sqlSessionFactory);
        ConfigurationUtils.mybatisInitialized(configuration, sqlSessionFactory, sqlSessionManager);
    }
}
