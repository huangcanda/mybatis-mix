package org.wanghailu.mybatismix.apt.handler;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Name;
import org.wanghailu.mybatismix.apt.help.JavacUtils;
import org.wanghailu.mybatismix.apt.model.JavacClassContext;
import org.wanghailu.mybatismix.apt.model.JavacMethodContext;
import org.wanghailu.mybatismix.apt.translator.AddUpdateFieldTreeTranslator;
import org.wanghailu.mybatismix.util.PrivateStringUtils;

import javax.lang.model.element.Modifier;
import javax.persistence.Transient;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 处理某个实体类，给类里面所有涉及添加addUpdateField方法记录更新的值
 */
public class AddUpdateFieldsHandler extends BaseAnnotationHandler<JavacClassContext> {
    
    public static final String SUPER_FIELD_LENGTH_FOR_FIELD_INDEX = "$$SUPER_FIELD_LENGTH_FOR_FIELD_INDEX";
    
    public static final String FIELD_LENGTH_FOR_FIELD_INDEX = "$$FIELD_LENGTH_FOR_FIELD_INDEX";
    
    public static final String STATIC_INIT_METHOD_NAME = "$$initFieldNamesOnFieldIndexUtils";
    
    private ArrayList<JCTree.JCVariableDecl> settableFieldList = new ArrayList<>();
    
    private String parentClassNameForFieldIndex;
    
    public AddUpdateFieldsHandler(JavacClassContext javacClassContext) {
        super(javacClassContext);
        findSettableFields();
    }
    
    @Override
    public boolean handle() {
        checkExtendsBaseExactUpdateRecord();
        context.getClassDecl().defs.stream().filter(def -> def instanceof JCTree.JCMethodDecl).forEach(def -> {
            JavacMethodContext methodContext = new JavacMethodContext(context, ((JCTree.JCMethodDecl) def).sym);
            def.accept(
                    new AddUpdateFieldTreeTranslator(methodContext, settableFieldList));
        });
        addFieldIndexInfo();
        return true;
    }
    
    private void addFieldIndexInfo(){
        if(addedFieldIndexInfo()){
            return;
        }
        int importState = context.importPackage( "org.wanghailu.mybatismix.model.BaseExactUpdateRecord");
        String defaultParentClassNameForFieldIndex = importState == -1 ?"org.wanghailu.mybatismix.model.BaseExactUpdateRecord":"BaseExactUpdateRecord";
        if(parentClassNameForFieldIndex==null){
            parentClassNameForFieldIndex = defaultParentClassNameForFieldIndex;
        }
        initSuperFieldLengthForFieldIndex();
        int importState2 = context.importPackage( "org.wanghailu.mybatismix.model.FieldIndexUtils");
        String fieldIndexUtilsName = importState2 == -1 ?"org.wanghailu.mybatismix.model.FieldIndexUtils":"FieldIndexUtils";
        initFieldNamesOnFieldIndexUtils(fieldIndexUtilsName);
        addMethodOnStaticBlock();
    }
    
    private boolean addedFieldIndexInfo(){
        boolean initSuperFieldLengthForFieldIndex = false;
        boolean initFieldNamesOnFieldIndexUtils = false;
        for (JCTree def : context.getClassDecl().defs) {
            if(def instanceof JCTree.JCVariableDecl){
                if(SUPER_FIELD_LENGTH_FOR_FIELD_INDEX.equals(((JCTree.JCVariableDecl) def).name.toString())){
                    initSuperFieldLengthForFieldIndex = true;
                }
            }
            if(def instanceof JCTree.JCMethodDecl){
                if(STATIC_INIT_METHOD_NAME.equals(((JCTree.JCMethodDecl) def).name.toString())){
                    initFieldNamesOnFieldIndexUtils = true;
                }
            }
        }
        return initFieldNamesOnFieldIndexUtils && initSuperFieldLengthForFieldIndex;
    }
    
