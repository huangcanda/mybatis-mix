package org.wanghailu.mybatismix.dialect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wanghailu.mybatismix.common.BaseManager;
import org.wanghailu.mybatismix.util.ExceptionUtils;
import org.wanghailu.mybatismix.util.SpiExtensionLoader;
import org.wanghailu.mybatismix.util.TruckUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;

/**
 * 分页方言管理器
 *
 * @author cdhuang
 * @date 2021/9/26
 */
public class DialectManager extends BaseManager {
    
    private static Logger logger = LoggerFactory.getLogger(DialectManager.class);
    
    /**
     * 缓存url对应的dialect
     */
    protected Map<DataSource, String> dbTypeMap = new WeakHashMap<>();
    
    public Map<String, Dialect> dialectMap = new HashMap<>();
    
    @Override
    public void initAfterSetProperties() {
        //基于jdk的SPI加载支持的方言，方便其他数据库扩展
        dialectMap.putAll(SpiExtensionLoader.loadSpiExtensionMap(Dialect.class));
        setProperties(configuration.getVariables());
    }
    
    
    /**
     * 根据数据源查找对应方言
     *
     * @return
     */
    public Dialect getDialect() {
        return getDialect(configuration.getEnvironment().getDataSource());
    }
    
    /**
     * 根据数据源查找对应方言
     *
     * @param dataSource
     * @return
     */
    public Dialect getDialect(DataSource dataSource) {
        return getDialect(getDbType(dataSource));
    }
    
    public Dialect getDialect(String dbType) {
        return dialectMap.get(dbType);
    }
    
    public String getDbType(DataSource dataSource) {
        String dbType = dbTypeMap.get(dataSource);
        if (dbType == null) {
            synchronized (dbTypeMap) {
                dbType = dbTypeMap.get(dataSource);
                if (dbType == null) {
                    String url = getUrl(dataSource);
                    for (String type : dialectMap.keySet()) {
                        if (url.contains(":" + type + ":")) {
                            dbType = type;
                            break;
                        }
                    }
                    TruckUtils.assertNotNull(dbType, "不支持的数据库方言，请自行通过SPI方式进行拓展实现！jdbc地址为：" + url);
                    dbTypeMap.put(dataSource, dbType);
                }
            }
        }
        return dbType;
    }
    
    public void setProperties(Properties properties) {
        for (Dialect dialect : dialectMap.values()) {
            dialect.setProperties(properties);
        }
    }
    
    private static String getUrl(DataSource dataSource) {
        String url;
        
            Connection connection = null;
            try {
                connection = dataSource.getConnection();
                url = connection.getMetaData().getURL();
            } catch (SQLException e) {
                ExceptionUtils.throwException(e);
                return null;
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        logger.info("获取jdbcUrl时连接池关闭失败");
                    }
                }
            }
        return url;
    }
}
