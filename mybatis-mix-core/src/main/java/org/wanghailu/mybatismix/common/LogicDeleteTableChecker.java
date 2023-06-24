package org.wanghailu.mybatismix.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.exception.MybatisMixException;
import org.wanghailu.mybatismix.util.EntityUtils;
import org.wanghailu.mybatismix.util.PrivateStringUtils;
import org.wanghailu.mybatismix.util.SqlRunner;

import java.util.Collection;

/**
 * 校验逻辑删除对应的表是否存在
 * @author cdhuang
 * @date 2023/3/15
 */
public class LogicDeleteTableChecker {
    
    private static Logger logger = LoggerFactory.getLogger(LogicDeleteTableChecker.class);
    
    protected MybatisMixConfiguration configuration;
    
    public LogicDeleteTableChecker(MybatisMixConfiguration configuration) {
        this.configuration = configuration;
    }
    
    public void checkTableExist(Collection<Class<?>> classSet) {
        if(!configuration.getBoolProperty("check-logic-delete-table-exist",true)){
            return;
        }
        for (Class<?> aClass : classSet) {
            String logicDeleteTable = EntityUtils.getEntityDescriptor(aClass).getLogicDeleteTable();
            if(PrivateStringUtils.isNotEmpty(logicDeleteTable)){
                String sql = "select * from " + logicDeleteTable + " where 1=0";
                try {
                    SqlRunner.executeSelectSql(configuration, sql, rs -> {});
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                    throw new MybatisMixException("校验逻辑删除表不存在，请检查表" + logicDeleteTable + "是否存在！");
                }
            }
        }
    }
}
