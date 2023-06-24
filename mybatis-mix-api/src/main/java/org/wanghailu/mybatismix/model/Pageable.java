package org.wanghailu.mybatismix.model;

/**
 * @author cdhuang
 * @date 2022/12/15
 */
public interface Pageable {
    
    /**
     * 当前页码，从1开始
     *
     * @return
     */
    long getCurrentPage();
    
    /**
     * 分页显示记录数
     *
     * @return
     */
    long getPageSize();
    
    /**
     * 写入总行数
     *
     * @param count
     */
    void setCountSize(long count);
    
    /**
     * 是否计算总行数
     *
     * @return
     */
    default boolean isSelectCount(){
        return true;
    }
    
    /**
     * 分页参数将作为sql参数进行执行（预编译模式里的？占位符）
     * @return
     */
    default boolean isSqlParamMode(){
        return true;
    }
    /**
     *  记录起始坐标
     */
    default long getOffsetStart() {
        return getOffsetStartPrev() + 1;
    }
    
    /**
     * 记录下标
     *
     * @return the recordEnd
     */
    default long getOffsetEnd() {
        if (getCurrentPage() > 0) {
            return getCurrentPage() * getPageSize();
        } else {
            return 0;
        }
    }
    
    /**
     * 记录起始坐标 -1
     * @return
     */
    default long getOffsetStartPrev() {
        if (getCurrentPage() > 0) {
            return (getCurrentPage() - 1) * getPageSize();
        } else {
            return 0;
        }
    }
}
