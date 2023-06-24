package org.wanghailu.mybatismix.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * 自动添加Comment注解
 */
@Documented
@Target({ TYPE})
@Retention(RUNTIME)
public @interface AutoComment {

    Class<? extends Annotation> value() default Comment.class;

    String commentFieldName() default "value";
}
