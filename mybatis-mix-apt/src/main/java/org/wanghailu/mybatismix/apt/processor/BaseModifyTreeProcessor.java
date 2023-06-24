package org.wanghailu.mybatismix.apt.processor;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import org.wanghailu.mybatismix.apt.model.JavacContext;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

/**
 * 需要修改类的语法树的处理器，继承该类，暂时仅支持javac
 * @author cdhuang
 * @date 2021/11/30
 */
public abstract class BaseModifyTreeProcessor extends BaseAnnotationProcessor {

    protected boolean process = false;
    
    protected int processCount = 0;
    
    protected JavacContext context;
    
    protected TreeMaker treeMaker;
    
    protected Names names;
    
    protected JavacTrees javacTrees;
    
    protected ReEnterSymbolProcessor reEnterSymbolProcessor;
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        this.roundEnv = roundEnv;
        if (processingEnv instanceof JavacProcessingEnvironment == false) {
            System.out.println(
                    this.getClass().getSimpleName() + " only support javac compiler. Your processor is:" + processingEnv
                            .getClass().getName());
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    this.getClass().getSimpleName() + " only support javac compiler. Your processor is:" + processingEnv
                            .getClass().getName());
            return false;
        }
        context = new JavacContext(processingEnv);
        treeMaker = context.getTreeMaker();
        names = context.getNames();
        javacTrees = context.getJavacTrees();
        
        if (reEnterSymbolProcessor == null) {
            reEnterSymbolProcessor = new ReEnterSymbolProcessor(this);
            reEnterSymbolProcessor.saveContext(annotations, roundEnv);
        }
        processCount++;
        if (processOnReEnter() && reEnterSymbolProcessor.haveLombok() && processCount < 2) {
            return false;
        }
        if (process) {
            return false;
        }
        process = true;
        try {
            return reEnterSymbolProcessor.doInvoke();
        } catch (Throwable e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.valueOf(e.getMessage()));
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getStackTrace()[0].toString());
            process = false;
        }
        
        return false;
    }
    
    protected abstract boolean doProcess(Symbol element, TypeElement annotation);
    

    
    protected boolean processOnReEnter(){
        return false;
    }
}
