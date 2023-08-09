package org.wanghailu.mybatismix.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.wanghailu.mybatismix.batch.BatchExecuteTemplateBinder;
import org.wanghailu.mybatismix.batch.MybatisBatchExecute;
import org.wanghailu.mybatismix.test.entity.UserEntity;
import org.wanghailu.mybatismix.test.mapper.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cdhuang
 * @date 2023/8/1
 */
public class ExecutorTest {
    
    @Autowired
    private UserMapper userMapper;
    
    public List<UserEntity> setUserAndGetUser(List<UserEntity> userEntityList) {
        return BatchExecuteTemplateBinder.getTemplate()
                .executeOnBatchMode(context -> setUserAndGetUser0(userEntityList));
    }
    
    public List<UserEntity> setUserAndGetUser0(List<UserEntity> userEntityList) {
        int index = 0;
        for (UserEntity userEntity : userEntityList) {
            index += userMapper.insert(userEntity);
        }
        if (index == 0) {
            return userEntityList;
        }
        List<Long> ids = userEntityList.stream().map(UserEntity::getId).collect(Collectors.toList());
        Long[] array = ids.toArray(new Long[0]);
        return userMapper.selectListByIds(array);
    }
    
    @MybatisBatchExecute
    public void setUserMapper() {
    
    }
    
    public void setUserMapper3() {
        BatchExecuteTemplateBinder.getTemplate().executeOnBatchMode(context -> {
            setUserMapper3();
            return null;
        });
    }
    
    
    @MybatisBatchExecute
    public int setUserMapper2(int count) {
        return count;
    }
}
