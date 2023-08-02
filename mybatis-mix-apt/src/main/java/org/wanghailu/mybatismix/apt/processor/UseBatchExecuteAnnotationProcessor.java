package org.wanghailu.mybatismix.apt.processor;


import com.sun.tools.javac.code.Symbol;
import org.wanghailu.mybatismix.apt.handler.UseBatchExecuteHandler;
import org.wanghailu.mybatismix.apt.model.JavacMethodContext;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;

/**
 * 对MybatisBatchExecute进行支持
 */
@SupportedAnnotationTypes("org.wanghailu.mybatismix.batch.MybatisBatchExecute")
public class UseBatchExecuteAnnotationProcessor extends BaseModifyTreeProcessor {
    
    @Override
    protected boolean doProcess(Symbol element, TypeElement annotation) {
        UseBatchExecuteHandler handler = new UseBatchExecuteHandler(new JavacMethodContext(context, (Symbol.MethodSymbol) element));
        handler.handle();
        return false;
    }
}
