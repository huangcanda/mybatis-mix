package org.wanghailu.mybatismix.apt.translator;

import com.sun.tools.javac.tree.JCTree;
import org.wanghailu.mybatismix.apt.model.SymbolClassType;

import java.util.HashMap;
import java.util.Map;

/**
 * 语法树节点上下文
 */
public class TreeNodeContext<T extends TreeNodeContext> {

    public JCTree parentNode;

    public JCTree bodyNode;

    public boolean isBlockStat;

    public boolean canAddCode;

    public boolean isFunction;

    public T oldTreeNodeContext;

    /**
     * 当前上下文中，生效的自定义变量
     */
    protected Map<String, SymbolClassType> workVarMap = new HashMap<>();

    public TreeNodeContext(JCTree parentNode, JCTree bodyNode, boolean isBlockStat, boolean canAddCode, boolean isFunction, T oldTreeNodeContext) {
        this.parentNode = parentNode;
        this.bodyNode = bodyNode;
        this.isBlockStat = isBlockStat;
        this.canAddCode = canAddCode;
        this.isFunction = isFunction;
        this.oldTreeNodeContext = oldTreeNodeContext;
        if (oldTreeNodeContext != null) {
            workVarMap.putAll(oldTreeNodeContext.getWorkVarMap());
        }
    }

    public JCTree handleTreeBody() {
        return bodyNode;
    }

    public Map<String, SymbolClassType> getWorkVarMap() {
        if (isBlockStat) {
            return oldTreeNodeContext.getWorkVarMap();
        } else {
            return workVarMap;
        }
    }
}
