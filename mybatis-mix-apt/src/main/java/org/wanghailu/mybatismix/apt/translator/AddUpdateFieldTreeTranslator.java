package org.wanghailu.mybatismix.apt.translator;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import org.wanghailu.mybatismix.apt.help.JavacUtils;
import org.wanghailu.mybatismix.apt.model.JavacMethodContext;
import org.wanghailu.mybatismix.apt.model.SymbolClassType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 遍历语法树添加addUpdateField，记录对应更新值
 */
public class AddUpdateFieldTreeTranslator extends AddCodeTreeTranslator<AddUpdateFieldTreeTranslator.AddSetFieldContext> {

    private ArrayList<String> settableFieldNameList;

    private AtomicInteger varNameIndex = new AtomicInteger(0);

    public AddUpdateFieldTreeTranslator(JavacMethodContext context, ArrayList<JCTree.JCVariableDecl> settableFieldList) {
        super(context);
        this.settableFieldNameList = settableFieldList.stream().map(x -> x.name.toString()).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    protected void initTreeNodeContext(JCTree node, JCTree body, boolean isBlockStat, boolean canAddCode, boolean isFunction) {
        currentContext = new AddSetFieldContext(node, body, isBlockStat, canAddCode, isFunction, currentContext);
    }

    @Override
    public void visitAssign(JCTree.JCAssign jcAssign) {
        super.visitAssign(jcAssign);
        visitOpVar(jcAssign.lhs);
    }

    @Override
    public void visitAssignop(JCTree.JCAssignOp jcAssignOp) {
        super.visitAssignop(jcAssignOp);
        visitOpVar(jcAssignOp.lhs);
    }

    @Override
    public void visitUnary(JCTree.JCUnary jcUnary) {
        super.visitUnary(jcUnary);
        visitOpVar(jcUnary.arg);
    }

    private void visitOpVar(JCTree.JCExpression opVar) {
        if (opVar instanceof JCTree.JCParens) {
            opVar = ((JCTree.JCParens) opVar).expr;
        }
        if (opVar instanceof JCTree.JCIdent) {
            JCTree.JCIdent jcIdent = (JCTree.JCIdent) opVar;
            String fieldName = jcIdent.name.toString();
            if (settableFieldNameList.contains(fieldName)) {
                //需要排除掉当前上下文中的自定义变量名
                if (!currentContext.getWorkVarMap().containsKey(fieldName)) {
                    currentContext.getSetFieldNameList().add(fieldName);
                }
            }
        } else if (opVar instanceof JCTree.JCFieldAccess) {
            JCTree.JCFieldAccess jcFieldAccess = (JCTree.JCFieldAccess) opVar;
            String fieldName = jcFieldAccess.name.toString();
            if (settableFieldNameList.contains(fieldName)) {
                JCTree.JCExpression selected = jcFieldAccess.selected;
                if (selected instanceof JCTree.JCIdent && selected.toString().equals("this")) {
                    currentContext.getSetFieldNameList().add(fieldName);
                } else {
                    Symbol.ClassSymbol symbol = context.getExpressionSymbolClassType(selected, initVarContextMap()).symbol;
                    if(symbol.toString().equals(context.getClassSymbol().toString())){
                        currentContext.getOtherSetFieldNameMap().put(fieldName, jcFieldAccess);
                    }else{
                        Type updateFieldsRecordType = context.getSymbolFromSimpleClassName("org.wanghailu.mybatismix.model.ExactUpdateEnable").type;
                        if (context.typeIndexOf(symbol.type, updateFieldsRecordType) > -1) {
                            currentContext.getOtherSetFieldNameMap().put(fieldName, jcFieldAccess);
                        }
                    }
                }
            }
        }
    }
    
    public static final String addMethodName = "addUpdateField";

    public class AddSetFieldContext extends TreeNodeContext<AddSetFieldContext> {
        /**
         * 当前上下文中被修改的属性
         */
        private Set<String> setFieldNameList = new HashSet<>();

        /**
         * 当前上下文中被修改的属性
         */
        private Map<String, JCTree.JCFieldAccess> otherSetFieldNameMap = new HashMap<>();

        public AddSetFieldContext(JCTree node, JCTree body, boolean isBlockStat, boolean canAddCode, boolean isFunction, AddSetFieldContext oldTreeNodeContext) {
            super(node, body, isBlockStat, canAddCode, isFunction, oldTreeNodeContext);
        }

        public Set<String> getSetFieldNameList() {
            if (canAddCode) {
                return setFieldNameList;
            } else {
                return oldTreeNodeContext.getSetFieldNameList();
            }
        }

        public Map<String, JCTree.JCFieldAccess> getOtherSetFieldNameMap() {
            if (canAddCode) {
                return otherSetFieldNameMap;
            } else {
                return oldTreeNodeContext.getOtherSetFieldNameMap();
            }
        }

        @Override
        public JCTree handleTreeBody() {
            if (!this.canAddCode) {
                return bodyNode;
            }
            if (getSetFieldNameList().size() == 0 && getOtherSetFieldNameMap().size() == 0) {
                return bodyNode;
            }
            JCTree bodyNode;
            JCTree.JCBlock jcBlock;
            JCTree.JCStatement jcStatement;
            if (isBlockStat) {
                jcBlock = (JCTree.JCBlock) parentNode;
                jcStatement = (JCTree.JCStatement) this.bodyNode;
                bodyNode = this.bodyNode;
            } else {
                JCTree jcTree = this.bodyNode;
                if (jcTree instanceof JCTree.JCStatement) {
                    jcStatement = (JCTree.JCStatement) jcTree;
                } else if (jcTree instanceof JCTree.JCExpression) {
                    boolean addReturn =false;
                    if (this.parentNode instanceof JCTree.JCLambda) {
                        SymbolClassType interfaceClassSymbol = context.getLambdaFunctionInterfaceClassSymbol((JCTree.JCLambda) this.parentNode);
                        if (interfaceClassSymbol.symbol != null) {
                            Symbol.MethodSymbol lambdaMethodSymbol = JavacUtils.getCanLambdaMethodSymbol(interfaceClassSymbol.symbol);
                            if (lambdaMethodSymbol != null && lambdaMethodSymbol.type instanceof Type.MethodType) {
                                Type lambdaMethodReturnType = ((Type.MethodType) lambdaMethodSymbol.type).restype;
                                if (lambdaMethodReturnType instanceof Type.JCVoidType == false && jcTree instanceof JCTree.JCReturn == false) {
                                    addReturn =true;
                                }
                            }
                        }
                    }
                    if(addReturn){
                        jcStatement = treeMaker.Return((JCTree.JCExpression) jcTree);
                    }else{
                        jcStatement = treeMaker.Exec((JCTree.JCExpression) jcTree);
                    }
                } else {
                    throw new UnsupportedOperationException("未知的jctree类型:" + jcTree.getClass().getName());
                }
                List<JCTree.JCStatement> list = List.of(jcStatement);
                jcBlock = treeMaker.Block(0, list);
                bodyNode = jcBlock;
            }
            for (String varName : getSetFieldNameList()) {
                boolean added = false;
                for (JCTree.JCStatement stat : jcBlock.stats) {
                    if (stat instanceof JCTree.JCExpressionStatement) {
                        if (stat.toString().equals(addMethodName+"(\"" + varName + "\");") || stat.toString().equals("this."+addMethodName+"(\"" + varName + "\");") || stat.toString().equals("super."+addMethodName+"(\"" + varName + "\");")) {
                            added = true;
                            break;
                        }
                    }
                }
                int appendIndex = jcBlock.stats.indexOf(jcStatement) + 1;
                if (jcStatement instanceof JCTree.JCReturn) {
                    appendIndex--;
                }
                if (!added) {
                    JCTree.JCExpressionStatement jcExpressionStatement = context.execExp(addMethodName+"()", treeMaker.Literal(varName));
                    jcBlock.stats = JavacUtils.listAppend(jcBlock.stats, jcExpressionStatement, appendIndex);
                }
            }
            for (Map.Entry<String, JCTree.JCFieldAccess> entry : getOtherSetFieldNameMap().entrySet()) {
                String fieldName = entry.getKey();
                JCTree.JCFieldAccess fieldAccess = entry.getValue();
                JCTree.JCExpression exp = fieldAccess.selected;
                String expStr = exp.toString();
                String addSettedFieldExpStr = expStr + "."+addMethodName+"(\"" + fieldName + "\")";
                boolean added = false;
                for (JCTree.JCStatement stat : jcBlock.stats) {
                    if (stat instanceof JCTree.JCExpressionStatement) {
                        if (stat.toString().contains(addSettedFieldExpStr)) {
                            added = true;
                            break;
                        }
                    }
                }
                int appendIndex = jcBlock.stats.indexOf(jcStatement);
                if (!added) {
                    if (expStr.contains("(") && expStr.contains(")")) {
                        String varName = "var$$settedFieldsRecord_temp_" + varNameIndex.getAndAdd(1);
                        Symbol.ClassSymbol classSymbol = context.getExpressionSymbolClassType(exp, initVarContextMap()).symbol;
                        String typeStr;
                        if (classSymbol.type.toString().equals(context.getClassSymbol().type.toString())) {
                            typeStr = classSymbol.name.toString();
                        } else {
                            int importResult = context.importPackage(classSymbol.type.toString());
                            typeStr = importResult == -1 ? classSymbol.type.toString() : classSymbol.name.toString();
                        }
                        JCTree.JCVariableDecl variableDecl = context.initVar(typeStr, varName, exp);
                        jcBlock.stats = JavacUtils.listAppend(jcBlock.stats, variableDecl, appendIndex);
                        appendIndex++;
                        fieldAccess.selected = context.initExp(varName);
                        if (jcStatement instanceof JCTree.JCReturn == false) {
                            appendIndex++;
                        }
                        jcBlock.stats = JavacUtils.listAppend(jcBlock.stats, context.execExp(varName + "."+addMethodName+"()", treeMaker.Literal(fieldName)), appendIndex);
                    } else {
                        if (jcStatement instanceof JCTree.JCReturn == false) {
                            appendIndex++;
                        }
                        JCTree.JCExpression newExp = treeMaker.Select(exp, names.fromString(addMethodName));
                        newExp = treeMaker.Apply(List.nil(), newExp, List.of(treeMaker.Literal(fieldName)));
                        JCTree.JCExpressionStatement jcExpressionStatement = treeMaker.Exec(newExp);
                        jcBlock.stats = JavacUtils.listAppend(jcBlock.stats, jcExpressionStatement, appendIndex);
                    }
                }
            }
            return bodyNode;

        }
    }
}
