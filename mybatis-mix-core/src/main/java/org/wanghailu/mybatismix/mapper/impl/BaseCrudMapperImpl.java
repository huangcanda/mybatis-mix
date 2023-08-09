package org.wanghailu.mybatismix.mapper.impl;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.constant.UpdateModeEnum;
import org.wanghailu.mybatismix.mapper.IBaseCrudMapper;
import org.wanghailu.mybatismix.mapping.EntityMappedStatementNameEnum;
import org.wanghailu.mybatismix.model.ExactUpdateEnable;
import org.wanghailu.mybatismix.transaction.TransactionRunner;
import org.wanghailu.mybatismix.util.BeanInvokeUtils;
import org.wanghailu.mybatismix.util.EntityUtils;
import org.wanghailu.mybatismix.util.TruckUtils;

import javax.persistence.OptimisticLockException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 基础增删改查的实现
 *
 * @author cdhuang
 * @date 2022/12/29
 */
public class BaseCrudMapperImpl<Entity> extends BaseMapperImpl<Entity> implements IBaseCrudMapper<Entity> {
    
    protected Logger logger = LoggerFactory.getLogger(getClass());
    
    private static int defaultSelectInListSize = 800;
    
    public BaseCrudMapperImpl(MybatisMixConfiguration configuration, SqlSession sqlSession, Class<Entity> entityClass) {
        super(configuration, sqlSession, entityClass);
    }
    
    @Override
    public int insert(Entity entity) {
        String statementId = EntityMappedStatementNameEnum.insert.getStatementId(entityClass);
        int result = this.getSqlSession().insert(statementId, entity);
        if (result == 0) {
            logger.warn(EntityUtils.getTableName(entity.getClass()) + "，新增异常，没有被影响的数据！");
        } else {
            if (entity instanceof ExactUpdateEnable) {
                ((ExactUpdateEnable) entity).updateFieldsClear();
            }
        }
        return result;
    }
    
    
    @Override
    public int insertList(List<Entity> entities) {
        if (entities == null || entities.isEmpty()) {
            return 0;
        }
        String statementId = EntityMappedStatementNameEnum.insertList.getStatementId(entityClass);
        return this.getSqlSession().insert(statementId, buildMap("list", entities));
    }
    
    @Override
    public List<Entity> selectAll() {
        String statementId = EntityMappedStatementNameEnum.selectAll.getStatementId(entityClass);
        return this.getSqlSession().selectList(statementId);
    }
    
    @Override
    public Entity selectById(Serializable primaryKeyValue) {
        if (TruckUtils.isEmptyPrimaryKey(primaryKeyValue)) {
            logger.warn("selectByPrimaryKey,参数primaryKeyValue为空");
            return null;
        }
        String statementId = EntityMappedStatementNameEnum.selectById.getStatementId(entityClass);
        return this.getSqlSession().selectOne(statementId, buildMap("primaryKey", primaryKeyValue));
    }
    
    @Override
    public List<Entity> selectListByIds(Serializable... primaryKeyValues) {
        if (primaryKeyValues.length == 1) {
            return Arrays.asList(selectById(primaryKeyValues[0]));
        }
        List<Entity> resultList = new ArrayList<>();
        List<Entity> selectAllList = new ArrayList<>();
        List<Serializable>[] listArray = TruckUtils
                .averageSplit(Arrays.asList(primaryKeyValues), defaultSelectInListSize);
        for (List<Serializable> primaryKeyList : listArray) {
            Map<String, Serializable> primaryKeyMap = new LinkedHashMap<>();
            int i = 1;
            for (Serializable primaryKeyValue : primaryKeyList) {
                if (TruckUtils.isEmptyPrimaryKey(primaryKeyValue)) {
                    logger.warn("selectListByPrimaryKey,参数primaryKeyValueList中第" + i + "个主键为空");
                    continue;
                }
                primaryKeyMap.put("primaryKey" + i, primaryKeyValue);
                i++;
            }
            String statementId = EntityMappedStatementNameEnum.selectListByIds.getStatementId(entityClass);
            List<Entity> selectList = this.getSqlSession().selectList(statementId, primaryKeyMap);
            selectAllList.addAll(selectList);
        }
        Map<Serializable, Entity> entityMap = new HashMap<>(selectAllList.size());
        for (Entity entity : selectAllList) {
            entityMap.put(BeanInvokeUtils.getPrimaryKeyValue(entity), entity);
        }
        for (Serializable primaryKeyValue : primaryKeyValues) {
            resultList.add(entityMap.get(primaryKeyValue));
        }
        return resultList;
    }
    
