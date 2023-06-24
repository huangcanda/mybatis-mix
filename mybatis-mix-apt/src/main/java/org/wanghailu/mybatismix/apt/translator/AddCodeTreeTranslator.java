package org.wanghailu.mybatismix.apt.translator;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import org.wanghailu.mybatismix.apt.help.JavacUtils;
import org.wanghailu.mybatismix.apt.model.JavacMethodContext;
import org.wanghailu.mybatismix.apt.model.SymbolClassType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 记录当前作用域的变量
 * @author cdhuang
 * @date 2021/11/30
 */
public abstract class AddCodeTreeTranslator<T extends TreeNodeContext<T>> extends BaseMethodTreeTranslator {

    protected T currentContext;

    public AddCodeTreeTranslator(JavacMethodContext context) {
        super(context);
    }

    protected boolean sourceLambdaVar = true;

    protected void initTreeNodeContext(JCTree node, JCTree body, boolean isBlockStat, boolean canAddCode, boolean isFunction) {
        currentContext = (T) new TreeNodeContext(node, body, isBlockStat, canAddCode, isFunction, currentContext);
    }

    protected <B> B restoreTreeNodeContext() {
        JCTree result = currentContext.handleTreeBody();
        currentContext = currentContext.oldTreeNodeContext;
        return (B) result;
    }

    protected Map<String, SymbolClassType> initVarContextMap() {
        Map<String, SymbolClassType> varContextMap = new HashMap<>();
        for (JCTree def : context.getClassDecl().defs) {
            if (def instanceof JCTree.JCVariableDecl) {
                JCTree.JCVariableDecl variableDecl = (JCTree.JCVariableDecl) def;
                varContextMap.put(variableDecl.name.toString(), context.getSymbolClassType(variableDecl.vartype));
            }
        }
        varContextMap.putAll(currentContext.getWorkVarMap());
        return varContextMap;
    }

    @Override
    public void visitVarDef(JCTree.JCVariableDecl jcVariableDecl) {
        super.visitVarDef(jcVariableDecl);
        if (currentContext != null) {
            if(jcVariableDecl.vartype!=null){
                currentContext.getWorkVarMap().put(jcVariableDecl.name.toString(), context.getSymbolClassType(jcVariableDecl.vartype));
            }else if(sourceLambdaVar && currentContext.parentNode instanceof JCTree.JCLambda){
                JCTree.JCLambda jcLambda = (JCTree.JCLambda) currentContext.parentNode;
                SymbolClassType interfaceClassSymbol = context.getLambdaFunctionInterfaceClassSymbol(jcLambda);
                Symbol.MethodSymbol lambdaMethodSymbol = JavacUtils.getCanLambdaMethodSymbol(interfaceClassSymbol.symbol);
                int argIndex = jcLambda.params.indexOf(jcVariableDecl);
                SymbolClassType paramSymbolClassType= context.getSymbolClassType(lambdaMethodSymbol.params().get(argIndex).type,interfaceClassSymbol,null);
                currentContext.getWorkVarMap().put(jcVariableDecl.name.toString(), paramSymbolClassType);
            }
        }
    }

    @Override
    public void visitMethodDef(JCTree.JCMethodDecl jcMethodDecl) {
        initTreeNodeContext(jcMethodDecl, jcMethodDecl.body, false, true, true);
        super.visitMethodDef(jcMethodDecl);
        restoreTreeNodeContext();
    }

    @Override
    public void visitBlock(JCTree.JCBlock jcBlock) {
        ArrayList<JCTree.JCStatement> list = new ArrayList<>();
        for (JCTree.JCStatement stat : jcBlock.stats) {
            list.add(stat);
        }
        for (JCTree.JCStatement stat : list) {
            initTreeNodeContext(jcBlock, stat, true, true, false);
            this.translate(stat);
            restoreTreeNodeContext();
        }
        this.result = jcBlock;
    }

    @Override
    public void visitLambda(JCTree.JCLambda jcLambda) {
        initTreeNodeContext(jcLambda, jcLambda.body, false, true, true);
        super.visitLambda(jcLambda);
        jcLambda.body = restoreTreeNodeContext();
    }

    @Override
    public void visitDoLoop(JCTree.JCDoWhileLoop jcDoWhileLoop) {
        initTreeNodeContext(jcDoWhileLoop, jcDoWhileLoop.body, false, true, false);
        super.visitDoLoop(jcDoWhileLoop);
        jcDoWhileLoop.body = restoreTreeNodeContext();
    }

    @Override
    public void visitForeachLoop(JCTree.JCEnhancedForLoop jcEnhancedForLoop) {
        initTreeNodeContext(jcEnhancedForLoop, jcEnhancedForLoop.body, false, true, false);
        super.visitForeachLoop(jcEnhancedForLoop);
        jcEnhancedForLoop.body = restoreTreeNodeContext();
    }

    @Override
    public void visitForLoop(JCTree.JCForLoop jcForLoop) {
        initTreeNodeContext(jcForLoop, jcForLoop.cond, false, false, false);
        jcForLoop.init = this.translate(jcForLoop.init);
        jcForLoop.cond = this.translate(jcForLoop.cond);

        initTreeNodeContext(jcForLoop, jcForLoop.body, false, true, false);
        jcForLoop.body = this.translate(jcForLoop.body);
        jcForLoop.step = this.translate(jcForLoop.step);
        jcForLoop.body = restoreTreeNodeContext();

        restoreTreeNodeContext();
        this.result = jcForLoop;
    }

    @Override
    public void visitIf(JCTree.JCIf jcIf) {
        initTreeNodeContext(jcIf, jcIf.cond, false, false, false);
        jcIf.cond = this.translate(jcIf.cond);

        initTreeNodeContext(jcIf, jcIf.thenpart, false, true, false);
        jcIf.thenpart = this.translate(jcIf.thenpart);
        jcIf.thenpart = restoreTreeNodeContext();

        initTreeNodeContext(jcIf, jcIf.elsepart, false, true, false);
        jcIf.elsepart = this.translate(jcIf.elsepart);
        jcIf.elsepart = restoreTreeNodeContext();

        restoreTreeNodeContext();
        this.result = jcIf;
    }

    @Override
    public void visitWhileLoop(JCTree.JCWhileLoop jcWhileLoop) {
        initTreeNodeContext(jcWhileLoop, jcWhileLoop.cond, false, false, false);
        jcWhileLoop.cond = this.translate(jcWhileLoop.cond);

        initTreeNodeContext(jcWhileLoop, jcWhileLoop.body, false, true, false);
        jcWhileLoop.body = this.translate(jcWhileLoop.body);
        jcWhileLoop.body = restoreTreeNodeContext();

        restoreTreeNodeContext();
        this.result = jcWhileLoop;
    }
}
