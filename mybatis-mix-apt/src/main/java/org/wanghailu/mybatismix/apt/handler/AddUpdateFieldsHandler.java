package org.wanghailu.mybatismix.apt.handler;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
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
 * @author cdhuang
 * @date 2021/12/17
 */
public class AddUpdateFieldsHandler extends BaseAnnotationHandler<JavacClassContext> {

    private ArrayList<JCTree.JCVariableDecl> settableFieldList = new ArrayList<>();

    public AddUpdateFieldsHandler(JavacClassContext javacClassContext) {
        super(javacClassContext);
        findSettableFields();
    }

    @Override
    public boolean handle() {
        checkExtendsBaseExactUpdateRecord();
        context.getClassDecl().defs.stream().filter(def -> def instanceof JCTree.JCMethodDecl).forEach(def -> {
            JavacMethodContext methodContext= new JavacMethodContext(context,((JCTree.JCMethodDecl) def).sym);
            def.accept(new AddUpdateFieldTreeTranslator(methodContext, settableFieldList));
        });
        return true;
    }

    private void checkExtendsBaseExactUpdateRecord() {
        Symbol symbol = context.getSymbolFromSimpleClassName("org.wanghailu.mybatismix.model.ExactUpdateEnable");
        if(symbol==null){
            throw new NullPointerException("找不到org.wanghailu.mybatismix.model.ExactUpdateEnable的符号");
        }
        Type updateFieldsRecordType = symbol.type;
        //先判断是否有实现接口
        if(context.typeIndexOf(context.getClassSymbol().type,updateFieldsRecordType)!=-1){
            return;
        }
        boolean needAddExtend = false;
        JCTree.JCClassDecl jcClassDecl = context.getClassDecl();
        if (jcClassDecl.extending != null) {
            Symbol parentClassSymbol = context.getSymbolFromSimpleClassName( jcClassDecl.extending.toString());
            if ("java.lang.Object".equals(parentClassSymbol.type.toString())) {
                needAddExtend = true;
            } else if(context.typeIndexOf(parentClassSymbol.type,updateFieldsRecordType)!=-1){
                return;
            }else if(context.isContainAnnotation(parentClassSymbol,"org.wanghailu.mybatismix.model.EnableExactUpdateModel")){
                return;
            }else{
                context.getMessager().printMessage(Diagnostic.Kind.ERROR, "使用注解@EnableExactUpdateModel的类必须继承BaseExactUpdateRecord类。");
            }
        } else {
            needAddExtend = true;
        }
        if (needAddExtend) {
            int result = context.importPackage( "org.wanghailu.mybatismix.model.BaseExactUpdateRecord");
            context.getClassDecl().extending = context.initExp(result == -1 ? "org.wanghailu.mybatismix.model.BaseExactUpdateRecord" : "BaseExactUpdateRecord");
        }
    }

    private void findSettableFields() {
        List<JCTree.JCVariableDecl> variableDeclList = (List) context.getClassDecl().defs.stream().filter(def -> def instanceof JCTree.JCVariableDecl).collect(Collectors.toList());
        List<String> publicFields = new ArrayList<>();
        for (JCTree.JCVariableDecl variableDecl : variableDeclList) {
            Set<Modifier> modifiers = variableDecl.mods.getFlags();
            if (modifiers.contains(Modifier.STATIC)
                    || modifiers.contains(Modifier.FINAL)
                    || modifiers.contains(Modifier.TRANSIENT)) {
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
            context.getMessager().printMessage(Diagnostic.Kind.ERROR, "使用注解@EnableExactUpdateModel的类中的字段应该满足面向对象封装规范，字段" + PrivateStringUtils
                    .join(publicFields, ",") + "应该使用private进行修饰。");
        }
    }
}
