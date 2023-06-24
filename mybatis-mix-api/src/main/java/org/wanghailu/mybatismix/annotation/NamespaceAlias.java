package org.wanghailu.mybatismix.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 在实体类上使用，重新定义实体类对应的Mapper的namespace的别名
 * @author cdhuang
 * @date 2022/12/27
 */
@Documented
@Target({TYPE})
@Retention(RUNTIME)
@Inherited
public @interface NamespaceAlias {
    String value();
}
