package org.wanghailu.mybatismix.apt.handler;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import org.wanghailu.mybatismix.apt.help.JavacUtils;
import org.wanghailu.mybatismix.apt.model.JavacMethodContext;

import java.util.Set;

/**
 * 处理MybatisBatchExecute注解，做对应支持，开启批处理
 * @author cdhuang
 * @date 2023/8/1
 */
public class UseBatchExecuteHandler extends BaseAnnotationHandler<JavacMethodContext> {
    
    private String originalMethodName;
    
    public UseBatchExecuteHandler(JavacMethodContext context) {
        super(context);
        originalMethodName = context.getMethodSymbol().name.toString() + "$$0$$OnMybatisBatchExecute";
    }
    
    @Override
    public boolean handle() {
        int importState = context.importPackage("org.wanghailu.mybatismix.batch.BatchExecuteTemplateBinder");
        if (importState == 0 && checkOriginalMethodExist()) {
            return false;
        }
        addOriginalMethod();
        changeMethodBody(importState != -1);
        return true;
    }
    
    protected void addOriginalMethod() {
        JCTree.JCMethodDecl methodDecl = context.getMethodDecl();
        Symbol.MethodSymbol methodSymbol = context.getMethodSymbol();
        Set<Flags.Flag> flagSet = Flags.asFlagSet(methodSymbol.flags());
        JCTree.JCModifiers modifiers;
        if (flagSet.contains(Flags.Flag.STATIC)) {
            modifiers = treeMaker.Modifiers(Flags.PRIVATE + Flags.STATIC);
        } else {
            modifiers = treeMaker.Modifiers(Flags.PRIVATE);
        }
        Name methodName = names.fromString(originalMethodName);
        JCTree.JCExpression returnType = treeMaker.Type(methodDecl.restype.type);
        ListBuffer<Type> typaramType = new ListBuffer<>();
        for (JCTree.JCTypeParameter typaram : methodDecl.typarams) {
            typaramType.append(typaram.type);
        }
        List<JCTree.JCTypeParameter> typeParameters = treeMaker.TypeParams(typaramType.toList());
        ListBuffer<JCTree.JCVariableDecl> paramList = new ListBuffer<>();
        int index = 0;
        for (Symbol.VarSymbol param : methodSymbol.params) {
            JCTree.JCVariableDecl param2 = methodDecl.params.get(index);
            JCTree.JCVariableDecl variableDecl = treeMaker
                    .VarDef(treeMaker.Modifiers(param2.mods.flags), param2.getName(), treeMaker.Type(param.type), null);
            paramList.append(variableDecl);
            index++;
        }
        List<JCTree.JCExpression> thrownTypes = treeMaker.Types(methodSymbol.type.getThrownTypes());
        JCTree.JCBlock block = treeMaker.Block(0, methodDecl.getBody().stats);
        JCTree.JCMethodDecl newMethodDecl = treeMaker
                .MethodDef(modifiers, methodName, returnType, typeParameters, null, paramList.toList(), thrownTypes,
                        block, null);
        context.getClassDecl().defs = JavacUtils.listAppend(context.getClassDecl().defs, newMethodDecl);
    }
    
    protected void changeMethodBody(boolean imported) {
        JCTree.JCMethodDecl methodDecl = context.getMethodDecl();
        boolean isVoid = "void".equals(methodDecl.restype.toString());
        String templateBinderName =
                imported ? "BatchExecuteTemplateBinder" : "org.wanghailu.mybatismix.batch.BatchExecuteTemplateBinder";
        JCTree.JCExpression[] args = new JCTree.JCExpression[context.getMethodDecl().params.length()];
        int index = 0;
        for (JCTree.JCVariableDecl param : context.getMethodDecl().params) {
            args[index] = treeMaker.Ident(param.name);
            index++;
        }
        JCTree.JCExpression methodInvocation = context.initExp(originalMethodName + "()", args);
        JCTree.JCVariableDecl param = treeMaker
                .VarDef(treeMaker.Modifiers(8589934592L), names.fromString("context"), null, null);
        JCTree.JCLambda lambda;
        if(isVoid){
            JCTree.JCExpressionStatement jces1 = treeMaker.Exec(methodInvocation);
            JCTree.JCReturn jces2 = treeMaker.Return(treeMaker.Literal(TypeTag.BOT,null));
            lambda = treeMaker.Lambda(List.of(param), treeMaker.Block(0,List.of(jces1,jces2)));
        }else{
            lambda = treeMaker.Lambda(List.of(param), methodInvocation);
        }
        
        JCTree.JCExpression methodInvocation2 = context
                .initExp(templateBinderName + ".getTemplate().executeOnBatchMode()", lambda);
        if(isVoid){
            methodDecl.body = treeMaker.Block(0, List.of(treeMaker.Exec(methodInvocation2)));
        }else{
            JCTree.JCReturn jcReturn = treeMaker.Return(methodInvocation2);
            methodDecl.body = treeMaker.Block(0, List.of(jcReturn));
        }
        
    }
    
    private boolean checkOriginalMethodExist() {
        for (JCTree def : context.getClassDecl().defs) {
            if (def instanceof JCTree.JCMethodDecl) {
                JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl) def;
                if (methodDecl.name.toString().equals(originalMethodName) && methodDecl.params
                        .toString().equals(context.getMethodDecl().params.toString())) {
                    return true;
                }
            }
        }
        return false;
    }
}
