package org.wanghailu.mybatismix.apt.help;


import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import org.wanghailu.mybatismix.apt.model.JavacContext;

import java.util.ArrayList;
import java.util.Set;

/**
 * javac相关工具类
 */
public class JavacUtils {


    public static Symbol getSymbol(JCTree jcTree) {
        if (jcTree instanceof JCTree.JCClassDecl) {
            return ((JCTree.JCClassDecl) jcTree).sym;
        } else if (jcTree instanceof JCTree.JCMethodDecl) {
            return ((JCTree.JCMethodDecl) jcTree).sym;
        } else if (jcTree instanceof JCTree.JCVariableDecl) {
            return ((JCTree.JCVariableDecl) jcTree).sym;
        } else if (jcTree instanceof JCTree.JCIdent) {
            return ((JCTree.JCIdent) jcTree).sym;
        } else if (jcTree instanceof JCTree.JCFieldAccess) {
            return ((JCTree.JCFieldAccess) jcTree).sym;
        }
        throw new UnsupportedOperationException(jcTree.getClass().getName());
    }

    public static Symbol.MethodSymbol getCanLambdaMethodSymbol(Symbol symbol) {
        if (symbol instanceof Symbol.ClassSymbol == false) {
            return null;
        }
        Set<Flags.Flag> flagSet = Flags.asFlagSet(symbol.flags());
        if (flagSet.contains(Flags.Flag.INTERFACE)) {
            int abstractMethodCount = 0;
            Symbol.MethodSymbol result = null;
            for (Symbol member : JavacContext.javacFunction.getMemberSymbols(symbol.members())) {
                if (member instanceof Symbol.MethodSymbol) {
                    flagSet = Flags.asFlagSet(member.flags());
                    if (flagSet.contains(Flags.Flag.ABSTRACT) || flagSet.contains(Flags.Flag.INTERFACE)) {
                        if (!flagSet.contains(Flags.Flag.DEFAULT)) {
                            abstractMethodCount++;
                            result = (Symbol.MethodSymbol) member;
                        }
                    }
                }
            }
            if (abstractMethodCount == 1) {
                return result;
            }
        }
        return null;
    }

    public static ArrayList<JCTree.JCVariableDecl> getMethodVarList(JCTree.JCMethodDecl methodDecl) {
        ArrayList<JCTree.JCVariableDecl> varList = new ArrayList<>();
        methodDecl.accept(new TreeTranslator() {
            @Override
            public void visitVarDef(JCTree.JCVariableDecl jcVariableDecl) {
                if (!jcVariableDecl.name.toString().startsWith("var$$")) {
                    varList.add(jcVariableDecl);
                }
                super.visitVarDef(jcVariableDecl);
            }

            @Override
            public void visitLambda(JCTree.JCLambda jcLambda) {
                this.result = jcLambda;
            }

            @Override
            public void visitNewClass(JCTree.JCNewClass jcNewClass) {
                this.result = jcNewClass;
            }
        });
        return varList;
    }

    public static <T> List<T> listAppend(List<T> list, T obj) {
        return listAppend(list, obj, -1, false);
    }

    public static <T> List<T> listAppend(List<T> list, T obj, int index) {
        return listAppend(list, obj, index, false);
    }

    public static <T> List<T> listReplace(List<T> list, T obj, int index) {
        return listAppend(list, obj, index, true);
    }

    private static <T> List<T> listAppend(List<T> list, T obj, int index, boolean isReplace) {
        ListBuffer<T> listBuffer = new ListBuffer<>();
        boolean noAppend = true;
        int i = 0;
        if (list != null && list.size() > 0) {
            for (T t : list) {
                if (index == i) {
                    listBuffer.append(obj);
                    noAppend = false;
                } else if (isReplace) {
                    listBuffer.append(t);
                }
                if (!isReplace) {
                    listBuffer.append(t);
                }
                i++;
            }
        }
        if (noAppend) {
            listBuffer.append(obj);
        }
        return listBuffer.toList();
    }

}
