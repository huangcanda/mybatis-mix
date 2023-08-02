package org.wanghailu.mybatismix.dao;

import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.wanghailu.mybatismix.mapping.MappedStatementManager;
import org.wanghailu.mybatismix.model.Page;
import org.wanghailu.mybatismix.page.PageHelper;
import org.wanghailu.mybatismix.transaction.TransactionRunner;
import org.wanghailu.mybatismix.util.MybatisContext;
import org.wanghailu.mybatismix.util.TruckUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;

/**
 * MyBatisDao
 */
@Deprecated
public class MyBatisDao extends SqlSessionDaoSupport {
    
    public static final String DYNAMIC_MAPPER_ID = "org.wanghailu.mybatismix.dao.sql.mapper.FrameworkMapper.";
    
    private static final String DYNAMIC_INSERT_SQL_STATEMENT_ID = DYNAMIC_MAPPER_ID + "DynamicInsertSql";
    
    private static final String DYNAMIC_DELETE_SQL_STATEMENT_ID = DYNAMIC_MAPPER_ID + "DynamicDeleteSql";
    
    private static final String DYNAMIC_UPDATE_SQL_STATEMENT_ID = DYNAMIC_MAPPER_ID + "DynamicUpdateSql";
    
    private static final String DYNAMIC_SELECT_SQL_STATEMENT_ID = DYNAMIC_MAPPER_ID + "DynamicSelectSql";
    
    private static final String DYNAMIC_SQL = "dynamicSql";
    
    private static final Pattern insertPattern = Pattern.compile("^[insert/s].*");
    
    private static final Pattern deletePattern = Pattern.compile("^[delete/s].*");
    
    private static final Pattern updatePattern = Pattern.compile("^[update/s].*");
    
    private static int defaultBatchFlushDataSize = 1000;
    
    private boolean selectOneWithLimit = false;
    
    //TODO
    private boolean selectOneWithCheck = false;
    
    private static Page limitOnePage = new Page(1, 1);
    
    static {
        limitOnePage.setSelectCount(false);
        limitOnePage.setSqlParamMode(false);
    }
    
    private <Entity> Entity selectOne(Supplier<List<Entity>> listSupplier) {
        if (selectOneWithLimit) {
            try {
                PageHelper.setLocalPage(limitOnePage);
                List<Entity> entities = listSupplier.get();
                return entities.size() == 0 ? null : entities.get(0);
            } finally {
                PageHelper.clearPage();
            }
        } else {
            List<Entity> entities = listSupplier.get();
            return entities.size() == 0 ? null : entities.get(0);
        }
    }
    
    public <E> List<E> selectListBySql(String statementId) {
        return this.getSqlSession().selectList(statementId);
    }
    
    public <E> List<E> selectListBySql(String statementId, Map<String, Object> parameterMap) {
        return this.getSqlSession().selectList(statementId, parameterMap);
    }
    
