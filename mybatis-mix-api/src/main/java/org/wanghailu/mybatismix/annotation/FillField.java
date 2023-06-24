package org.wanghailu.mybatismix.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 字段填充注解，定义填充条件场景以及对应策略
 * @author cdhuang
 * @date 2022/12/28
 */
@Documented
@Target({FIELD,METHOD,TYPE})
@Retention(RUNTIME)
@Repeatable(FillField.List.class)
public @interface FillField {
    
    String condition();
    
    String strategy();
    
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Target({FIELD,METHOD,TYPE})
    @interface List {
        FillField[] value();
    }
}
