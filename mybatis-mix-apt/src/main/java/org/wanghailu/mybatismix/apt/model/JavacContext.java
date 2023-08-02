package org.wanghailu.mybatismix.apt.model;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Names;
import org.wanghailu.mybatismix.apt.help.Javac8Function;
import org.wanghailu.mybatismix.apt.help.Javac9Function;
import org.wanghailu.mybatismix.apt.help.JavacFunction;
import org.wanghailu.mybatismix.util.PrivateStringUtils;
import org.wanghailu.mybatismix.util.TruckUtils;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
/**
 * @author cdhuang
 * @date 2021/11/19
 */
public class JavacContext extends BaseContext {
    
    public static JavacFunction javacFunction;
    
    protected Context context;
    
    protected JavacElements elementUtils;
    
    protected TreeMaker treeMaker;
    
    protected Names names;
    
    protected Symtab symtab;
    
    protected JavacTrees javacTrees;
    
    protected Types types;
    
    protected Attr attr;
    
    protected Enter enter;
    
    
    public JavacContext(ProcessingEnvironment processingEnv) {
        super(processingEnv);
        this.context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.elementUtils = (JavacElements) processingEnv.getElementUtils();
        this.treeMaker = TreeMaker.instance(context);
        this.names = Names.instance(context);
        this.symtab = Symtab.instance(context);
        this.javacTrees = JavacTrees.instance(context);
        this.types = Types.instance(context);
        this.attr = Attr.instance(context);
        this.enter = Enter.instance(context);
        initJavacFunction();
    }
    
    protected void initJavacFunction() {
        try {
            Class.forName("javax.lang.model.element.ModuleElement");
            javacFunction = new Javac9Function();
        } catch (ClassNotFoundException e) {
            javacFunction = new Javac8Function();
        }
    }
    
    public JavacContext(JavacContext javacContext) {
        this(javacContext.processingEnv);
    }
    
    public JavacContext getContext() {
        return this;
    }
    
    public TreeMaker getTreeMaker() {
        return treeMaker;
    }
    
    public Names getNames() {
        return names;
    }
    
    public JavacTrees getJavacTrees() {
        return javacTrees;
    }
    
