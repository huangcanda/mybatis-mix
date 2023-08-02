package org.wanghailu.mybatismix.page.countsql;

public class SimpleCountSqlGenerator implements CountSqlGenerator {

    public static final String NAME ="simple";

    @Override
    public String getCountSql(String baseSql, String dbType) {
        return "select count(1) as countNum from (" + baseSql + ") countSql";
    }

    @Override
    public String name() {
        return NAME;
    }
}
