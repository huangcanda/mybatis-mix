package org.wanghailu.mybatismix.constant;

/**
 * @author cdhuang
 * @date 2023/4/17
 */
public interface ConfigurationStateConstant {
    /**
     * 新建状态
     */
    int NEW=1;
    /**
     * 已初始化配置相关内容
     */
    int SET_PROPERTIES=2;
    /**
     * 已初始化完成
     */
    int INITIALIZED=3;
    /**
     * 组件已关闭的状态
     */
    int CLOSED=4;

}
