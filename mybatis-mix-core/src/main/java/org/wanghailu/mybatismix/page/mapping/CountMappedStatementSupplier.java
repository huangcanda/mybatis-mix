package org.wanghailu.mybatismix.page.mapping;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlSource;
import org.wanghailu.mybatismix.mapping.BaseMappedStatementSupplier;

import java.util.Arrays;

/**
 * 定义创建分页MappedStatement的逻辑
 *
 * @author cdhuang
 * @date 2021/9/27
 */
public class CountMappedStatementSupplier extends BaseMappedStatementSupplier {


    public static String statementSuffix = ".AutoCount";

    public CountMappedStatementSupplier(MappedStatement oldMappedStatement) {
        super(oldMappedStatement);
        this.id = oldMappedStatement.getId() + statementSuffix;
    }

    @Override
    public void apply(MappedStatement.Builder builder) {
        ResultMap resultMap = new ResultMap.Builder(oldMappedStatement.getConfiguration(), builder.id(), Integer.class, BaseMappedStatementSupplier.EMPTY_RESULTMAPPING).build();
        builder.resultMaps(Arrays.asList(resultMap));
    }

    @Override
    public SqlSource getSqlSource() {
        return new CountSqlSource(oldMappedStatement);
    }
}
