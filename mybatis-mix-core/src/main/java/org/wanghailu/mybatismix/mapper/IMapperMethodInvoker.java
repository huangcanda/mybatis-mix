package org.wanghailu.mybatismix.mapper;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public interface IMapperMethodInvoker {
    
    /**
     * 重写方法执行动作
     *
     * @param proxy
     * @param method
     * @param args
     * @param superInvoke
     * @return
     * @throws Throwable
     */
    Object doMethodInvoke(Object proxy, Method method, Object[] args, Callable superInvoke) throws Throwable;
    
}
