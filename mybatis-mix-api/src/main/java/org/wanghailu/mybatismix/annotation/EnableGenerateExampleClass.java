package org.wanghailu.mybatismix.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 生成对应的example类
 * @author cdhuang
 * @date 2023/3/21
 */
@Documented
@Target({ TYPE})
@Retention(RUNTIME)
@Inherited
public @interface EnableGenerateExampleClass {
    
}
