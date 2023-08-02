package org.wanghailu.mybatismix.batch;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 开启mybatis的批处理
 * @author cdhuang
 * @date 2023/8/1
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface MybatisBatchExecute {
    
}
