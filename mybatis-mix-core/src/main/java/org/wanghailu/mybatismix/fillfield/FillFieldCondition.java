package org.wanghailu.mybatismix.fillfield;

import org.apache.ibatis.mapping.MappedStatement;
import org.wanghailu.mybatismix.support.SpiExtension;

import java.util.Collection;

/**
 * @author cdhuang
 * @date 2023/1/30
 */
public interface FillFieldCondition extends SpiExtension {

    /**
     * 通过条件过滤出来的单个需要填充的对象
     * @param ms
     * @param parameter
     * @return
     */
    Object getEntity(MappedStatement ms, Object parameter,Object result);
    
    /**
     * 通过条件过滤出来的多个需要填充的对象
     * @param ms
     * @param parameter
     * @return
     */
     Collection getEntities(MappedStatement ms, Object parameter,Object result);


}
