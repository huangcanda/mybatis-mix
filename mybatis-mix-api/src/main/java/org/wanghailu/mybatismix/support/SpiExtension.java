package org.wanghailu.mybatismix.support;

/**
 * 使用接口重新定义spi加载的顺序，方便对框架进行扩展
 * @author cdhuang
 * @date 2022/12/28
 */
public interface SpiExtension extends OrderItem{
    
    /**
     * spi拓展的名字，name相同，则使用优先级高的实现
     * @return
     */
    String name();
    
    
    
    /**
     * spi扩展的优先级定义，值越小优先级越高
     * @return
     */
    default boolean agreeLoad() {
        return true;
    }
}
