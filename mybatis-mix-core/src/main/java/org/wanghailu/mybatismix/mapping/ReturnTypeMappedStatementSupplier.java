package org.wanghailu.mybatismix.mapping;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;

import java.util.Arrays;

/**
 * @author cdhuang
 * @date 2021/9/27
 */
public class ReturnTypeMappedStatementSupplier extends BaseMappedStatementSupplier {

    private Class resultType;

    public ReturnTypeMappedStatementSupplier(Class resultType,MappedStatement oldMappedStatement) {
        super(oldMappedStatement);
        this.resultType = resultType;
        id = oldMappedStatement.getId() + "_" + resultType.getName();
    }

    @Override
    public void apply(MappedStatement.Builder builder) {
        ResultMap resultMap = new ResultMap.Builder(oldMappedStatement.getConfiguration(), builder.id(), resultType, EMPTY_RESULTMAPPING).build();
        builder.resultMaps(Arrays.asList(resultMap));
    }
}
