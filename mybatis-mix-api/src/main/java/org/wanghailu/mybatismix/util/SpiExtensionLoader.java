package org.wanghailu.mybatismix.util;

import org.wanghailu.mybatismix.support.SpiExtension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * 为jdk自带的spi提供排序功能，定义优先级，便于扩展和重写
 *
 * @author cdhuang
 * @date 2022/12/28
 */
public class SpiExtensionLoader<S extends SpiExtension> implements Iterable<S> {
    
    
    public static <S extends SpiExtension> SpiExtensionLoader<S> load(Class<S> service) {
        ServiceLoader<S> serviceLoader = ServiceLoader.load(service);
        List<S> list = new ArrayList<>();
        for (S s : serviceLoader) {
            if(s.agreeLoad()){
                list.add(s);
            }
        }
        TruckUtils.listSort(list);
        return new SpiExtensionLoader<>(list);
    }

    public static <S extends SpiExtension> Map<String,S> loadSpiExtensionMap(Class<S> service){
        Map<String,S> resultMap = new LinkedHashMap<>();
        SpiExtensionLoader<S> spiExtensionLoader = load(service);
        for (S s : spiExtensionLoader) {
            resultMap.putIfAbsent(s.name(),s);
        }
        return resultMap;
    }
    
    private Collection collection;
    
    public SpiExtensionLoader(Collection collection) {
        this.collection = collection;
    }
    
    @Override
    public Iterator<S> iterator() {
        return collection.iterator();
    }
    
}
