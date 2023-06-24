package org.wanghailu.mybatismix.mapping;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cdhuang
 * @date 2021/9/27
 */
public abstract class BaseMappedStatementSupplier {

    public static final List<ResultMapping> EMPTY_RESULTMAPPING = new ArrayList<>(0);

    protected String id;

    protected MappedStatement oldMappedStatement;

    public BaseMappedStatementSupplier(MappedStatement oldMappedStatement) {
        this.oldMappedStatement = oldMappedStatement;
    }

    /**
     * 对新的MappedStatement的Builder构造器进行处理
     *
     * @param builder
     */
    public void apply(MappedStatement.Builder builder) {

    }

    /**
     * 新的MappedStatement的id定义
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * 定义新的MappedStatement的SqlSource
     *
     * @return
     */
    public SqlSource getSqlSource() {
        return oldMappedStatement.getSqlSource();
    }

    /**
     * 定义新的MappedStatement的SqlCommandType
     *
     * @return
     */
    public SqlCommandType getSqlCommandType() {
        return oldMappedStatement.getSqlCommandType();
    }
}
