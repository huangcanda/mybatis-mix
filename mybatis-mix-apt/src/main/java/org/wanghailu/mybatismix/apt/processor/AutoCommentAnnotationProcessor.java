package org.wanghailu.mybatismix.apt.processor;

import com.sun.tools.javac.code.Symbol;
import org.wanghailu.mybatismix.apt.handler.AddCommentAnnotationHandler;
import org.wanghailu.mybatismix.apt.model.JavacClassContext;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;

/**
 * javac编译插件，自动生成注释的注解@Comment(或自定义的注释的注解)
 * 即用于在程序运行时获得类或属性或者方法的注释信息，通过获得类，属性，方法上的@Comment注解(或自定义注解)
 *
 * @author cdhuang
 * @date 2020/8/3
 */

@SupportedAnnotationTypes({"org.wanghailu.mybatismix.model.annotation.AutoComment"})
public class AutoCommentAnnotationProcessor extends BaseModifyTreeProcessor {
    
    @Override
    protected boolean doProcess(Symbol element, TypeElement annotation) {
        JavacClassContext javacClassContext = new JavacClassContext(context, (Symbol.ClassSymbol) element);
        AddCommentAnnotationHandler handler = new AddCommentAnnotationHandler(javacClassContext);
        handler.handle();
        return false;
    }
}
