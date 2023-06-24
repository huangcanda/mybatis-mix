package org.wanghailu.mybatismix.apt.model;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;

/**
 * @author cdhuang
 * @date 2021/11/19
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
