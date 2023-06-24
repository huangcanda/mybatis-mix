package org.wanghailu.mybatismix.test.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.wanghailu.mybatismix.annotation.JpaQuery;
import org.wanghailu.mybatismix.mapper.IBaseMapper;
import org.wanghailu.mybatismix.test.entity.UserEntity;

import java.util.List;

/**
 * @author cdhuang
 * @date 2023/1/17
 */
@Mapper
public interface UserMapper extends IBaseMapper<UserEntity> {
    
    @JpaQuery
    List<UserEntity> findDistinctUserNameByUserNameAndAddressOrderByUserNameAsc(@Param("name") String userName, String address);
    
    @JpaQuery
    List<UserEntity> findTop30ByUserNameStartsWith(String userName);
    
    @JpaQuery(UserEntity.class)
    int updateNotNullById(UserEntity entity,Long id);
    
    @JpaQuery(UserEntity.class)
    int deleteById(Long id);
    
    @JpaQuery(UserEntity.class)
    int countByUserNameContainsAndPasswordIn(String userName,List<String> passwords);
}
