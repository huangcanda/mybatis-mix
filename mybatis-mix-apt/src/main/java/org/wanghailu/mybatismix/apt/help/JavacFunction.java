package org.wanghailu.mybatismix.apt.help;

import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.util.Name;

import java.util.ArrayList;

/**
 * @author cdhuang
 * @date 2023/2/1
 */
public interface JavacFunction {
    
    Symbol getSymbolFromClasses(Symtab symtab, Name name);
    
    Symbol getSymbolFromPackages(Symtab symtab, Name name);
    
    Iterable<Symbol> getMemberSymbols(Scope members);
    
    Symbol getSymbolFromImport(Scope score, Name name);
    
    ArrayList<Symbol.MethodSymbol> getMethodSymbolFromImportMethod(Scope score, String name);
}
