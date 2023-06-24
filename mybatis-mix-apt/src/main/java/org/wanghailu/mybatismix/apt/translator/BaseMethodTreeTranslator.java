package org.wanghailu.mybatismix.apt.translator;

import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Names;
import org.wanghailu.mybatismix.apt.model.JavacMethodContext;


/**
 * @author cdhuang
 * @date 2021/11/30
 */
public abstract class BaseMethodTreeTranslator extends TreeTranslator {

    protected JavacMethodContext context;

    protected TreeMaker treeMaker;

    protected Names names;

    public BaseMethodTreeTranslator(JavacMethodContext context) {
        this.context = context;
        this.treeMaker = context.getTreeMaker();
        this.names = context.getNames();
    }

}
