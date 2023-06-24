package org.wanghailu.mybatismix.constant;

/**
 * @author cdhuang
 * @date 2023/4/17
 */
public interface ConfigurationStateConstant {
    /**
     * 0 = 默认为：若更新的对象实现ExactUpdateEnable则使用精准更新，否则使用更新非空的方式
     */
    int NEW=1;
    /**
     * 10 = 精准更新
     */
    int SET_PROPERTIES=2;
    /**
     * 20 = 更新非空
     */
    int INITIALIZED=3;
    /**
     * 30 = 更新全部
     */
    int CLOSED=4;

}