    /**
     * 是否包含某个注解
     *
     * @param sym
     * @param annotationClassName
     * @return
     */
    public boolean isContainAnnotation(Symbol sym, String annotationClassName) {
        for (Attribute.Compound annotationMirror : sym.getAnnotationMirrors()) {
            if (annotationMirror.type.toString().equals(annotationClassName)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获得对应的代码注释
     *
     * @param sym
     * @return
     */
    public String getComment(Symbol sym) {
        String docComment = elementUtils.getDocComment(sym);
        if (TruckUtils.isNotEmpty(docComment)) {
            ArrayList<String> commentList = new ArrayList<>();
            String[] comments = docComment.split("\\r?\\n");
            for (String comment : comments) {
                comment = comment.trim();
                if (TruckUtils.isNotEmpty(comment)) {
                    if (comment.startsWith("@author") || comment.startsWith("@date")) {
                        continue;
                    }
                    if (comment.startsWith("@return") || comment.startsWith("@param")) {
                        continue;
                    }
                    commentList.add(comment);
                }
            }
            docComment = PrivateStringUtils.join(commentList, TruckUtils.lineSeparator);
        }
        return docComment;
    }
    
    public JCTree.JCExpression stringBuild(Object... strings) {
        JCTree.JCExpression currentExpression = null;
        for (Object obj : strings) {
            JCTree.JCExpression nextExpression;
            if (obj instanceof String) {
                String string = (String) obj;
                if (string.startsWith("Ident_")) {
                    nextExpression = treeMaker.Ident(names.fromString(string.substring(6)));
                } else {
                    nextExpression = treeMaker.Literal(string);
                }
            } else if (obj instanceof JCTree.JCExpression) {
                nextExpression = (JCTree.JCExpression) obj;
            } else {
                throw new IllegalArgumentException("argType is " + obj.getClass().getName());
            }
            if (currentExpression == null) {
                currentExpression = nextExpression;
            } else {
                currentExpression = treeMaker.Binary(JCTree.Tag.PLUS, currentExpression, nextExpression);
            }
        }
        return currentExpression;
    }
    
    /**
     * 定义一个变量或属性
     *
     * @param typeName       类型名
     * @param varName        变量名
     * @param initExpression 初始化表达式
     * @return
     */
    public JCTree.JCVariableDecl initVar(String typeName, String varName, JCTree.JCExpression initExpression) {
        return initVar(0, typeName, varName, initExpression);
    }
    
    /**
     * 定义一个变量或属性
     *
     * @param modifiersFlag  修饰符
     * @param typeName       类型名
     * @param varName        变量名
     * @param initExpression 初始化表达式
     * @return
     */
    public JCTree.JCVariableDecl initVar(long modifiersFlag, String typeName, String varName,
            JCTree.JCExpression initExpression) {
        JCTree.JCExpression type;
        if ("long".equals(typeName)) {
            type = treeMaker.TypeIdent(TypeTag.LONG);
        } else if ("int".equals(typeName)) {
            type = treeMaker.TypeIdent(TypeTag.INT);
        } else if ("short".equals(typeName)) {
            type = treeMaker.TypeIdent(TypeTag.SHORT);
        } else if ("double".equals(typeName)) {
            type = treeMaker.TypeIdent(TypeTag.DOUBLE);
        } else if ("boolean".equals(typeName)) {
            type = treeMaker.TypeIdent(TypeTag.BOOLEAN);
        } else if ("char".equals(typeName)) {
            type = treeMaker.TypeIdent(TypeTag.CHAR);
        } else if ("float".equals(typeName)) {
            type = treeMaker.TypeIdent(TypeTag.FLOAT);
        } else if ("byte".equals(typeName)) {
            type = treeMaker.TypeIdent(TypeTag.BYTE);
        } else {
            type = initExp(typeName);
        }
        return treeMaker.VarDef(treeMaker.Modifiers(modifiersFlag), names.fromString(varName), type, initExpression);
    }
    
    /**
     * 初始化一个表达式  可以是属性也可以是方法
     *
     * @param fieldAccess 方法名
     * @param args        参数
     * @return
     */
    public JCTree.JCExpression initExp(String fieldAccess, JCTree.JCExpression... args) {
        String[] fields = fieldAccess.split("\\.");
        JCTree.JCExpression currentExpression = null;
        int argIndex = 0;
        int index = 0;
        for (String field : fields) {
            int argNum = -1;
            int startIndex = field.indexOf("(");
            int endIndex = field.indexOf(")");
            if (startIndex != -1 && endIndex != -1) {
                String str = field.substring(startIndex + 1, endIndex);
                if (str.length() > 0) {
                    argNum = Integer.parseInt(str);
                } else {
                    argNum = 0;
                }
                field = field.substring(0, startIndex);
            }
            
            if (index == 0) {
                boolean isLiteral = false;
                if (field.startsWith("\"") && field.endsWith("\"")) {
                    field = field.substring(1, field.length() - 1);
                    isLiteral = true;
                } else if ("true".equals(field) || "false".equals(field)) {
                    isLiteral = true;
                } else {
                    try {
                        Double.parseDouble(field);
                        isLiteral = true;
                    } catch (Exception e) {
                    }
                }
                if (isLiteral) {
                    currentExpression = treeMaker.Literal(field);
                } else {
                    currentExpression = treeMaker.Ident(names.fromString(field));
                }
            } else {
                currentExpression = treeMaker.Select(currentExpression, names.fromString(field));
            }
            
            if (argNum != -1) {
                ListBuffer<JCTree.JCExpression> listBuffer = new ListBuffer<>();
                if (argNum > 0) {
                    for (int i = 0; i < argNum; i++) {
                        listBuffer.append(args[argIndex]);
                        argIndex++;
                    }
                }
                if (index == fields.length - 1 && argIndex != args.length) {
                    for (; argIndex < args.length; argIndex++) {
                        listBuffer.append(args[argIndex]);
                    }
                }
                currentExpression = treeMaker.Apply(List.nil(), currentExpression, listBuffer.toList());
            }
            index++;
        }
        return currentExpression;
    }
    
    /**
     * 单独执行一行代码表达式 通常是单独一行代码来执行某个方法
     *
     * @param fieldAccess
     * @param args
     * @return
     */
    public JCTree.JCExpressionStatement execExp(String fieldAccess, JCTree.JCExpression... args) {
        JCTree.JCExpression exp = initExp(fieldAccess, args);
        return treeMaker.Exec(exp);
    }
    
    /**
     * 初始化赋值语句  xxx = 表达式
     *
     * @param targetVarName 结果变量
     * @param exeExpression
     * @return
     */
    public JCTree.JCAssign initAssign(String targetVarName, JCTree.JCExpression exeExpression) {
        return treeMaker.Assign(initExp(targetVarName), exeExpression);
    }
    
    /**
     * 单独执行赋值语句  xxx = 表达式 通常是单独一行代码来执行
     *
     * @param targetVarName 结果变量
     * @param exeExpression
     * @return
     */
    public JCTree.JCExpressionStatement execAssign(String targetVarName, JCTree.JCExpression exeExpression) {
        return treeMaker.Exec(initAssign(targetVarName, exeExpression));
    }
    
    /**
     * 获得类声明描述 (忽略泛型)
     *
     * @param type
     * @return
     */
    public SymbolClassType getSymbolClassType(Type type) {
        return getSymbolClassType(type, null, null);
    }
    
    /**
     * 获得类声明描述
     *
     * @param type                  传入的类型
     * @param sourceSymbolClassType 类的声明（在类的泛型推断中会用到）
     * @param argClassTypes         参数的类型声明（在方法的泛型推断中会用到）
     * @return
     */
    public SymbolClassType getSymbolClassType(Type type, SymbolClassType sourceSymbolClassType,
            SymbolClassType[] argClassTypes) {
        if (type instanceof Type.ArrayType) {
            return getSymbolClassType(((Type.ArrayType) type).elemtype, sourceSymbolClassType, argClassTypes)
                    .isArray(true);
        } else if (type instanceof Type.ClassType) {
            Symbol symbol = type.tsym;
            List<Type> params = type.allparams();
            SymbolClassType[] typeArgs = new SymbolClassType[params.size()];
            int index = 0;
            for (Type param : params) {
                typeArgs[index] = getSymbolClassType(param, sourceSymbolClassType, argClassTypes);
                index++;
            }
            return new SymbolClassType((Symbol.ClassSymbol) symbol, typeArgs);
        } else if (type instanceof Type.TypeVar) {
            return getSymbolClassTypeFromTypeVar((Type.TypeVar) type, sourceSymbolClassType, argClassTypes);
        } else if (type instanceof Type.JCVoidType) {
            return new SymbolClassType().isVoid(true);
        } else if (type instanceof Type.WildcardType) {
            Type.WildcardType wildcardType = (Type.WildcardType) type;
            return getSymbolClassType(wildcardType.type, sourceSymbolClassType, argClassTypes);
        }
        throw new IllegalArgumentException();
    }
    
    /**
     * 获得泛型声明的实际类型
     *
     * @param type
     * @param sourceSymbolClassType
     * @param argClassTypes
     * @return
     */
    private SymbolClassType getSymbolClassTypeFromTypeVar(Type.TypeVar type, SymbolClassType sourceSymbolClassType,
            SymbolClassType[] argClassTypes) {
        if (sourceSymbolClassType == null || sourceSymbolClassType.symbol == null) {
            return null;
        }
        Symbol.TypeVariableSymbol tsym = (Symbol.TypeVariableSymbol) type.tsym;
        if (tsym.owner instanceof Symbol.ClassSymbol) {
            return getSymbolClassTypeFromTypeVarLoop(type, (Type.ClassType) sourceSymbolClassType.symbol.type,
                    sourceSymbolClassType);
        } else if (tsym.owner instanceof Symbol.MethodSymbol) {
            Symbol.MethodSymbol symbol = (Symbol.MethodSymbol) tsym.owner;
            if (argClassTypes != null && argClassTypes.length > 0 && symbol.type instanceof Type.ForAll) {
                Type.ForAll allType = (Type.ForAll) symbol.type;
                Type.MethodType qtype = (Type.MethodType) allType.qtype;
                int argIndex = qtype.argtypes.indexOf(type);
                if (argIndex > -1) {
                    return argClassTypes[argIndex];
                }
            }
            return getSymbolClassType(type.bound);
        }
        throw new IllegalArgumentException();
    }
    
    /**
     * 递归计算泛型的实际类型
     *
     * @param typeVar
     * @param currentClassType
     * @param sourceSymbolClassType
     * @return
     */
    private SymbolClassType getSymbolClassTypeFromTypeVarLoop(Type typeVar, Type.ClassType currentClassType,
            SymbolClassType sourceSymbolClassType) {
        Symbol.TypeVariableSymbol tsym = (Symbol.TypeVariableSymbol) typeVar.tsym;
        if (tsym.owner.toString().equals(sourceSymbolClassType.symbol.toString())) {
            int typeIndex = tsym.owner.type.allparams().indexOf(typeVar);
            if (sourceSymbolClassType.typeArgs != null && sourceSymbolClassType.typeArgs.length > typeIndex) {
                return sourceSymbolClassType.typeArgs[typeIndex];
            }
            throw new IllegalArgumentException();
        }
        ArrayList<Type> parentTypes = getParentType(currentClassType);
        for (Type parentType : parentTypes) {
            if (parentType.tsym.toString().equals(tsym.owner.toString())) {
                int typeIndex = tsym.owner.type.allparams().indexOf(typeVar);
                Type type = parentType.allparams().get(typeIndex);
                if (type instanceof Type.ClassType) {
                    return getSymbolClassType(type);
                } else if (type instanceof Type.TypeVar) {
                    return getSymbolClassTypeFromTypeVarLoop(type, (Type.ClassType) sourceSymbolClassType.symbol.type,
                            sourceSymbolClassType);
                }
            } else {
                currentClassType = (Type.ClassType) parentType.tsym.type;
                SymbolClassType parentSymbolClassType = getSymbolClassTypeFromTypeVarLoop(typeVar, currentClassType,
                        sourceSymbolClassType);
                if (parentSymbolClassType != null) {
                    return parentSymbolClassType;
                }
            }
        }
        return null;
    }
    
    /**
     * 获得某个类的属性声明
     *
     * @param classSymbol
     * @param nameStr
     * @return
     */
    public Symbol.VarSymbol getFieldTypeMembersSymbol(Symbol.ClassSymbol classSymbol, String nameStr) {
        for (Symbol member : getAccessibleMembers(classSymbol)) {
            if (member instanceof Symbol.VarSymbol) {
                if (nameStr.equals(member.name.toString())) {
                    return (Symbol.VarSymbol) member;
                }
            }
        }
        return null;
    }
    
    /**
     * 获得某个类的同名方法声明
     *
     * @param classSymbol
     * @param nameStr
     * @return
     */
    public ArrayList<Symbol.MethodSymbol> getMethodMembersSymbolList(Symbol.ClassSymbol classSymbol, String nameStr) {
        ArrayList<Symbol.MethodSymbol> symbols = new ArrayList<>();
        for (Symbol member : getAccessibleMembers(classSymbol)) {
            if (member instanceof Symbol.MethodSymbol) {
                if (nameStr.equals(member.name.toString())) {
                    symbols.add((Symbol.MethodSymbol) member);
                }
                if (nameStr.equals(classSymbol.name.toString()) && "init".equals(member.name.toString())) {
                    symbols.add((Symbol.MethodSymbol) member);
                }
            }
        }
        return symbols;
    }
    
    /**
     * 获得某个类的内部类声明
     *
     * @param classSymbol
     * @param nameStr
     * @return
     */
    public Symbol.ClassSymbol getNestClassSymbol(Symbol.ClassSymbol classSymbol, String nameStr) {
        for (Symbol member : getAccessibleMembers(classSymbol)) {
            if (member instanceof Symbol.ClassSymbol) {
                if (nameStr.equals(member.name.toString())) {
                    return (Symbol.ClassSymbol) member;
                }
            }
        }
        return null;
    }
    
    /**
     * 获得某个类可以访问的所有成员
     *
     * @param classSymbol
     * @return
     */
    private ArrayList<Symbol> getAccessibleMembers(Symbol.ClassSymbol classSymbol) {
        Map<String, Symbol> symbolMap = new HashMap<>();
        for (Symbol member : javacFunction.getMemberSymbols(classSymbol.members())) {
            symbolMap.put(member.toString(), member);
        }
        findParentAccessibleMembers(classSymbol, symbolMap);
        return new ArrayList<>(symbolMap.values());
    }
    
    /**
     * 获得某个类的父类可以访问的所有成员
     *
     * @param classSymbol
     * @param symbolMap
     */
    private void findParentAccessibleMembers(Symbol classSymbol, Map<String, Symbol> symbolMap) {
        ArrayList<Type> parentTypes = getParentType((Type.ClassType) classSymbol.type);
        for (Type parentType : parentTypes) {
            Symbol currentClassSymbol = parentType.tsym;
            for (Symbol member : javacFunction.getMemberSymbols(classSymbol.members())) {
                Set<Flags.Flag> flagSet = Flags.asFlagSet(member.flags());
                if (flagSet.contains(Flags.Flag.ABSTRACT) && !flagSet.contains(Flags.Flag.DEFAULT)) {
                    continue;
                }
                if (flagSet.contains(Flags.Flag.INTERFACE) && !flagSet.contains(Flags.Flag.DEFAULT)) {
                    continue;
                }
                if (flagSet.contains(Flags.Flag.PRIVATE)) {
                    continue;
                }
                symbolMap.putIfAbsent(member.toString(), member);
            }
        }
        for (Type parentType : parentTypes) {
            findParentAccessibleMembers(parentType.tsym, symbolMap);
        }
    }
    
    /**
     * 获得某个类的所有父类
     *
     * @param classType
     * @return
     */
    private ArrayList<Type> getParentType(Type.ClassType classType) {
        ArrayList<Type> parentTypes = new ArrayList<>();
        parentTypes.add(classType.supertype_field);
        parentTypes.addAll(classType.interfaces_field);
        parentTypes = parentTypes.stream().filter(x -> x != null && x != Type.noType)
                .collect(Collectors.toCollection(ArrayList::new));
        return parentTypes;
    }
    
    /**
     * @param source   子类
     * @param baseType 父类或接口
     * @return 返回-1则不存在父子关系，否则表示多少代父子关系
     */
    public int typeIndexOf(Type source, Type baseType) {
        if (source instanceof Type.JCPrimitiveType && baseType instanceof Type.JCPrimitiveType) {
            return source.toString().equals(baseType.toString()) ? 0 : -1;
        } else if (source instanceof Type.JCPrimitiveType || baseType instanceof Type.JCPrimitiveType) {
            Type.JCPrimitiveType primitiveType;
            if (source instanceof Type.JCPrimitiveType) {
                primitiveType = (Type.JCPrimitiveType) source;
            } else {
                primitiveType = (Type.JCPrimitiveType) baseType;
            }
            String primitiveTypeStr = primitiveType.toString();
            if (primitiveTypeStr.equals("int") && source.toString().equals("java.lang.Integer")) {
                return 10;
            }
            if (primitiveTypeStr.equals("long") && source.toString().equals("java.lang.Long")) {
                return 10;
            }
            if (primitiveTypeStr.equals("short") && source.toString().equals("java.lang.Short")) {
                return 10;
            }
            if (primitiveTypeStr.equals("double") && source.toString().equals("java.lang.Double")) {
                return 10;
            }
            if (primitiveTypeStr.equals("float") && source.toString().equals("java.lang.Float")) {
                return 10;
            }
            if (primitiveTypeStr.equals("char") && source.toString().equals("java.lang.Character")) {
                return 10;
            }
            if (primitiveTypeStr.equals("byte") && source.toString().equals("java.lang.Byte")) {
                return 10;
            }
            if (primitiveTypeStr.equals("boolean") && source.toString().equals("java.lang.Boolean")) {
                return 10;
            } else {
                return -1;
            }
        } else {
            return typeIndexOf(new ArrayList(Arrays.asList((Type.ClassType) source)) , baseType.toString());
        }
    }
    
    /**
     * @param source        子类列表
     * @param baseClassName 父类或接口的类名
     * @return 返回-1则不存在父子关系，否则表示多少代父子关系
     */
    private int typeIndexOf(ArrayList<Type.ClassType> source, String baseClassName) {
        int deep = 0;
        while (source.size() > 0) {
            ArrayList parents = new ArrayList<>();
            for (Type.ClassType classType : source) {
                if (baseClassName.equals(classType.toString())) {
                    return deep;
                }
                if (classType.supertype_field != null && classType.supertype_field instanceof Type.ClassType) {
                    parents.add(classType.supertype_field);
                }
                if (classType.interfaces_field != null && classType.interfaces_field.size() > 0) {
                    for (Type type : classType.interfaces_field) {
                        if (type != null && type instanceof Type.ClassType) {
                            parents.add(type);
                        }
                    }
                }
            }
            source = parents;
            deep++;
        }
        return -1;
    }
}
