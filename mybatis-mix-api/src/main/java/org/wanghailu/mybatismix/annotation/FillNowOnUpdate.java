package org.wanghailu.mybatismix.annotation;

/**
 * 更方便的使用FillField注解，更新时对字段写入当前时间
 * @author cdhuang
 * @date 2023/1/30
 */
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target({FIELD,METHOD})
@Retention(RUNTIME)
@FillField(strategy = "now",condition = "update")
public @interface FillNowOnUpdate {

}