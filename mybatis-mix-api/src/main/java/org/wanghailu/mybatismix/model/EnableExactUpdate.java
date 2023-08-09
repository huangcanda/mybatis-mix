package org.wanghailu.mybatismix.model;

import org.wanghailu.mybatismix.constant.ExactUpdateTypeEnum;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 使用该注解后，更新时会使用ExactUpdate模式进行字段精准更新
 * @author cdhuang
 */
@Documented
@Target({ TYPE})
@Retention(RUNTIME)
public @interface EnableExactUpdate {
    
    /**
     * 定义自动继承的类
     * @return
     */
    ExactUpdateTypeEnum exactUpdateType() default ExactUpdateTypeEnum.DEFAULT;
}
