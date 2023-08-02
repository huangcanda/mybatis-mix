package org.wanghailu.mybatismix.page.countsql;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.PagerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wanghailu.mybatismix.common.BaseManager;
import org.wanghailu.mybatismix.util.SpiExtensionLoader;
import org.wanghailu.mybatismix.util.TruckUtils;

import java.util.HashMap;
import java.util.Map;

import static org.wanghailu.mybatismix.constant.ConfigurationKeyConstant.countSqlGeneratorName;

/**
 * 通过解析sql的方式生成count语句
 */
public class CountSqlManager extends BaseManager {

    private static Logger logger = LoggerFactory.getLogger(CountSqlManager.class);

    protected static Map<String, CountSqlGenerator> countSqlGeneratorMap = new HashMap<>();
    public static String defaultGeneratorName;
    static {
        countSqlGeneratorMap.putAll(SpiExtensionLoader.loadSpiExtensionMap(CountSqlGenerator.class));
        defaultGeneratorName = countSqlGeneratorMap.keySet().iterator().next();
    }

    public String getCountSql(String baseSql, String dbType) {
        String generatorName = configuration.getProperty(countSqlGeneratorName,defaultGeneratorName);
        if(SimpleCountSqlGenerator.NAME.equals(generatorName)){
            return getSimpleCountSql(baseSql,dbType);
        }else{
            try {
                String countSql = countSqlGeneratorMap.get(generatorName).getCountSql(baseSql,dbType);;
                if (countSql == null) {
                    countSql = getSimpleCountSql(baseSql,dbType);
                }
                if(logger.isTraceEnabled()){
                    logger.trace(generatorName+"Generator,countSql:" + TruckUtils.lineSeparator + countSql);
                }
                return countSql;
            } catch (Exception e) {
                logger.warn(generatorName+"Generator生成countSql异常,原生sql为:" + TruckUtils.lineSeparator + baseSql + TruckUtils.lineSeparator + "------------------------------------------------------------");
                return getSimpleCountSql(baseSql,dbType);
            }
        }
    }

    protected String getSimpleCountSql(String baseSql,String dbType){
        return countSqlGeneratorMap.get(SimpleCountSqlGenerator.NAME).getCountSql(baseSql,dbType);
    }

    public static void main(String[] arg) {
        String sql2 = "select count(*) as test from t_project group by t.user desc";

        System.out.println(CountSqlManager.countSqlGeneratorMap.get("optimize").getCountSql(sql2, "mysql"));
        System.out.println(PagerUtils.count(sql2, DbType.mysql));
    }
}
