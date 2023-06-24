package org.wanghailu.mybatismix.pagehelper.countsql;

import org.wanghailu.mybatismix.support.SpiExtension;

public interface CountSqlGenerator extends SpiExtension {

    String getCountSql(String baseSql, String dbType);

}
