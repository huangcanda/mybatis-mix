package org.wanghailu.mybatismix.test.entity;

import org.wanghailu.mybatismix.annotation.EnableGenerateExampleClass;
import org.wanghailu.mybatismix.annotation.LogicDelete;
import org.wanghailu.mybatismix.model.EnableExactUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.time.LocalDateTime;

/**
 * @author cdhuang
 * @date 2023/1/6
 */
@Entity
@Table(name = "t_vip_user")
@LogicDelete
@EnableGenerateExampleClass
@EnableExactUpdate
public class VipUserEntity extends UserEntity {
    
    private int vipLevel;
    
    private LocalDateTime vipExpirationTime;
    
    @Transient
    private String vipDetail;
    
    @Version
    private Integer lock;
    
    public int getVipLevel() {
        return vipLevel;
    }
    
    public void setVipLevel(int vipLevel) {
        this.vipLevel = vipLevel;
    }
    
    public LocalDateTime getVipExpirationTime() {
        return vipExpirationTime;
    }
    
    public void setVipExpirationTime(LocalDateTime vipExpirationTime) {
        this.vipExpirationTime = vipExpirationTime;
    }
    
    public String getVipDetail() {
        return vipDetail;
    }
    
    public void setVipDetail(String vipDetail) {
        this.vipDetail = vipDetail;
    }
    
    public Integer getLock() {
        return lock;
    }
    
    public void setLock(Integer lock) {
        this.lock = lock;
    }
}
