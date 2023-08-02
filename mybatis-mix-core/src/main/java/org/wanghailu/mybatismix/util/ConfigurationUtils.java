package org.wanghailu.mybatismix.util;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.wanghailu.mybatismix.MybatisMixConfiguration;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * MybatisMixConfiguration的初始化逻辑抽取
 */
public class ConfigurationUtils {
    
    public static MybatisMixConfiguration createConfiguration(String configLocation) {
        return createConfiguration(MybatisMixConfiguration.class, configLocation);
    }
    
    public static MybatisMixConfiguration createConfiguration(
            Class<? extends MybatisMixConfiguration> configurationClass, String configLocation) {
        InputStream inputStream = null;
        if (PrivateStringUtils.isNotEmpty(configLocation)) {
            if (configLocation.startsWith("classpath:")) {
                configLocation = configLocation.substring("classpath:".length());
            }
            inputStream = ConfigurationUtils.class.getResourceAsStream(configLocation);
        }
        return createConfiguration(configurationClass, inputStream);
    }
    
    public static MybatisMixConfiguration createConfiguration(
            Class<? extends MybatisMixConfiguration> configurationClass, InputStream inputStream) {
        MybatisMixConfiguration configuration;
        try {
            if (inputStream != null && inputStream.available() > 0) {
                XMLConfigBuilder xmlConfigBuilder = new XMLConfigBuilder(configurationClass, inputStream, null, null);
                configuration = (MybatisMixConfiguration) xmlConfigBuilder.getConfiguration();
                xmlConfigBuilder.parse();
            } else {
                configuration = ReflectUtils.newInstance(configurationClass);
            }
        } catch (Exception e) {
            ExceptionUtils.throwException(e);
            return null;
        }
        
        return configuration;
    }
    
    public static void putConfigurationProperties(MybatisMixConfiguration configuration, Map configurationProperties) {
        if (TruckUtils.isNotEmpty(configurationProperties)) {
            Properties variables = configuration.getVariables();
            if (variables == null) {
                variables = new Properties();
                configuration.setVariables(variables);
            }
            variables.putAll(configurationProperties);
        }
        configuration.initAfterSetProperties();
    }
    
    public static void mybatisInitialized(MybatisMixConfiguration configuration, SqlSessionFactory sqlSessionFactory,
            SqlSession mainSqlSession) {
        configuration.setSqlSessionFactory(sqlSessionFactory);
        configuration.setMainSqlSession(mainSqlSession);
        configuration.initAfterMybatisInit();
    }
}
