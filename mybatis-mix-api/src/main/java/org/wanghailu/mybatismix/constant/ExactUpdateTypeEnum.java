package org.wanghailu.mybatismix.constant;

/**
 * @author cdhuang
 * @date 2023/8/4
 */
public enum ExactUpdateTypeEnum {
    
    /**
     * 根据代码分析，自动推断继承哪一个类，对于跨模块编译的父子继承的情况，没法自动推断，需手动推断
     */
    AUTO,
    /**
     * 自动继承BaseExactUpdateRecord
     */
    DEFAULT,
    /**
     * 自动继承BaseExactUpdateRecordViewable
     */
    SIMPLIFIED,
    /**
     * 自动继承BaseExactUpdateRecordViewable
     */
    VIEWABLE;
}
