package org.wanghailu.mybatismix.page.mapping;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 修改count语句和参数
 */
public class CountBoundSql extends BoundSql {

    private String originalSql;

    private BoundSql delegate;

    private List<ParameterMapping> countSqlParameterMappings;


    public CountBoundSql(Configuration configuration, BoundSql delegate, String countSql) {
        super(configuration, countSql, delegate.getParameterMappings(), delegate.getParameterObject());
        this.originalSql = delegate.getSql();
        this.delegate = delegate;
        checkParameterLength();
    }

    private void checkParameterLength() {
        String countSql = getSql();
        int originalSqlParameterLength = getParametersLength(originalSql);
        int countSqlParameterLength = getParametersLength(countSql);
        if (originalSqlParameterLength > countSqlParameterLength) {
            countSqlParameterMappings = new ArrayList<>();
            int subLength = originalSqlParameterLength - countSqlParameterLength;
            int index =0;
            for (ParameterMapping parameterMapping : delegate.getParameterMappings()) {
                if(index>=subLength){
                    countSqlParameterMappings.add(parameterMapping);
                }
                index++;
            }
        }
    }

    private int getParametersLength(String sql) {
        return sql.length() - sql.replaceAll("\\?", "").length();
    }

    @Override
    public List<ParameterMapping> getParameterMappings() {
        if(countSqlParameterMappings==null){
            return delegate.getParameterMappings();
        }else{
            return countSqlParameterMappings;
        }
    }

    @Override
    public boolean hasAdditionalParameter(String name) {
        return delegate.hasAdditionalParameter(name);
    }

    @Override
    public void setAdditionalParameter(String name, Object value) {
        delegate.setAdditionalParameter(name, value);
    }

    @Override
    public Object getAdditionalParameter(String name) {
        return delegate.getAdditionalParameter(name);
    }
}