    private void initSuperFieldLengthForFieldIndex(){
        JCTree.JCVariableDecl variableDecl = context.initVar(Flags.PUBLIC + Flags.STATIC+ Flags.FINAL,"int",
                SUPER_FIELD_LENGTH_FOR_FIELD_INDEX,context.initExp(parentClassNameForFieldIndex+ "."+FIELD_LENGTH_FOR_FIELD_INDEX));
        context.getClassDecl().defs = JavacUtils.listAppend(context.getClassDecl().defs, variableDecl,1);
        JCTree.JCVariableDecl variableDecl2 = context.initVar(Flags.PUBLIC + Flags.STATIC+ Flags.FINAL,"int",
                FIELD_LENGTH_FOR_FIELD_INDEX,treeMaker.Binary(JCTree.Tag.PLUS,treeMaker.Literal(settableFieldList.size()),context.initExp(SUPER_FIELD_LENGTH_FOR_FIELD_INDEX)));
        context.getClassDecl().defs = JavacUtils.listAppend(context.getClassDecl().defs, variableDecl2,2);
    }
    
    private void addMethodOnStaticBlock(){
        JCTree.JCBlock staticBlock = null;
        for (JCTree def : context.getClassDecl().defs) {
            if(def instanceof JCTree.JCBlock && ((JCTree.JCBlock) def).flags == Flags.STATIC){
                staticBlock = (JCTree.JCBlock) def;
                break;
            }
        }
        if(staticBlock == null){
            staticBlock = treeMaker.Block(Flags.STATIC,com.sun.tools.javac.util.List.nil());
            context.getClassDecl().defs = JavacUtils.listAppend(context.getClassDecl().defs, staticBlock,3);
        }
        staticBlock.stats = JavacUtils.listAppend(staticBlock.stats,context.execExp(STATIC_INIT_METHOD_NAME + "()"),0);
    }
    
    private void initFieldNamesOnFieldIndexUtils(String fieldIndexUtilsName){
        JCTree.JCModifiers modifiers = treeMaker.Modifiers(Flags.PRIVATE + Flags.STATIC);

        Name methodName = names.fromString(STATIC_INIT_METHOD_NAME);
        JCTree.JCExpression returnType = treeMaker.TypeIdent(TypeTag.VOID);
        int importState = context.importPackage( "java.util.Arrays");
        String arraysImport = importState==-1?"java.util.Arrays":"Arrays";
        JCTree.JCExpression parentArray = context.initExp("FieldIndexUtils.getFieldNames()",context.initExp(parentClassNameForFieldIndex+".class"));
        JCTree.JCExpression array = context.initExp(arraysImport+".copyOf()",parentArray,context.initExp(FIELD_LENGTH_FOR_FIELD_INDEX));
        JCTree.JCVariableDecl variableDecl = treeMaker.VarDef(treeMaker.Modifiers(0),names.fromString("fieldNames"),treeMaker.TypeArray(context.initExp("String")),array);
        com.sun.tools.javac.util.List<JCTree.JCStatement> stats = com.sun.tools.javac.util.List.nil();
        stats = JavacUtils.listAppend(stats,variableDecl);
        int index = 0;
        for (JCTree.JCVariableDecl jcVariableDecl : settableFieldList) {
            JCTree.JCBinary indexExp = treeMaker.Binary(JCTree.Tag.PLUS, treeMaker.Literal(index), treeMaker.Ident(names.fromString(SUPER_FIELD_LENGTH_FOR_FIELD_INDEX)));
            JCTree.JCExpression varArray = context.initExp("fieldNames");
            JCTree.JCExpression arrayIndex = treeMaker.Indexed(varArray,indexExp);
            JCTree.JCExpressionStatement expressionStatement = treeMaker.Exec(treeMaker.Assign(arrayIndex,treeMaker.Literal(jcVariableDecl.name.toString())));
            stats = JavacUtils.listAppend(stats,expressionStatement);
            index++;
        }
    
        JCTree.JCExpressionStatement putStat = context.execExp(fieldIndexUtilsName + ".putFieldNames()",context.initExp(context.getClassSymbol().getSimpleName().toString()+".class"),context.initExp("fieldNames"));
        stats = JavacUtils.listAppend(stats,putStat);
        
        JCTree.JCBlock block = treeMaker.Block(0, stats);
        JCTree.JCMethodDecl newMethodDecl = treeMaker
                .MethodDef(modifiers, methodName, returnType, com.sun.tools.javac.util.List.nil(), null, com.sun.tools.javac.util.List.nil(), com.sun.tools.javac.util.List.nil(),
                        block, null);
        context.getClassDecl().defs = JavacUtils.listAppend(context.getClassDecl().defs, newMethodDecl,4);
    }
    
