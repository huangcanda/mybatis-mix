package org.wanghailu.mybatismix.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 使用注解重新定义顺序，方便对框架进行扩展
 * @author cdhuang
 * @date 2022/12/28
 */
@Documented
@Target({ TYPE})
@Retention(RUNTIME)
@Inherited
public @interface OrderedItem {
    
    /**
     * 值越小，优先级越高
     * @return
     */
    int value() default -10000;
}