    @Override
    public int update(Entity entity) {
        return update(entity, UpdateModeEnum.DEFAULT);
    }
    
    @Override
    public int update(Entity entity, UpdateModeEnum updateMode) {
        if (entity == null) {
            logger.error("对象为null，无法更新！");
            return 0;
        }
        if (entity instanceof ExactUpdateEnable) {
            ExactUpdateEnable exactUpdateEntity = (ExactUpdateEnable) entity;
            if (exactUpdateEntity.updateFieldsSelect().size() == 0) {
                logger.warn("表" + EntityUtils.getTableName(entity.getClass()) + "，主键为" + BeanInvokeUtils
                        .getPrimaryKeyValue(entity) + "的数据更新异常，数据没有被修改，无需更新！");
                return 0;
            }
            int result = realDoUpdateByModel(entity, updateMode);
            if (result > 0) {
                exactUpdateEntity.updateFieldsClear();
            }
            return result;
        } else {
            return realDoUpdateByModel(entity, updateMode);
        }
    }
    
    protected int realDoUpdateByModel(Entity entity, UpdateModeEnum updateMode) {
        String statementId = EntityMappedStatementNameEnum.update.getStatementId(entityClass);
        int result = this.getSqlSession()
                .update(statementId, buildMap("setEntity", entity, "updateMode", updateMode.getValue()));
        if (result == 0) {
            //没有影响到数据，并且实体具有版本字段，抛出乐观锁异常
            if (EntityUtils.getVersionFieldName(entity.getClass()) != null) {
                throw new OptimisticLockException(
                        "表" + EntityUtils.getTableName(entity.getClass()) + "，id为" + BeanInvokeUtils
                                .getPrimaryKeyValue(entity) + "的数据出现乐观锁异常，数据被修改、删除 或 未保存！");
            }
            logger.warn("表" + EntityUtils.getTableName(entity.getClass()) + "，id为" + BeanInvokeUtils
                    .getPrimaryKeyValue(entity) + "的数据，更新异常，没有被影响的数据！");
        }
        return result;
    }
    
    @Override
    public int delete(Entity entity) {
        Map<String, Object> paramMap = buildMap("primaryKey", BeanInvokeUtils.getPrimaryKeyValue(entity));
        if (EntityUtils.isLogicDelete(entityClass)) {
            return TransactionRunner.forceRunInTransaction(configuration, () -> deleteLogicTable(paramMap));
        } else {
            return this.getSqlSession()
                    .delete(EntityMappedStatementNameEnum.delete.getStatementId(entityClass), paramMap);
        }
    }
    
    protected int deleteLogicTable(Map<String, Object> paramMap) {
        String logicDeleteTable = EntityUtils.getEntityDescriptor(entityClass).getLogicDeleteTable();
        int result = 0;
        String statementId = EntityMappedStatementNameEnum.deleteOnInsertLogicTable.getStatementId(entityClass);
        try {
            result = this.getSqlSession().delete(statementId, paramMap);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        if (result == 0) {
            logger.warn(logicDeleteTable + "逻辑删除表写入异常！");
        }
        return this.getSqlSession().delete(EntityMappedStatementNameEnum.delete.getStatementId(entityClass), paramMap);
    }
    
    @Override
    public int insertOrUpdate(Entity entity) {
        Serializable primaryKeyValue = BeanInvokeUtils.getPrimaryKeyValue(entity);
        if (TruckUtils.isEmpty(primaryKeyValue)) {
            //主键为空，默认为新增
            return insert(entity);
        } else {
            return update(entity);
        }
    }

}
