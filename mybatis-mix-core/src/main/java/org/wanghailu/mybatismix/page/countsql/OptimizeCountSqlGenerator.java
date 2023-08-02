package org.wanghailu.mybatismix.page.countsql;

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
    
    @Override
    public boolean agreeLoad() {
        try{
            Class.forName("com.alibaba.druid.sql.ast.SQLStatement");
            return true;
        }catch (ClassNotFoundException e){
            return false;
        }
    }
    
    @Override
    public int getOrder() {
        return -2000;
    }
}
