package org.wanghailu.mybatismix.constant;

public interface SqlSymbolConstant {

    String AND = " AND ";
    String OR = " OR ";
    
    String IS_NULL = " IS NULL";
    String IS_NOT_NULL = " IS NOT NULL";

    String EQ = " = ";
    String NOT_EQ = " != ";
    
    String GT = " > ";
    String GE = " >= ";
    
    String LT = " < ";
    String LE = " <= ";

    String LIKE = " LIKE ";

    String IN = " IN ";
    String NOT_IN = " NOT IN ";
    
    String BETWEEN = " BETWEEN ";
    String NOT_BETWEEN = " NOT BETWEEN ";

    String COUNT ="count";

    String MAX ="max";

    String MIN ="min";

    String SUM ="sum";

    String AVG = "avg";

    String[] COMMON_FUNCTION = {COUNT,MAX,MIN,SUM,AVG};
}