    private void checkExtendsBaseExactUpdateRecord() {
        Symbol symbol = context.getSymbolFromSimpleClassName("org.wanghailu.mybatismix.model.ExactUpdateEnable");
        if (symbol == null) {
            throw new NullPointerException("找不到org.wanghailu.mybatismix.model.ExactUpdateEnable的符号");
        }
        Type updateFieldsRecordType = symbol.type;
        boolean needAddExtend = false;
        JCTree.JCClassDecl jcClassDecl = context.getClassDecl();
        if (jcClassDecl.extending != null) {
            Symbol parentClassSymbol = context.getSymbolFromSimpleClassName(jcClassDecl.extending.toString());
            if ("java.lang.Object".equals(parentClassSymbol.type.toString())) {
                needAddExtend = true;
            } else if (context.typeIndexOf(parentClassSymbol.type, updateFieldsRecordType) != -1) {
                parentClassNameForFieldIndex = parentClassSymbol.name.toString();
                return;
            } else if (context
                    .isContainAnnotation(parentClassSymbol, "org.wanghailu.mybatismix.model.EnableExactUpdate")) {
                parentClassNameForFieldIndex = parentClassSymbol.name.toString();
                return;
            }else if (context.typeIndexOf(context.getClassSymbol().type, updateFieldsRecordType) != -1) {
                return;
            }
            else {
                context.getMessager()
                        .printMessage(Diagnostic.Kind.ERROR, "使用注解@EnableExactUpdate的类的父类必须实现EnableExactUpdate接口。");
            }
        } else {
            if (context.typeIndexOf(context.getClassSymbol().type, updateFieldsRecordType) != -1) {
                return;
            }
            needAddExtend = true;
        }
        if (needAddExtend) {
            int result = context.importPackage("org.wanghailu.mybatismix.model.BaseExactUpdateRecord");
            String parentName = result == -1 ? "org.wanghailu.mybatismix.model.BaseExactUpdateRecord" : "BaseExactUpdateRecord";
            parentClassNameForFieldIndex = parentName;
            context.getClassDecl().extending = context.initExp(parentName);
        }
    }
    
    private void findSettableFields() {
        List<JCTree.JCVariableDecl> variableDeclList = (List) context.getClassDecl().defs.stream()
                .filter(def -> def instanceof JCTree.JCVariableDecl).collect(Collectors.toList());
        List<String> publicFields = new ArrayList<>();
        for (JCTree.JCVariableDecl variableDecl : variableDeclList) {
            Set<Modifier> modifiers = variableDecl.mods.getFlags();
            if (modifiers.contains(Modifier.STATIC) || modifiers.contains(Modifier.FINAL) || modifiers
                    .contains(Modifier.TRANSIENT)) {
                continue;
            }
            if (context.isContainAnnotation(variableDecl.sym, Transient.class.getName())) {
                continue;
            }
            if (modifiers.contains(Modifier.PRIVATE)) {
                settableFieldList.add(variableDecl);
            } else {
                publicFields.add(variableDecl.name.toString());
            }
        }
        if (publicFields.size() > 0) {
            context.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "使用注解@EnableExactUpdate的类中的字段应该满足面向对象封装规范，字段" + PrivateStringUtils.join(publicFields, ",")
                            + "应该使用private进行修饰。");
        }
    }
}
