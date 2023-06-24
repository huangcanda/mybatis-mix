package org.wanghailu.mybatismix.apt.translator;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import org.wanghailu.mybatismix.apt.model.JavacMethodContext;
import org.wanghailu.mybatismix.apt.model.SymbolClassType;
import org.wanghailu.mybatismix.support.TwoTuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cdhuang
 * @date 2022/1/5
 */
public class LambdaUtilTreeTranslator extends AddCodeTreeTranslator {

    public Map<JCTree.JCLambda, SymbolClassType> lambdaSymbolMap = new HashMap<>();

    public LambdaUtilTreeTranslator(JavacMethodContext context) {
        super(context);
        sourceLambdaVar = false;
    }

    @Override
    public void visitNewClass(JCTree.JCNewClass jcNewClass) {
        super.visitNewClass(jcNewClass);
        findLambda(jcNewClass);
    }


    @Override
    public void visitApply(JCTree.JCMethodInvocation jcMethodInvocation) {
        super.visitApply(jcMethodInvocation);
        findLambda(jcMethodInvocation);
    }

    private void findLambda(JCTree.JCPolyExpression expression) {
        List<JCTree.JCExpression> args;
        if (expression instanceof JCTree.JCMethodInvocation) {
            JCTree.JCMethodInvocation jcMethodInvocation = (JCTree.JCMethodInvocation) expression;
            args = jcMethodInvocation.args;
        } else {
            JCTree.JCNewClass jcNewClass = (JCTree.JCNewClass) expression;
            args = jcNewClass.args;
        }
        if (args == null || args.size() == 0) {
            return;
        }
        if (args.stream().filter(arg -> arg instanceof JCTree.JCLambda).count() == 0) {
            return;
        }
        TwoTuple<ArrayList<Symbol.MethodSymbol>, SymbolClassType> tuple;
        if (expression instanceof JCTree.JCMethodInvocation) {
            JCTree.JCMethodInvocation jcMethodInvocation = (JCTree.JCMethodInvocation) expression;
            tuple = context.getMethodSymbols(jcMethodInvocation, initVarContextMap());
        } else {
            JCTree.JCNewClass jcNewClass = (JCTree.JCNewClass) expression;
            String classStr = jcNewClass.clazz.toString();
            tuple = new TwoTuple<>(context.getMethodMembersSymbolList((Symbol.ClassSymbol) context.getSymbolFromSimpleClassName(classStr), classStr), null);
        }
        ArrayList<Symbol.MethodSymbol> methodSymbols = tuple.getFirst();
        Symbol.MethodSymbol methodSymbol = context.chooseMethodSymbol(methodSymbols, args, initVarContextMap());
        if (methodSymbol != null) {
            int index = 0;
            for (JCTree.JCExpression arg : args) {
                if (arg instanceof JCTree.JCLambda) {
                    SymbolClassType symbol = context.getSymbolClassType(methodSymbol.params.get(index).type,tuple.getSecond(),null);
                    lambdaSymbolMap.put((JCTree.JCLambda) arg, symbol);
                }
                index++;
            }
        }
    }

    @Override
    public void visitVarDef(JCTree.JCVariableDecl jcVariableDecl) {
        super.visitVarDef(jcVariableDecl);
        if (jcVariableDecl.init instanceof JCTree.JCLambda) {
            SymbolClassType symbol = context.getSymbolClassType(jcVariableDecl.vartype);
            lambdaSymbolMap.put((JCTree.JCLambda) jcVariableDecl.init, symbol);
        }
    }

    @Override
    public void visitAssign(JCTree.JCAssign jcAssign) {
        super.visitAssign(jcAssign);
        if (jcAssign.rhs instanceof JCTree.JCLambda) {
            SymbolClassType symbol = context.getExpressionSymbolClassType(jcAssign.lhs, initVarContextMap());
            lambdaSymbolMap.put((JCTree.JCLambda) jcAssign.rhs, symbol);
        }
    }
}
