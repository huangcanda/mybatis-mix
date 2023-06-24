package org.wanghailu.mybatismix.apt.handler;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import org.wanghailu.mybatismix.annotation.AutoComment;
import org.wanghailu.mybatismix.annotation.Comment;
import org.wanghailu.mybatismix.apt.help.JavacUtils;
import org.wanghailu.mybatismix.apt.model.JavacClassContext;
import org.wanghailu.mybatismix.util.TruckUtils;

import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

/**
 * @author cdhuang
 * @date 2021/12/17
 */
public class AddCommentAnnotationHandler extends BaseAnnotationHandler<JavacClassContext> {

    private CommentMeta commentMeta;

    public AddCommentAnnotationHandler(JavacClassContext javacClassContext) {
        super(javacClassContext);
        AutoComment autoComment = javacClassContext.getClassSymbol().getAnnotation(AutoComment.class);
        commentMeta = getCommentMeta(autoComment);
    }

    @Override
    public boolean handle() {
        context.importPackage(commentMeta.getClassName());
        commentClass(context.getClassDecl());
        return true;
    }

    private void commentClass(JCTree.JCClassDecl jcClassDecl) {
        addComment(jcClassDecl.sym, jcClassDecl.mods);
        for (JCTree def : jcClassDecl.defs) {
            if (def instanceof JCTree.JCVariableDecl) {
                JCTree.JCVariableDecl jcVariableDecl = (JCTree.JCVariableDecl) def;
                addComment(jcVariableDecl.sym, jcVariableDecl.mods);
            } else if (def instanceof JCTree.JCMethodDecl) {
                JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) def;
                addComment(jcMethodDecl.sym, jcMethodDecl.mods);
            } else if (def instanceof JCTree.JCClassDecl) {
                JCTree.JCClassDecl jcClassDeclInner = (JCTree.JCClassDecl) def;
                commentClass(jcClassDeclInner);
            }
        }
    }

    private void addComment(Symbol sym, JCTree.JCModifiers mods) {
        if(sym==null){
            return;
        }
        if (context.isContainAnnotation(sym,commentMeta.getClassName())) {
            return;
        }
        String docComment = context.getComment(sym);
        if (TruckUtils.isNotEmpty(docComment)) {
            JCTree.JCAnnotation jcAnnotation = treeMaker.Annotation(context.initExp(commentMeta.getSimpleClassName()), List.of(context.initAssign(commentMeta.getCommentFieldName(), treeMaker.Literal(docComment))));
            mods.annotations = JavacUtils.listAppend(mods.annotations, jcAnnotation);
        }
    }

    private CommentMeta getCommentMeta(AutoComment obj) {
        String className = null;
        try {
            className = obj.value().getName();
        } catch (Exception e) {
            if (e instanceof MirroredTypeException) {
                MirroredTypeException mirroredTypeException = (MirroredTypeException) e;
                TypeMirror typeMirror = mirroredTypeException.getTypeMirror();
                if (typeMirror instanceof Type.ClassType) {
                    Type.ClassType classType = (Type.ClassType) typeMirror;
                    className = classType.toString();
                }
            }
        }
        if (className == null) {
            className = Comment.class.getName();
        }
        String commentFieldName;
        try {
            commentFieldName = obj.commentFieldName();
        } catch (Exception e) {
            commentFieldName = "value";
        }
        return new CommentMeta(className, commentFieldName);
    }

    private static class CommentMeta {

        private String className;

        private String packageName;

        private String simpleClassName;

        private String commentFieldName;

        public CommentMeta(String className, String commentFieldName) {
            this.className = className;
            this.packageName = className.substring(0, className.lastIndexOf("."));
            this.simpleClassName = className.substring(className.lastIndexOf(".") + 1);
            this.commentFieldName = commentFieldName;
        }

        public String getClassName() {
            return className;
        }

        public String getPackageName() {
            return packageName;
        }

        public String getSimpleClassName() {
            return simpleClassName;
        }

        public String getCommentFieldName() {
            return commentFieldName;
        }
    }
}
