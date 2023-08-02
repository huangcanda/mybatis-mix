package org.wanghailu.mybatismix.apt.help;

import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.util.Name;
import org.wanghailu.mybatismix.util.ReflectUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

/**
 * jdk8支持
 */
public class Javac8Function implements JavacFunction{
    
    @Override
    public Symbol getSymbolFromClasses(Symtab symtab, Name name) {
        Map map = (Map) ReflectUtils.getFieldValue(symtab,"classes");
        return (Symbol) map.get(name);
    }
    
    
    @Override
    public Symbol getSymbolFromPackages(Symtab symtab,Name name) {
        Map map = (Map) ReflectUtils.getFieldValue(symtab,"packages");
        return (Symbol) map.get(name);
    }
    
    @Override
    public Iterable<Symbol> getMemberSymbols(Scope members) {
        Method method = ReflectUtils.getMethod(members.getClass(),"getElements");
        return (Iterable<Symbol>) ReflectUtils.invokeMethod(method,members);
    }
    
    @Override
    public Symbol getSymbolFromImport(Scope score, Name name) {
        Method method = ReflectUtils.getMethod(score.getClass(),"lookup",Name.class);
        Object obj = ReflectUtils.invokeMethod(method,score,name);
        return (Symbol) ReflectUtils.getFieldValue(obj,"sym");
    }
    
    @Override
    public ArrayList<Symbol.MethodSymbol> getMethodSymbolFromImportMethod(Scope score, String name) {
        ArrayList<Symbol.MethodSymbol> list = new ArrayList<>();
        Method method = ReflectUtils.getMethod(score.getClass(),"lookup",Name.class);
        Object entry = ReflectUtils.invokeMethod(method,score,name);
        while (true){
            Object sym = ReflectUtils.getFieldValue(entry,"sym");
            if(sym!=null){
                if (sym instanceof Symbol.MethodSymbol) {
                    list.add((Symbol.MethodSymbol) sym);
                }
                Method nextMethod = ReflectUtils.getMethod(entry.getClass(),"next");
                entry = ReflectUtils.invokeMethod(nextMethod,entry);
            }else{
                break;
            }
        }
        return list;
    }
}
