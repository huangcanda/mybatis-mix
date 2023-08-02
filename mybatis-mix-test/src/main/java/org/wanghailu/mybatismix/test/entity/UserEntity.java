package org.wanghailu.mybatismix.test.entity;

import org.wanghailu.mybatismix.annotation.EnableGenerateExampleClass;
import org.wanghailu.mybatismix.annotation.FillNowOnInsert;
import org.wanghailu.mybatismix.annotation.FillNowOnUpdate;
import org.wanghailu.mybatismix.annotation.LogicDelete;
import org.wanghailu.mybatismix.model.EnableExactUpdateModel;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author cdhuang
 * @date 2023/1/6
 */
@Entity
@Table(name = "t_user")
@EnableExactUpdateModel
@LogicDelete
@EnableGenerateExampleClass
public class UserEntity {
    
    @Id
    private Long id;
    
    private String userName;
    
    private String password;
    
    private String address;
    
    private String createUser;
    
    @FillNowOnInsert
    private Date createTime;
    
    private String updateUser;
    
    @FillNowOnInsert
    @FillNowOnUpdate
    private Date updateTime;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getCreateUser() {
        return createUser;
    }
    
    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }
    
    public Date getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    
    public String getUpdateUser() {
        return updateUser;
    }
    
    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }
    
    public Date getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
