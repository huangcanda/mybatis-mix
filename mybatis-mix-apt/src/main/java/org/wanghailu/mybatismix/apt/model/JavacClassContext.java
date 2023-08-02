package org.wanghailu.mybatismix.apt.model;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import org.wanghailu.mybatismix.apt.help.JavacUtils;
import org.wanghailu.mybatismix.support.TwoTuple;
import org.wanghailu.mybatismix.util.TruckUtils;

import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author cdhuang
 * @date 2021/11/19
 */
public class JavacClassContext extends JavacContext {

    /**
     * 类的标识
     */
    protected Symbol.ClassSymbol classSymbol;

    /**
     * 类的声明
     */
    protected JCTree.JCClassDecl classDecl;

    protected JCTree.JCCompilationUnit compilationUnit;

    public JavacClassContext(JavacContext context, Symbol.ClassSymbol classSymbol) {
        super(context);
        this.classSymbol = classSymbol;
        classDecl = javacTrees.getTree(classSymbol);
        compilationUnit = (JCTree.JCCompilationUnit) javacTrees.getPath(classSymbol).getCompilationUnit();
    }
    
    public Symbol.ClassSymbol getClassSymbol() {
        return classSymbol;
    }

    public JCTree.JCClassDecl getClassDecl() {
        return classDecl;
    }


