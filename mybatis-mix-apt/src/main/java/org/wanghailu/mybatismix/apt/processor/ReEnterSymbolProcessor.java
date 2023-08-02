package org.wanghailu.mybatismix.apt.processor;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Lombok修改语法树后，需要进行重新填充符号表 所以在编译处理的第二轮，获得重新填充的符号表后再进行处理
 */
public class ReEnterSymbolProcessor {
    
    private BaseModifyTreeProcessor targetProcessor;
    
    private List<SymbolContext> symbolContexts = new ArrayList<>();
    
    public ReEnterSymbolProcessor(BaseModifyTreeProcessor targetProcessor) {
        this.targetProcessor = targetProcessor;
    }

    /**
     * 保存上下文，方便在第二轮找到
     * @param annotations
     * @param roundEnv
     */
    public void saveContext(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
            for (Element element : elements) {
                JCTree.JCCompilationUnit compilationUnit = (JCTree.JCCompilationUnit) targetProcessor.javacTrees
                        .getPath(element).getCompilationUnit();
                
                symbolContexts.add(new SymbolContext((Symbol) element, annotation, compilationUnit));
            }
        }
    }
    
    public boolean doInvoke() {
        for (SymbolContext context : symbolContexts) {
            Symbol symbol = targetProcessor.processOnReEnter() ? context.getReEnterSymbol() : context.targetSymbol;
            targetProcessor.doProcess(symbol, context.annotationSymbol);
        }
        return false;
    }
    
    
    private Boolean haveLombok = null;
    
    public boolean haveLombok() {
        if (haveLombok != null) {
            return haveLombok;
        }
        
        Set<? extends Element> elements = targetProcessor.roundEnv.getRootElements();
        for (Element element : elements) {
            if (element instanceof Symbol.ClassSymbol) {
                Symbol.ClassSymbol classSymbol = (Symbol.ClassSymbol) element;
                JCTree.JCClassDecl classDecl = targetProcessor.javacTrees.getTree(classSymbol);
                if (haveLombok(classDecl)) {
                    haveLombok = true;
                    return true;
                }
            }
        }
        haveLombok = false;
        return false;
    }
    
    
    private boolean haveLombok(JCTree.JCClassDecl classDecl) {
        if (haveLombok(classDecl.mods)) {
            return true;
        }
        for (JCTree def : classDecl.defs) {
            if (def instanceof JCTree.JCVariableDecl) {
                JCTree.JCVariableDecl jcVariableDecl = (JCTree.JCVariableDecl) def;
                if (haveLombok(jcVariableDecl.mods)) {
                    return true;
                }
            } else if (def instanceof JCTree.JCMethodDecl) {
                JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) def;
                if (haveLombok(jcMethodDecl.mods)) {
                    return true;
                }
            } else if (def instanceof JCTree.JCClassDecl) {
                JCTree.JCClassDecl jcClassDeclInner = (JCTree.JCClassDecl) def;
                if (haveLombok(jcClassDeclInner.mods)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean haveLombok(JCTree.JCModifiers mods) {
        List<JCTree.JCAnnotation> annotations = mods.getAnnotations();
        for (JCTree.JCAnnotation annotation : annotations) {
            if (annotation.type != null && annotation.type.toString().startsWith("lombok.")) {
                return true;
            }
        }
        return false;
    }
    
    protected class SymbolContext {
        
        private Symbol targetSymbol;
        
        private TypeElement annotationSymbol;
        
        private JCTree.JCCompilationUnit compilationUnit;
        
        public SymbolContext(Symbol targetSymbol, TypeElement annotationSymbol,
                JCTree.JCCompilationUnit compilationUnit) {
            this.targetSymbol = targetSymbol;
            this.annotationSymbol = annotationSymbol;
            this.compilationUnit = compilationUnit;
        }
        
        public Symbol getReEnterSymbol() {
            for (JCTree tree : compilationUnit.defs) {
                Symbol childSymbol = findOnTree(tree);
                if (childSymbol != null) {
                    return childSymbol;
                }
            }
            return targetSymbol;
        }
        
        private Symbol findOnTree(JCTree tree) {
            if (tree instanceof JCTree.JCClassDecl) {
                JCTree.JCClassDecl classDecl = (JCTree.JCClassDecl) tree;
                if (classDecl.sym.toString().equals(targetSymbol.toString())) {
                    return ((JCTree.JCClassDecl) tree).sym;
                }
                for (JCTree childTree : classDecl.defs) {
                    Symbol childSymbol = findOnTree(childTree);
                    if (childSymbol != null) {
                        return childSymbol;
                    }
                }
            } else if (tree instanceof JCTree.JCVariableDecl) {
                JCTree.JCVariableDecl variableDecl = (JCTree.JCVariableDecl) tree;
                if (variableDecl.sym.toString().equals(targetSymbol.toString()) && variableDecl.sym.owner.toString()
                        .equals(targetSymbol.owner.toString())) {
                    return variableDecl.sym;
                }
            } else if (tree instanceof JCTree.JCMethodDecl) {
                JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl) tree;
                if (methodDecl.sym.toString().equals(targetSymbol.toString()) && methodDecl.sym.owner.toString()
                        .equals(targetSymbol.owner.toString())) {
                    return methodDecl.sym;
                }
            }
            return null;
        }
    }
}
