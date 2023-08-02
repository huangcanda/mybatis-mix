package org.wanghailu.mybatismix.model;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 使用该注解后，更新时会使用ExactUpdate模式进行字段精准更新
 */
@Documented
@Target({ TYPE})
@Retention(RUNTIME)
@Inherited
public @interface EnableExactUpdateModel {
}
