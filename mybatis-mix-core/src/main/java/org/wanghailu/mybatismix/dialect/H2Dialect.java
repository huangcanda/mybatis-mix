package org.wanghailu.mybatismix.dialect;

import org.wanghailu.mybatismix.constant.DbTypeConstant;

/**
 * @author cdhuang
 * @date 2023/1/19
 */
public class H2Dialect extends PgDialect {
    
    @Override
    public String getDbType() {
        return DbTypeConstant.h2;
    }
    
}
