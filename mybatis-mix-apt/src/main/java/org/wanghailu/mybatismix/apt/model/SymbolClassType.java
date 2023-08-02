package org.wanghailu.mybatismix.apt.model;

import com.sun.tools.javac.code.Symbol;

/**
 * SymbolClassType
 */
public class SymbolClassType {

    public Symbol.ClassSymbol symbol;

    public boolean isArray;

    public boolean isPrimitive;

    public SymbolClassType[] typeArgs;

    public boolean isPackage;

    public boolean isVoid;

    public SymbolClassType() {
    }

    public SymbolClassType(Symbol.ClassSymbol symbol) {
        this.symbol = symbol;
    }

    public SymbolClassType(Symbol.ClassSymbol symbol, SymbolClassType[] typeArgs) {
        this.symbol = symbol;
        this.typeArgs = typeArgs;
    }

    public SymbolClassType(Symbol.ClassSymbol symbol, boolean isArray) {
        this.symbol = symbol;
        this.isArray = isArray;
    }

    public SymbolClassType(Symbol.ClassSymbol symbol, boolean isArray, boolean isPrimitive, SymbolClassType[] typeArgs) {
        this.symbol = symbol;
        this.isArray = isArray;
        this.isPrimitive = isPrimitive;
        this.typeArgs = typeArgs;
    }

    public SymbolClassType isArray(boolean isArray) {
        this.isArray = isArray;
        return this;
    }

    public SymbolClassType isPrimitive(boolean isPrimitive) {
        this.isPrimitive = isPrimitive;
        return this;
    }

    public SymbolClassType isPackage(boolean isPackage) {
        this.isPackage = isPackage;
        return this;
    }

    public SymbolClassType isVoid(boolean isVoid) {
        this.isVoid = isVoid;
        return this;
    }
}
