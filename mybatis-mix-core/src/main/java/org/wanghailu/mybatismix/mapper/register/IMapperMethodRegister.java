package org.wanghailu.mybatismix.mapper.register;

import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.mapper.IMapperMethodInvoker;
import org.wanghailu.mybatismix.support.NamedSpiExtension;

import java.lang.reflect.Method;

public interface IMapperMethodRegister extends NamedSpiExtension {
    
    IMapperMethodInvoker supportMethod(Class mapperClass, Method method, MybatisMixConfiguration configuration);
}
