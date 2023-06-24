package org.wanghailu.mybatismix.pagehelper.countsql;

public class OptimizeCountSqlGenerator implements CountSqlGenerator {

    public static final String NAME ="optimize";

    @Override
    public String getCountSql(String baseSql, String dbType) {
        return CountSqlByDruid.getCountSql(baseSql, dbType);
    }

    @Override
    public String name() {
        return NAME;
    }
}
