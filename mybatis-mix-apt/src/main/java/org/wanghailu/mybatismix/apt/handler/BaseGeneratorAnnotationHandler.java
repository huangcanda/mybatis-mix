package org.wanghailu.mybatismix.apt.handler;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * @author cdhuang
 * @date 2023/8/8
 */
public abstract class BaseGeneratorAnnotationHandler {
    
    protected Messager messager;
    
    protected Filer filer;
    
    protected Elements elementUtils;
    
    protected Types typeUtils;
    
    public synchronized void init(ProcessingEnvironment processingEnv) {
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
    }
    
    
    
}
