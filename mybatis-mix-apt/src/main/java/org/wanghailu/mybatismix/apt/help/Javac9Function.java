package org.wanghailu.mybatismix.apt.help;

import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.util.Name;
import org.wanghailu.mybatismix.util.ReflectUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * jdk9+支持
 */
public class Javac9Function implements JavacFunction{
    
    @Override
    public Symbol getSymbolFromClasses(Symtab symtab,Name name) {
        Map<Name, Map> map = (Map<Name, Map>) ReflectUtils.getFieldValue(symtab,"classes");
        Map<Object,Symbol> symMap = map.get(name);
        if(symMap==null){
            return null;
        }
        return getOneForIterator(symMap.values());
    }
    
    
    @Override
    public Symbol getSymbolFromPackages(Symtab symtab,Name name) {
        Map<Name, Map> map = (Map<Name, Map>) ReflectUtils.getFieldValue(symtab,"packages");
        Map<Object,Symbol> symMap = map.get(name);
        if(symMap==null){
            return null;
        }
        return getOneForIterator(symMap.values());
    }
    
    @Override
    public Iterable<Symbol> getMemberSymbols(Scope members) {
        Method method = ReflectUtils.getMethod(members.getClass(),"getSymbols");
        return (Iterable<Symbol>) ReflectUtils.invokeMethod(method,members);
    }
    
    @Override
    public Symbol getSymbolFromImport(Scope score, Name name) {
        Method method = ReflectUtils.getMethod(score.getClass(),"getSymbolsByName",Name.class);
        Iterable<Symbol> symbols= (Iterable<Symbol>) ReflectUtils.invokeMethod(method,score,name);
        return getOneForIterator(symbols);
    }
    
    @Override
    public ArrayList<Symbol.MethodSymbol> getMethodSymbolFromImportMethod(Scope score, String name) {
        ArrayList<Symbol.MethodSymbol> list = new ArrayList<>();
        Method method = ReflectUtils.getMethod(score.getClass(),"getSymbolsByName",Name.class);
        Iterable<Symbol> symbols= (Iterable<Symbol>) ReflectUtils.invokeMethod(method,score,name);
        for (Symbol symbol : symbols) {
            if (symbol instanceof Symbol.MethodSymbol) {
                list.add((Symbol.MethodSymbol) symbol);
            }
        }
        return list;
    }
    
    private <T> T getOneForIterator(Iterable<T> iterable) {
        Iterator<T> iterator = iterable.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }
}