    public <T> T selectOneBySql(String statementId) {
        List<T> list = this.getSqlSession().selectList(statementId);
        if (list == null || list.size() <= 0) {
            return null;
        } else {
            return list.get(0);
        }
    }
    
    
    public <T> T selectOneBySql(String statementId, Map<String, Object> parameterMap) {
        List<T> list = this.getSqlSession().selectList(statementId, parameterMap);
        if (list == null || list.size() <= 0) {
            return null;
        } else {
            return list.get(0);
        }
    }
    
    
    public int insertBySql(String statementId, Map<String, Object> parameterMap) {
        return this.getSqlSession().insert(statementId, parameterMap);
    }
    
    
    public int insertBySql(String statementId, List<Map<String, Object>> parameters) {
        return newBatchExecuteTemplateForList(parameters, parameter -> this.insertBySql(statementId, parameter),
                defaultBatchFlushDataSize);
    }
    
    
    public int deleteBySql(String statementId, Map<String, Object> parameterMap) {
        return this.getSqlSession().delete(statementId, parameterMap);
    }
    
    
    public int deleteBySql(String statementId, List<Map<String, Object>> parameters) {
        return newBatchExecuteTemplateForList(parameters, parameter -> this.deleteBySql(statementId, parameter),
                defaultBatchFlushDataSize);
    }
    
    
    public int updateBySql(String statementId, Map<String, Object> parameterMap) {
        return this.getSqlSession().update(statementId, parameterMap);
    }
    
    
    public int updateBySql(String statementId, List<Map<String, Object>> parameters) {
        return newBatchExecuteTemplateForList(parameters, parameter -> this.updateBySql(statementId, parameter),
                defaultBatchFlushDataSize);
    }
    
    
    public int insertByDynamicSql(String sql, Map<String, Object> parameterMap) {
        parameterMap.put(DYNAMIC_SQL, sql);
        return this.insertBySql(DYNAMIC_INSERT_SQL_STATEMENT_ID, parameterMap);
    }
    
    
    public int insertByDynamicSql(String sql, List<Map<String, Object>> parameters) {
        return newBatchExecuteTemplateForList(parameters, parameter -> this.insertByDynamicSql(sql, parameter),
                defaultBatchFlushDataSize);
    }
    
    
    public int deleteByDynamicSql(String sql, Map<String, Object> parameterMap) {
        parameterMap.put(DYNAMIC_SQL, sql);
        return this.deleteBySql(DYNAMIC_DELETE_SQL_STATEMENT_ID, parameterMap);
    }
    
    
    public int deleteByDynamicSql(String sql, List<Map<String, Object>> parameters) {
        return newBatchExecuteTemplateForList(parameters, parameter -> this.deleteByDynamicSql(sql, parameter),
                defaultBatchFlushDataSize);
    }
    
    
    public int updateByDynamicSql(String sql, Map<String, Object> parameterMap) {
        parameterMap.put(DYNAMIC_SQL, sql);
        return this.updateBySql(DYNAMIC_UPDATE_SQL_STATEMENT_ID, parameterMap);
    }
    
    
    public int updateByDynamicSql(String sql, List<Map<String, Object>> parameters) {
        return newBatchExecuteTemplateForList(parameters, parameter -> this.updateByDynamicSql(sql, parameter),
                defaultBatchFlushDataSize);
    }
    
    
    public <K, V> Map<K, V> selectOneByDynamicSql(String sql) {
        return this.selectOneByDynamicSql(sql, null);
    }
    
    
    public <K, V> Map<K, V> selectOneByDynamicSql(String sql, Map<String, Object> parameterMap) {
        List<Map<K, V>> maps = this.selectListByDynamicSql(sql, parameterMap);
        if (null != maps && maps.size() > 0) {
            return maps.get(0);
        }
        return null;
    }
    
    
    public <R> R selectOneByDynamicSql(String sql, Map<String, Object> parameterMap, Class<R> returnType) {
        List<R> maps = this.selectListByDynamicSql(sql, parameterMap, returnType);
        if (null != maps && maps.size() > 0) {
            return maps.get(0);
        }
        return null;
    }
    
    
    public <K, V> List<Map<K, V>> selectListByDynamicSql(String sql) {
        return this.selectListByDynamicSql(sql, null);
    }
    
    
    public <K, V> List<Map<K, V>> selectListByDynamicSql(String sql, Map<String, Object> parameterMap) {
        Map<String, Object> parameterMapNew = parameterMap == null ? new HashMap<>() : new HashMap<>(parameterMap);
        parameterMapNew.put(DYNAMIC_SQL, sql);
        return this.selectListBySql(DYNAMIC_SELECT_SQL_STATEMENT_ID, parameterMapNew);
    }
    
    
    public <R> List<R> selectListByDynamicSql(String sql, Map<String, Object> parameterMap, Class<R> returnType) {
        Map<String, Object> parameterMapNew = parameterMap == null ? new HashMap<>() : new HashMap<>(parameterMap);
        parameterMapNew.put(DYNAMIC_SQL, sql);
        parameterMapNew.put(MappedStatementManager.DYNAMIC_RETURN_TYPE_CLASS, returnType);
        return this.selectListBySql(DYNAMIC_SELECT_SQL_STATEMENT_ID, parameterMapNew);
    }
    
    
    public int executeSqls(List<String> sqls, List<Map<String, Object>> parameters) {
        if (sqls == null || parameters == null || sqls.size() != parameters.size()) {
            throw new RuntimeException("sqls size mismatch with parameters!");
        } else {
            int size = sqls.size();
            for (int i = 0; i < size; i++) {
                String sql = sqls.get(i);
                Map<String, Object> parameterMap = parameters.get(i);
                //区分insert,delete,update语句分别执行
                if (insertPattern.matcher(sql.toLowerCase()).matches()) {
                    this.insertByDynamicSql(sql, parameterMap);
                }
                if (updatePattern.matcher(sql.toLowerCase()).matches()) {
                    this.updateByDynamicSql(sql, parameterMap);
                }
                if (deletePattern.matcher(sql.toLowerCase()).matches()) {
                    this.deleteByDynamicSql(sql, parameterMap);
                }
            }
        }
        return 0;
    }
    
    /**
     * 针对list数据进行操作的批处理封装+
     *
     * @param list
     * @param consumer
     * @param <T>
     * @return
     */
    public <T> int newBatchExecuteTemplateForList(List<T> list, ToIntFunction<T> consumer, int batchFlushDataSize) {
        if (TruckUtils.isEmpty(list)) {
            logger.warn("批处理的list是空的，没有被影响的数据！");
            return 0;
        }
        if (list.size() == 1) {
            return consumer.applyAsInt(list.get(0));
        }
        TransactionRunner.forceRunInTransaction(MybatisContext.getConfiguration(), () -> {
            for (T t : list) {
                consumer.applyAsInt(t);
            }
        });
        return 0;
    }
    
}
