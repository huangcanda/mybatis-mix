package org.wanghailu.mybatismix.dialect;

import org.wanghailu.mybatismix.constant.DbTypeConstant;

/**
 * @author cdhuang
 * @date 2023/1/19
 */
public class GbasedbtDialect extends MysqlDialect {
    
    @Override
    public String getDbType() {
        return DbTypeConstant.gbasedbt;
    }
}
