package org.wanghailu.mybatismix.support;

/**
 * 重新定义优先级
 * @author cdhuang
 * @date 2023/4/23
 */
public interface OrderItem {
    
    /**
     * 扩展的优先级定义，值越小优先级越高
     * @return
     */
    default int getOrder() {
        return -1000;
    }
}
