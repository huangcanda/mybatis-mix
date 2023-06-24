package org.wanghailu.mybatismix.support;

/**
 * @author cdhuang
 * @date 2023/4/4
 */
public interface NamedSpiExtension extends SpiExtension {
    
    /**
     * 直接以class名字作为name
     * @return
     */
    @Override
    default String name() {
        return this.getClass().getSimpleName();
    }
}
