package org.wanghailu.mybatismix.apt.processor;

import org.wanghailu.mybatismix.apt.handler.GenerateExampleClassHandler;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.persistence.Transient;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author cdhuang
 * @date 2021/12/17
 */
@SupportedAnnotationTypes("org.wanghailu.mybatismix.annotation.EnableGenerateExampleClass")
public class GenerateExampleClassAnnotationProcessor extends BaseAnnotationProcessor {

    boolean generated = false;
    
    boolean isBscMavenPlugin =false;
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }
        if(generated){
            return false;
        }
        generated = true;
        isBscMavenPlugin = checkBscMavenPlugin();
        for (TypeElement annotation : annotations) {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
            for (Element classElement : elements) {
                List<String> fieldList = new ArrayList<>();
                visitEntityFieldList(classElement, fieldList);
                createExampleClass(classElement, fieldList);
            }
        }
        return false;
    }

    public void createExampleClass(Element classElement, List<String> fieldList) {
        String className = ((TypeElement) classElement).getQualifiedName().toString();
        String simpleClassName = classElement.getSimpleName().toString();

        String realClassName = (className.indexOf("$") == -1) ? className : className.substring(0, className.indexOf("$"));
        String packageName = realClassName.lastIndexOf(".") == -1 ? "example" : realClassName.substring(0, realClassName.lastIndexOf(".")) + ".example";
        GenerateExampleClassHandler handler = new GenerateExampleClassHandler(packageName, className, simpleClassName, fieldList);

        String criteria = packageName + "." + simpleClassName + "Criteria";
        generateJavaFile(criteria, handler.generateCriteriaCode(false));
        String deleteExample = packageName + "." + simpleClassName + "DeleteExample";
        generateJavaFile(deleteExample, handler.generateDeleteExampleCode());
        String updateExample = packageName + "." + simpleClassName + "UpdateExample";
        generateJavaFile(updateExample, handler.generateUpdateExampleCode());
        String queryExample = packageName + "." + simpleClassName + "QueryExample";
        generateJavaFile(queryExample, handler.generateQueryExampleTest());
    }

    protected void generateJavaFile(String className, String code) {
        if(isBscMavenPlugin){
            return;
        }
        try {
            JavaFileObject sourceFile = filer.createSourceFile(className);
            try(Writer writer = sourceFile.openWriter()){
                writer.write(code);
                writer.flush();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
    }

    protected void visitEntityFieldList(Element classElement, List<String> fieldList) {
        for (Element fieldElement : classElement.getEnclosedElements()) {
            if (ElementKind.FIELD == fieldElement.getKind()) {
                Set<Modifier> modifiers = fieldElement.getModifiers();
                if (modifiers.contains(Modifier.STATIC)) {
                    continue;
                }
                Transient aTransient = fieldElement.getAnnotation(Transient.class);
                if (aTransient != null) {
                    continue;
                }
                fieldList.add(fieldElement.getSimpleName().toString());
            }
        }
        TypeMirror superType = getSuperClass(classElement.asType());
        if (superType != null) {
            TypeElement typeElement = getTypeElement(superType);
            if (typeElement != null) {
                visitEntityFieldList(typeElement, fieldList);
            }

        }
    }

    protected TypeElement getTypeElement(TypeMirror typeMirror) {
        typeMirror = getRealType(typeMirror);
        if (typeMirror.getKind() == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) typeMirror;
            TypeElement typeElement = (TypeElement) declaredType.asElement();
            return typeElement;
        }
        return null;
    }

    protected TypeMirror getSuperClass(TypeMirror typeMirror) {
        TypeElement typeElement = getTypeElement(typeMirror);
        if (typeElement != null) {
            return typeElement.getSuperclass();
        }
        return null;
    }

    protected TypeMirror getRealType(TypeMirror type) {
        if (type.getKind() == TypeKind.TYPEVAR) {
            TypeVariable typeVar = (TypeVariable) type;
            if (typeVar.getUpperBound() != null) {
                return typeVar.getUpperBound();
            }
        } else if (type.getKind() == TypeKind.WILDCARD) {
            WildcardType wildcard = (WildcardType) type;
            if (wildcard.getExtendsBound() != null) {
                return wildcard.getExtendsBound();
            }
        }
        return type;
    }
    
    private boolean checkBscMavenPlugin(){
        StackTraceElement[] stackTraceElements =Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            if(stackTraceElement.getClassName().startsWith("org.bsc.maven.plugin.processor")){
                return true;
            }
        }
        return false;
    }
}
