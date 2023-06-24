package org.wanghailu.mybatismix.mapper.impl;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.example.BaseDeleteExample;
import org.wanghailu.mybatismix.example.BaseQueryExample;
import org.wanghailu.mybatismix.example.BaseUpdateExample;
import org.wanghailu.mybatismix.mapper.IExampleMapper;
import org.wanghailu.mybatismix.mapping.EntityMappedStatementNameEnum;
import org.wanghailu.mybatismix.transaction.TransactionRunner;
import org.wanghailu.mybatismix.util.EntityUtils;

import java.util.List;

/**
 * @author cdhuang
 * @date 2023/1/18
 */
public class ExampleMapperImpl<Entity> extends BaseMapperImpl<Entity> implements IExampleMapper<Entity> {
    
    protected Logger logger = LoggerFactory.getLogger(getClass());
    
    public ExampleMapperImpl(MybatisMixConfiguration configuration, SqlSession sqlSession, Class<Entity> entityClass) {
        super(configuration, sqlSession, entityClass);
    }
    
    @Override
    public List<Entity> selectByExample(BaseQueryExample<Entity, ? extends BaseQueryExample> example) {
        String statementId = EntityMappedStatementNameEnum.selectByExample.getStatementId(example.getEntityClass());
        return this.getSqlSession().selectList(statementId, example);
    }
    
    @Override
    public Entity selectOneByExample(BaseQueryExample<Entity, ? extends BaseQueryExample> example) {
        List<Entity> list = selectByExample(example);
        if (list == null || list.size() <= 0) {
            return null;
        } else {
            return list.get(0);
        }
    }
    
    @Override
    public int countByExample(BaseQueryExample<Entity, ? extends BaseQueryExample> example) {
        String statementId = EntityMappedStatementNameEnum.countByExample.getStatementId(example.getEntityClass());
        return this.getSqlSession().selectOne(statementId, example);
    }
    
    @Override
    public int deleteByExample(BaseDeleteExample<Entity> example) {
        if (EntityUtils.isLogicDelete(example.getEntityClass())) {
            return TransactionRunner.forceRunInTransaction(configuration, () -> deleteLogicTableByExample(example));
        } else {
            String statementId = EntityMappedStatementNameEnum.deleteByExample.getStatementId(example.getEntityClass());
            return this.getSqlSession().delete(statementId, example);
        }
    }
    
    protected int deleteLogicTableByExample(BaseDeleteExample<Entity> example) {
        String statementId = EntityMappedStatementNameEnum.deleteOnInsertLogicTableByExample
                .getStatementId(example.getEntityClass());
        int bakRows = -1;
        try {
            bakRows = this.getSqlSession().delete(statementId, example);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        int deleteRows = this.getSqlSession()
                .delete(EntityMappedStatementNameEnum.deleteByExample.getStatementId(example.getEntityClass()),
                        example);
        if (bakRows > -2 && bakRows != deleteRows) {
            String logicDeleteTable = EntityUtils.getEntityDescriptor(entityClass).getLogicDeleteTable();
            logger.warn(logicDeleteTable + "逻辑删除异常！删除记录为" + deleteRows + "行，插入记录为" + bakRows + "行！");
        }
        return deleteRows;
    }
    
    @Override
    public int updateByExample(BaseUpdateExample example) {
        String statementId = EntityMappedStatementNameEnum.updateByExample.getStatementId(example.getEntityClass());
        return this.getSqlSession().update(statementId, example);
    }
}
