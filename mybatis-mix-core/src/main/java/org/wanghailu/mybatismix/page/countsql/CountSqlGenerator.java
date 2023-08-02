package org.wanghailu.mybatismix.page.countsql;

import org.wanghailu.mybatismix.support.SpiExtension;

public interface CountSqlGenerator extends SpiExtension {

    String getCountSql(String baseSql, String dbType);

}
