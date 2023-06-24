package org.wanghailu.mybatismix.constant;

/**
 * 更新策略
 * @author cdhuang
 * @date 2022/12/21
 */
public enum UpdateModeEnum {
    /**
     * 0 = 默认为：若更新的对象实现ExactUpdateEnable则使用精准更新，否则使用更新非空的方式
     */
    DEFAULT(0),
    /**
     * 10 = 精准更新
     */
    EXACT(10),
    /**
     * 20 = 更新非空
     */
    NOT_NULL(20),
    /**
     * 30 = 更新全部
     */
    ALL(30);
    
    private int value;
    
    UpdateModeEnum(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
}
