package org.wanghailu.mybatismix.apt.model;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;

/**
 * BaseContext
 */
public class BaseContext {

    protected ProcessingEnvironment processingEnv;

    protected Messager messager;

    public BaseContext(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        this.messager = processingEnv.getMessager();
    }

    public ProcessingEnvironment getProcessingEnv() {
        return processingEnv;
    }

    public Messager getMessager() {
        return messager;
    }
}
