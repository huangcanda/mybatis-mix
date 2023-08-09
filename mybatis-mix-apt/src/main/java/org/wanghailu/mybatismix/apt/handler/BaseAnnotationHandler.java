package org.wanghailu.mybatismix.apt.handler;

import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import org.wanghailu.mybatismix.apt.model.JavacContext;

/**
 * @author cdhuang
 * @date 2021/11/19
 */
public abstract class BaseAnnotationHandler<T extends JavacContext> {

    protected T context;

    protected TreeMaker treeMaker;

    protected Names names;

    public BaseAnnotationHandler(T context) {
        this.context = context;
        treeMaker = context.getTreeMaker();
        names = context.getNames();
    }

    /**
     * 处理动作
     * @return
     */
    public abstract boolean handle();
}
