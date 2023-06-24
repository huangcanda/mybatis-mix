package org.wanghailu.mybatismix.apt.processor;

import com.sun.tools.javac.code.Symbol;
import org.wanghailu.mybatismix.apt.handler.AddUpdateFieldsHandler;
import org.wanghailu.mybatismix.apt.model.JavacClassContext;
import org.wanghailu.mybatismix.util.ExceptionUtils;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * @author cdhuang
 * @date 2021/12/17
 */
@SupportedAnnotationTypes("org.wanghailu.mybatismix.annotation.EnableExactUpdateModel")
public class AutoUpdateFieldsAnnotationProcessor extends BaseModifyTreeProcessor {
    
    @Override
    protected boolean doProcess(Symbol element, TypeElement annotation) {
        JavacClassContext javacClassContext = new JavacClassContext(context, (Symbol.ClassSymbol) element);
        try{
            javacClassContext.attribute();
        }catch (Throwable e){
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, ExceptionUtils.exceptionToString(e));
        }
        AddUpdateFieldsHandler handler = new AddUpdateFieldsHandler(javacClassContext);
        handler.handle();
        return false;
    }
    
    @Override
    protected boolean processOnReEnter() {
        return true;
    }
}
