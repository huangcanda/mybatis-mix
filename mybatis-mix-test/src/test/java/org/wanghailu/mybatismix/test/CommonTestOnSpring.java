package org.wanghailu.mybatismix.test;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wanghailu.mybatismix.batch.MybatisBatchExecute;
import org.wanghailu.mybatismix.constant.UpdateModeEnum;
import org.wanghailu.mybatismix.example.lambda.LambdaDeleteExample;
import org.wanghailu.mybatismix.example.lambda.LambdaQueryExample;
import org.wanghailu.mybatismix.example.lambda.LambdaUpdateExample;
import org.wanghailu.mybatismix.example.simple.SimpleDeleteExample;
import org.wanghailu.mybatismix.example.simple.SimpleQueryExample;
import org.wanghailu.mybatismix.example.simple.SimpleUpdateExample;
import org.wanghailu.mybatismix.mapper.IBaseCrudMapper;
import org.wanghailu.mybatismix.mapper.ICrudMapper;
import org.wanghailu.mybatismix.mapper.IExampleMapper;
import org.wanghailu.mybatismix.mapper.MapperManager;
import org.wanghailu.mybatismix.model.Page;
import org.wanghailu.mybatismix.page.PageHelper;
import org.wanghailu.mybatismix.test.base.BaseTestOnSpring;
import org.wanghailu.mybatismix.test.entity.UserEntity;
import org.wanghailu.mybatismix.test.entity.VipUserEntity;
import org.wanghailu.mybatismix.test.entity.example.UserEntityDeleteExample;
import org.wanghailu.mybatismix.test.entity.example.UserEntityQueryExample;
import org.wanghailu.mybatismix.test.entity.example.UserEntityUpdateExample;
import org.wanghailu.mybatismix.test.mapper.UserMapper;
import org.wanghailu.mybatismix.util.SpringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * @author cdhuang
 * @date 2023/1/11
 */
public class CommonTestOnSpring extends BaseTestOnSpring {
    
    @Autowired
    private MapperManager autoMapperManager;
    
    @Autowired
    private UserMapper userMapper;
    @Test
    @MybatisBatchExecute
    public void crudTest() {
        String[] beanNamesForType= SpringUtils.getBeanFactory().getBeanNamesForType(ICrudMapper.class);
        System.out.println(beanNamesForType);
        
        IBaseCrudMapper<UserEntity> crudMapper = autoMapperManager
                .getAutoMapper(IBaseCrudMapper.class, UserEntity.class);
        UserEntity userEntity = getNewEntity();
        crudMapper.insert(userEntity);
        userEntity = getNewEntity();
        crudMapper.insert(userEntity);
        userEntity = getNewEntity();
        crudMapper.insert(userEntity);
        userEntity.setPassword("111111");
        crudMapper.update(userEntity);
        UserEntity entity2 = crudMapper.selectById(userEntity.getId());
        System.out.println(entity2.getAddress());
        System.out.println(entity2.getPassword());
        
        entity2 = crudMapper.selectById(1000);
        System.out.println(entity2);
        
        PageHelper.setLocalPage(new Page(1, 5));
        List<UserEntity> list = crudMapper.selectAll();
        System.out.println(list);

        IBaseCrudMapper<UserEntity> crudMapper2 = autoMapperManager
                .getAutoMapper(IBaseCrudMapper.class, VipUserEntity.class);
        VipUserEntity vipUserEntity = getNewVipUserEntity();
        crudMapper2.insert(vipUserEntity);
        vipUserEntity.setVipExpirationTime(LocalDateTime.now());
        crudMapper2.update(vipUserEntity);
        
        ICrudMapper<UserEntity> batchCrudMapper = autoMapperManager.getAutoMapper(ICrudMapper.class, UserEntity.class);
        batchCrudMapper.batchInsert(Arrays.asList(getNewEntity(), getNewEntity(), getNewEntity()));
        batchCrudMapper.batchInsertList(Arrays.asList(getNewEntity(), getNewEntity(), getNewEntity()));
        crudMapper.delete(userEntity);
    }
    
    @Test
    public void jpaQueryTest() {
        List<UserEntity> list22 = userMapper.findDistinctUserNameByUserNameAndAddressOrderByUserNameAsc("admin", "111111");
        System.out.println(list22);
    
        list22 = userMapper.findTop30ByUserNameStartsWith("admin");
        Assert.assertEquals(list22.size(),30);
    
        int count = userMapper.countByUserNameContainsAndPasswordIn("admin",Arrays.asList("1111111","1111112"));
        Assert.assertEquals(count,2);
        
        UserEntity userEntity = getNewEntity();
        userMapper.insert(userEntity);
        Assert.assertEquals(userMapper.updateNotNullById(userEntity,userEntity.getId()),1);
        Assert.assertEquals(userMapper.deleteById(userEntity.getId()),1);
    }
        