    public JavacClassContext attribute(){
        Env<AttrContext> env = enter.getEnv(classSymbol);
        attr.attrib(env);
        return this;
    }
    /**
     * 引入依赖的包，返回false则表示依赖包已经存在，所以没有执行导入动作
     *
     * @param fullClassName
     */
    public int importPackage(String fullClassName) {
        String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
        String packageName = fullClassName.substring(0, fullClassName.lastIndexOf("."));
        for (JCTree.JCImport jcImport : compilationUnit.getImports()) {
            if (jcImport.qualid != null && jcImport.qualid instanceof JCTree.JCFieldAccess) {
                JCTree.JCFieldAccess jcFieldAccess = (JCTree.JCFieldAccess) jcImport.qualid;
                try {
                    if (packageName.equals(jcFieldAccess.selected.toString()) && className.equals(jcFieldAccess.name.toString())) {
                        return 0;
                    }
                    if (className.equals(jcFieldAccess.name.toString())) {
                        messager.printMessage(Diagnostic.Kind.WARNING, "自动导入包" + fullClassName + "失败，存在同名的包" + jcFieldAccess.toString());
                        return -1;
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
        JCTree.JCImport jcImport = treeMaker.Import(treeMaker.Select(treeMaker.Ident(names.fromString(packageName)),
                names.fromString(className)), false);
        java.util.List<JCTree> trees = new ArrayList<>();
        trees.addAll(compilationUnit.defs);
        if (!trees.contains(jcImport)) {
            int index = trees.size() == 0 ? 0 : 1;
            trees.add(index, jcImport);
        }
        compilationUnit.defs = List.from(trees);
        return 1;
    }

    /**
     * 根据一个class声明字符串 或包字符串 获得 对应的 Symbol
     *
     * @param simpleName
     * @return
     */
    public Symbol getSymbolFromSimpleClassName(String simpleName) {
        if (simpleName.endsWith("[]")) {
            simpleName = simpleName.substring(0, simpleName.length() - 2);
        }
        Name name = names.fromString(simpleName);
        Symbol sym;
        if (simpleName.contains(".")) {
            sym = javacFunction.getSymbolFromClasses(symtab,name);
            if (sym == null) {
                sym = javacFunction.getSymbolFromPackages(symtab,name);
            }
            //是否是某个类的内部类
            if (sym == null) {
                String subName = simpleName.substring(0, simpleName.lastIndexOf("."));
                Symbol subSymbol = getSymbolFromSimpleClassName(subName);
                if (subSymbol instanceof Symbol.ClassSymbol) {
                    sym = this.getNestClassSymbol((Symbol.ClassSymbol) subSymbol, simpleName.substring(simpleName.lastIndexOf(".") + 1));
                }
            }
        } else {
            //引入的类
            sym = javacFunction.getSymbolFromImport(compilationUnit.namedImportScope,name);
            if (sym == null) {
                //内置java.lang.xxxx的类
                sym = javacFunction.getSymbolFromImport(compilationUnit.starImportScope,name);
            }
            //不含包名的类
            if (sym == null) {
                sym = javacFunction.getSymbolFromClasses(symtab,name);
            }
            //是否是内部类
            if (sym == null) {
                sym = this.getNestClassSymbol(this.getClassSymbol(), name.toString());
            }
            //和当前类同级目录的类
            if (sym == null) {
                sym = javacFunction.getSymbolFromClasses(symtab,names.fromString(compilationUnit.getPackageName().toString() + "." + name));
            }
            //或许这不是个类，只是个包名
            if (sym == null) {
                sym = javacFunction.getSymbolFromPackages(symtab,name);
            }
            if (sym == null) {
                if ("int".equals(simpleName)) {
                    sym = symtab.intType.tsym;
                } else if ("long".equals(simpleName)) {
                    sym = symtab.longType.tsym;
                } else if ("short".equals(simpleName)) {
                    sym = symtab.shortType.tsym;
                } else if ("float".equals(simpleName)) {
                    sym = symtab.floatType.tsym;
                } else if ("double".equals(simpleName)) {
                    sym = symtab.doubleType.tsym;
                } else if ("byte".equals(simpleName)) {
                    sym = symtab.byteType.tsym;
                } else if ("char".equals(simpleName)) {
                    sym = symtab.charType.tsym;
                } else if ("boolean".equals(simpleName)) {
                    sym = symtab.booleanType.tsym;
                }
            }
        }
        return sym;
    }

    public SymbolClassType getSymbolClassType(String str) {
        Symbol symbol = getSymbolFromSimpleClassName(str);
        if (symbol instanceof Symbol.PackageSymbol) {
            return new SymbolClassType().isPackage(true);
        } else {
            return new SymbolClassType((Symbol.ClassSymbol) symbol);
        }
    }

    /**
     * 根据一个class声明字符串 或包字符串 获得 对应的 Symbol
     *
     * @param expression
     * @return
     */
    public SymbolClassType getSymbolClassType(JCTree expression) {
        if (expression instanceof JCTree.JCTypeApply) {
            JCTree.JCTypeApply jcTypeApply = (JCTree.JCTypeApply) expression;
            SymbolClassType parentSymbolClassType = getSymbolClassType(jcTypeApply.clazz);
            SymbolClassType[] argSymbolClassTypes = new SymbolClassType[jcTypeApply.arguments.size()];
            int index = 0;
            for (JCTree.JCExpression argument : jcTypeApply.arguments) {
                argSymbolClassTypes[index] = getSymbolClassType(argument);
                index++;
            }
            return new SymbolClassType(parentSymbolClassType.symbol, argSymbolClassTypes);
        } else if (expression instanceof JCTree.JCArrayTypeTree) {
            JCTree.JCArrayTypeTree jcArrayTypeTree = (JCTree.JCArrayTypeTree) expression;
            SymbolClassType parentSymbolClassType = getSymbolClassType(jcArrayTypeTree.elemtype);
            return new SymbolClassType(parentSymbolClassType.symbol, true);
        } else {
            return new SymbolClassType((Symbol.ClassSymbol) getSymbolFromSimpleClassName(expression.toString()));
        }
    }

    /**
     * 推断表达式 返回的 类型的 ClassSymbol
     *
     * @param exp
     * @param varMap
     * @return
     */
    public SymbolClassType getExpressionSymbolClassType(JCTree.JCExpression exp, Map<String, SymbolClassType> varMap) {
        return getExpressionSymbolClassTypeLoop(exp, varMap);
    }


    /**
     * 递归进行类型推断
     *
     * @param exp
     * @param varMap
     * @return
     */
    private SymbolClassType getExpressionSymbolClassTypeLoop(JCTree.JCExpression exp, Map<String, SymbolClassType> varMap) {
        if (exp instanceof JCTree.JCParens) {
            exp = ((JCTree.JCParens) exp).expr;
        }
        if (exp instanceof JCTree.JCMethodInvocation) {
            JCTree.JCMethodInvocation methodInvocation = (JCTree.JCMethodInvocation) exp;
            TwoTuple<ArrayList<Symbol.MethodSymbol>, SymbolClassType> tuple = getMethodSymbols(methodInvocation, varMap);
            ArrayList<Symbol.MethodSymbol> methodSymbols = tuple.getFirst();
            Symbol.MethodSymbol methodSymbol = chooseMethodSymbol(methodSymbols, methodInvocation.args, varMap);
            if (methodInvocation.args.size() > 0) {
                SymbolClassType[] argClassTypes = new SymbolClassType[methodInvocation.args.size()];
                int argIndex = 0;
                for (JCTree.JCExpression arg : methodInvocation.args) {
                    argClassTypes[argIndex] = getExpressionSymbolClassTypeLoop(arg, varMap);
                    argIndex++;
                }
                return methodSymbol == null ? null : getSymbolClassType(methodSymbol.getReturnType(), tuple.getSecond(), argClassTypes);
            } else {
                return methodSymbol == null ? null : getSymbolClassType(methodSymbol.getReturnType(), tuple.getSecond(), null);
            }
        } else if (exp instanceof JCTree.JCFieldAccess) {
            JCTree.JCFieldAccess fieldAccess = (JCTree.JCFieldAccess) exp;
            SymbolClassType selectedSymbol = getExpressionSymbolClassTypeLoop(fieldAccess.selected, varMap);
            if (selectedSymbol.isPackage) {
                return getSymbolClassType(fieldAccess.toString());
            } else {
                Symbol.ClassSymbol classSymbol = selectedSymbol.symbol;
                Symbol varSymbol = getFieldTypeMembersSymbol(classSymbol, fieldAccess.name.toString());
                return getSymbolClassType(varSymbol.type, selectedSymbol, null);
            }
        } else if (exp instanceof JCTree.JCIdent) {
            String nameStr = exp.toString();
            if (nameStr.equals("this") || nameStr.equals("super")) {
                return new SymbolClassType(getClassSymbol());
            }
            SymbolClassType symbol = varMap.get(nameStr);
            if (symbol == null) {
                symbol = getSymbolClassType(exp.toString());
            }
            return symbol;
        } else if (exp instanceof JCTree.JCAssign) {
            return getExpressionSymbolClassTypeLoop(((JCTree.JCAssign) exp).lhs, varMap);
        } else if (exp instanceof JCTree.JCAssignOp) {
            return getExpressionSymbolClassTypeLoop(((JCTree.JCAssignOp) exp).lhs, varMap);
        } else if (exp instanceof JCTree.JCUnary) {
            return getExpressionSymbolClassTypeLoop(((JCTree.JCUnary) exp).arg, varMap);
        } else if (exp instanceof JCTree.JCLiteral) {
            JCTree.JCLiteral literal = (JCTree.JCLiteral) exp;
            if (literal.typetag == TypeTag.CLASS) {
                return getSymbolClassType("java.lang.String");
            } else {
                return getSymbolClassType(literal.typetag.toString().toLowerCase());
            }
        } else if (exp instanceof JCTree.JCNewClass) {
            JCTree.JCNewClass newClass = (JCTree.JCNewClass) exp;
            return getSymbolClassType(newClass.clazz);
        } else if (exp instanceof JCTree.JCTypeCast) {
            JCTree.JCTypeCast typeCast = (JCTree.JCTypeCast) exp;
            return getSymbolClassType(typeCast.clazz);
        } else if (exp instanceof JCTree.JCNewArray) {
            return getExpressionSymbolClassTypeLoop(((JCTree.JCNewArray) exp).elemtype, varMap).isArray(true);
        } else if (exp instanceof JCTree.JCArrayAccess) {
            return getExpressionSymbolClassTypeLoop(((JCTree.JCArrayAccess) exp).indexed, varMap).isArray(false);
        } else if (exp instanceof JCTree.JCInstanceOf) {
            return getSymbolClassType("boolean");
        } else if (exp instanceof JCTree.JCLambda) {
            return new SymbolClassType();
        } else if (exp instanceof JCTree.JCMemberReference) {
            return new SymbolClassType();
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * 根据方法的语法树结构推断出该方法的多个方法声明
     *
     * @param methodInvocation
     * @param varMap
     * @return
     */
    public TwoTuple<ArrayList<Symbol.MethodSymbol>, SymbolClassType> getMethodSymbols(JCTree.JCMethodInvocation methodInvocation, Map<String, SymbolClassType> varMap) {
        JCTree.JCExpression exp = methodInvocation.meth;
        if (exp instanceof JCTree.JCFieldAccess) {
            JCTree.JCFieldAccess fieldAccess = (JCTree.JCFieldAccess) exp;
            SymbolClassType selectedSymbol = getExpressionSymbolClassType(fieldAccess.selected, varMap);
            ArrayList<Symbol.MethodSymbol> methodSymbols = getMethodMembersSymbolList(selectedSymbol.symbol, fieldAccess.name.toString());
            return new TwoTuple<>(methodSymbols, selectedSymbol);
        } else if (exp instanceof JCTree.JCIdent) {
            String nameStr = exp.toString();
            ArrayList<Symbol.MethodSymbol> methodSymbols = getMethodMembersSymbolList(getClassSymbol(), nameStr);
            if (methodSymbols.size() == 0) {
                methodSymbols.addAll(javacFunction.getMethodSymbolFromImportMethod(compilationUnit.namedImportScope,nameStr));
            }
            return new TwoTuple<>(methodSymbols, null);
        }
        throw new IllegalArgumentException();
    }

    /**
     * 根据参数和上下文，从多个方法声明里选择一个方法
     *
     * @param methodSymbols
     * @param args
     * @param varMap
     * @return
     */
    public Symbol.MethodSymbol chooseMethodSymbol(ArrayList<Symbol.MethodSymbol> methodSymbols, List<JCTree.JCExpression> args, Map<String, SymbolClassType> varMap) {
        if (args != null || args.size() > 0) {
            methodSymbols = methodSymbols.stream().filter(x -> x.params().size() == args.size()).collect(Collectors.toCollection(ArrayList::new));
            if (methodSymbols.size() > 1) {
                TreeMap<Integer, Symbol.MethodSymbol> treeMap = new TreeMap<>();
                for (Symbol.MethodSymbol methodSymbol : methodSymbols) {
                    int deepCount = 0;
                    int argIndex = 0;
                    for (Symbol.VarSymbol param : methodSymbol.params) {
                        JCTree.JCExpression arg = args.get(argIndex);
                        Symbol.ClassSymbol symbol = getExpressionSymbolClassType(arg, varMap).symbol;
                        int deep = 20;
                        if (symbol == null) {
                            if (arg instanceof JCTree.JCLambda) {
                                JCTree.JCLambda jcLambda = (JCTree.JCLambda) arg;
                                Symbol.MethodSymbol lambdaMethodSymbol = JavacUtils.getCanLambdaMethodSymbol(param.type.tsym);
                                deep = checkLambdaWithMethodSymbol(lambdaMethodSymbol, jcLambda, varMap);
                            }
                        } else {
                            deep = typeIndexOf(symbol.type, param.type);
                        }
                        if (deep == -1) {
                            deepCount = -1;
                            break;
                        }
                        deepCount += deep;
                        argIndex++;
                    }
                    if (deepCount > -1) {
                        treeMap.put(deepCount, methodSymbol);
                    }
                }
                methodSymbols = new ArrayList(Arrays.asList(treeMap.values().iterator().next()));
            }
        }
        return TruckUtils.isEmpty(methodSymbols) ? null : methodSymbols.get(0);
    }

    /**
     * 判断方法声明和lambda表达式是否匹配
     *
     * @param lambdaMethodSymbol
     * @param jcLambda
     * @param varMap
     * @return 返回-1则不匹配，返回值越小则表示越匹配
     */
    private int checkLambdaWithMethodSymbol(Symbol.MethodSymbol lambdaMethodSymbol, JCTree.JCLambda jcLambda, Map<String, SymbolClassType> varMap) {
        if (lambdaMethodSymbol == null) {
            return -1;
        }
        if (lambdaMethodSymbol.params().size() != jcLambda.params.size()) {
            return -1;
        }

        if (lambdaMethodSymbol.type instanceof Type.MethodType == false) {
            return -1;
        }
        Map<String, SymbolClassType> varMap2 = new HashMap<>(varMap);
        int index = 0;
        for (Symbol.VarSymbol varSymbol : lambdaMethodSymbol.params()) {
            varMap2.put(jcLambda.params.get(index).name.toString(), getSymbolClassType(varSymbol.type));
            index++;
        }
        boolean isVoidType = isVoidType(jcLambda, varMap2);
        Type lambdaMethodReturnType = ((Type.MethodType) lambdaMethodSymbol.type).restype;
        if (lambdaMethodReturnType instanceof Type.JCVoidType) {
            return isVoidType ? 1 : 5;
        } else {
            return isVoidType ? 5 : 1;
        }
    }

    /**
     * 判断一段代码是不是没有返回值（在推断lambda表达式时用到）
     *
     * @param body
     * @param varMap
     * @return
     */
    private boolean isVoidType(JCTree body, Map<String, SymbolClassType> varMap) {
        if (body instanceof JCTree.JCLambda) {
            return isVoidType(((JCTree.JCLambda) body).body, varMap);
        } else if (body instanceof JCTree.JCExpression) {
            return getExpressionSymbolClassType((JCTree.JCExpression) body, varMap).isVoid;
        } else {
            AtomicBoolean haveReturn = new AtomicBoolean(false);
            body.accept(new TreeTranslator() {
                @Override
                public void visitReturn(JCTree.JCReturn jcReturn) {
                    super.visitReturn(jcReturn);
                    haveReturn.set(true);
                }
            });
            return !haveReturn.get();
        }
    }
}
