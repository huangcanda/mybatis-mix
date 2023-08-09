package org.wanghailu.mybatismix.apt.processor;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.Writer;
import java.util.Set;

/**
 * @author cdhuang
 * @date 2023/8/8
 */
public abstract class BaseGeneratorAnnotationProcessor extends BaseAnnotationProcessor {
    
    boolean generated = false;
    
    boolean isBscMavenPlugin =false;
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }
        if(generated){
            return false;
        }
        generated = true;
        isBscMavenPlugin = checkBscMavenPlugin();
        return doProcess(annotations,roundEnv);
    }
    
    protected abstract boolean doProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv);
    
    protected void generateJavaFile(String className, String code) {
        if(isBscMavenPlugin){
            return;
        }
        try {
            JavaFileObject sourceFile = filer.createSourceFile(className);
            try(Writer writer = sourceFile.openWriter()){
                writer.write(code);
                writer.flush();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
    }
    
    private boolean checkBscMavenPlugin(){
        StackTraceElement[] stackTraceElements =Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            if(stackTraceElement.getClassName().startsWith("org.bsc.maven.plugin.processor")){
                return true;
            }
        }
        return false;
    }
    
}