        @Test
    public void simpleExampleTest() {
        IExampleMapper<UserEntity> exampleMapper = autoMapperManager
                .getAutoMapper(IExampleMapper.class, UserEntity.class);
        
        SimpleQueryExample<UserEntity> queryExample = SimpleQueryExample.from(UserEntity.class).select("userName")
                .select("id,address").where(x -> x.eq("userName", "111").isNotNull("password"))
                .groupBy("address,userName").groupBy("password").orderByDesc("userName,address").orderByAsc("password");
        List<UserEntity> list = exampleMapper.selectByExample(queryExample);
        System.out.println(list.size());
        
        queryExample.clear();
        queryExample.where(x -> x.isNotNull("id").isNotNull("userName"));
        System.out.println(exampleMapper.countByExample(queryExample));
        
        UserEntity userEntity = getNewEntity();
        SimpleUpdateExample<UserEntity> updateExample = SimpleUpdateExample.from(UserEntity.class).set(userEntity)
                .updateModel(UpdateModeEnum.ALL.getValue())
                .where(x -> x.eq("address", "123").or(y -> y.isNull("createTime").eq("userName", "123456")));
        exampleMapper.updateByExample(updateExample);
        
        SimpleDeleteExample<UserEntity> deleteExample = SimpleDeleteExample.from(UserEntity.class)
                .where(x -> x.eq("address", "123").or(y -> y.isNull("createTime").ifArgNotEmpty().eq("userName", "").endIf()));
        exampleMapper.deleteByExample(deleteExample);
    }
    
    @Test
    public void lambdaExampleTest() {
        IExampleMapper<UserEntity> exampleMapper = autoMapperManager
                .getAutoMapper(IExampleMapper.class, UserEntity.class);
        
        LambdaQueryExample<UserEntity> queryExample = LambdaQueryExample.from(UserEntity.class)
                .select(UserEntity::getAddress).select(UserEntity::getUserName, UserEntity::getPassword)
                .where(x -> x.eq(UserEntity::getUserName, "111").isNotNull(UserEntity::getPassword))
                .groupBy(UserEntity::getAddress, UserEntity::getUserName).groupBy(UserEntity::getPassword)
                .orderByDesc(UserEntity::getUserName, UserEntity::getAddress).orderByAsc(UserEntity::getPassword);
        List<UserEntity> list = exampleMapper.selectByExample(queryExample);
        System.out.println(list.size());
        
        queryExample.clear();
        queryExample.where(x -> x.isNotNull(UserEntity::getId).isNotNull(UserEntity::getUserName));
        System.out.println(exampleMapper.countByExample(queryExample));
        
        UserEntity userEntity = getNewEntity();
        LambdaUpdateExample<UserEntity> updateExample = LambdaUpdateExample.from(UserEntity.class).set(userEntity)
                .updateModel(UpdateModeEnum.ALL.getValue()).where(x -> x.eq(UserEntity::getAddress, "123")
                        .or(y -> y.isNull(UserEntity::getCreateTime).eq(UserEntity::getUserName, "123456")));
        exampleMapper.updateByExample(updateExample);
        
        LambdaDeleteExample<UserEntity> deleteExample = LambdaDeleteExample.from(UserEntity.class)
                .where(x -> x.eq(UserEntity::getAddress, "123")
                        .or(y -> y.isNull(UserEntity::getCreateTime).ifArgNotEmpty().eq(UserEntity::getUserName, "").eq(UserEntity::getAddress,"").endIf()));
        exampleMapper.deleteByExample(deleteExample);
    }
    
    @Test
    public void typeExampleTest() {
        IExampleMapper<UserEntity> exampleMapper = autoMapperManager
                .getAutoMapper(IExampleMapper.class, UserEntity.class);
    
    
        UserEntityQueryExample queryExample = UserEntityQueryExample.fromUserEntity()
                .select().address().userName().password().endSelect()
                .where(x -> x.userName.eq("111").password.isNotNull())
                .groupByAddress().groupByUserName().groupByPassword()
                .orderByUserNameDesc().orderByAddressDesc().orderByPasswordAsc();
        List<UserEntity> list = exampleMapper.selectByExample(queryExample);
        System.out.println(list.size());
    
        queryExample.clear();
        queryExample.where(x -> x.id.isNotNull().userName.isNotNull());
        System.out.println(exampleMapper.countByExample(queryExample));
    
        UserEntity userEntity = getNewEntity();
        UserEntityUpdateExample updateExample = UserEntityUpdateExample.fromUserEntity().set(userEntity)
                .updateModel(UpdateModeEnum.ALL.getValue()).where(x -> x.address.eq("123")
                        .or(y -> y.createTime.isNull().userName.eq("123456")));
        exampleMapper.updateByExample(updateExample);
    
        UserEntityDeleteExample deleteExample = UserEntityDeleteExample.fromUserEntity()
                .where(x -> x.ifCondition(false).id.eq("1").userName.isNull().endIf().userName.isNull()
                        .or(y -> y.ifArgNotEmpty().id.eq("1").endIf().id.isNull()));
        exampleMapper.deleteByExample(deleteExample);
    }
    

    
    private int index = 0;

    protected VipUserEntity getNewVipUserEntity() {
        VipUserEntity userEntity = new VipUserEntity();
        userEntity.setUserName("mybatisMix-" + index);
        userEntity.setAddress("软件园二期望海路" + index + "号楼");
        userEntity.setCreateUser("system");
        userEntity.setVipLevel(5);
        userEntity.setVipDetail("1111111111111111~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~``");
        return userEntity;
    }
    
    protected UserEntity getNewEntity() {
        UserEntity userEntity = new UserEntity();
        userEntity.setUserName("mybatisMix-" + index);
        userEntity.setAddress("软件园二期望海路" + index + "号楼");
        userEntity.setCreateUser("system");
        return userEntity;
    }
}
