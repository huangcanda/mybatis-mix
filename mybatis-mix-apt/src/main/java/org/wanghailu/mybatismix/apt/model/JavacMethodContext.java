package org.wanghailu.mybatismix.apt.model;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import org.wanghailu.mybatismix.apt.translator.LambdaUtilTreeTranslator;

/**
 * JavacMethodContext
 */
public class JavacMethodContext extends JavacClassContext {


    private Symbol.MethodSymbol methodSymbol;
    /**
     * 方法的声明
     */
    private JCTree.JCMethodDecl methodDecl;

    public JavacMethodContext(JavacContext context, Symbol.MethodSymbol methodSymbol) {
        super(context,(Symbol.ClassSymbol) methodSymbol.owner);
        this.methodSymbol = methodSymbol;
        methodDecl = context.getJavacTrees().getTree(methodSymbol);
    }

    public String getSimpleName() {
        return getClassSymbol().getSimpleName() + "." + methodSymbol.getSimpleName();
    }

    public Symbol.MethodSymbol getMethodSymbol() {
        return methodSymbol;
    }

    public JCTree.JCMethodDecl getMethodDecl() {
        return methodDecl;
    }

    public SymbolClassType getLambdaFunctionInterfaceClassSymbol(JCTree.JCLambda jcLambda) {
        LambdaUtilTreeTranslator treeTranslator = new LambdaUtilTreeTranslator(this);
        this.getMethodDecl().accept(treeTranslator);
        return treeTranslator.lambdaSymbolMap.get(jcLambda);
    }
}
