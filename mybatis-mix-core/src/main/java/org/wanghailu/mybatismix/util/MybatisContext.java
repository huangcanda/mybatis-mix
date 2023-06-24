package org.wanghailu.mybatismix.util;

import org.wanghailu.mybatismix.MybatisMixConfiguration;

/**
 * @author cdhuang
 * @date 2022/12/26
 */
public class MybatisContext {
    
    protected static MybatisMixConfiguration configuration;
    
    protected static String databaseId;
    
    
    public static void initMybatisContext(MybatisMixConfiguration configuration) {
        MybatisContext.configuration = configuration;
    }
    /**
     * 获得数据库类型
     *
     * @return
     */
    public static String getDatabaseId() {
        if (databaseId == null && configuration != null) {
            databaseId = configuration.getDatabaseId();
        }
        return databaseId;
    }
    
    public static MybatisMixConfiguration getConfiguration() {
        return configuration;
    }
}
