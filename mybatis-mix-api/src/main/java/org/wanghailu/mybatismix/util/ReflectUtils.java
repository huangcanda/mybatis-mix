package org.wanghailu.mybatismix.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 反射工具类(反射相关的杂七杂八的往这里扔)
 */
public class ReflectUtils {
    
    private static Method DEFINE_CLASS;
    
    private static final ProtectionDomain PROTECTION_DOMAIN;
    
    private static Logger logger = LoggerFactory.getLogger(ReflectUtils.class);
    
    static {
        PROTECTION_DOMAIN = (ProtectionDomain) AccessController.doPrivileged(new PrivilegedAction() {
            @Override
            public Object run() {
                return ReflectUtils.class.getProtectionDomain();
            }
        });
        
        AccessController.doPrivileged(new PrivilegedAction() {
            @Override
            public Object run() {
                try {
                    Class loader = Class.forName("java.lang.ClassLoader"); // JVM crash w/o this
                    DEFINE_CLASS = loader.getDeclaredMethod("defineClass",
                            new Class[] {String.class, byte[].class, Integer.TYPE, Integer.TYPE,
                                    ProtectionDomain.class});
                    DEFINE_CLASS.setAccessible(true);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        });
    }
    
    public static Class defineClass(String className, byte[] b, ClassLoader loader) {
        Object[] args = new Object[] {className, b, new Integer(0), new Integer(b.length), PROTECTION_DOMAIN};
        try {
            return (Class) DEFINE_CLASS.invoke(loader, args);
        } catch (Exception e) {
            ExceptionUtils.throwException(e);
        }
        return null;
    }
    
    public static void modifyFinalField(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            // 如果field为private,则需要使用该方法使其可被访问
            Field modifersField = Field.class.getDeclaredField("modifiers");
            modifersField.setAccessible(true);
            // 把指定的field中的final修饰符去掉
            modifersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            ExceptionUtils.throwException(e);
            return null;
        }
    }
    
    /**
     * 利用反射设置指定对象的指定属性为指定的值
     *
     * @param obj        目标对象
     * @param fieldName  目标属性
     * @param fieldValue 目标值
     */
    public static void setFieldValue(Object obj, String fieldName, Object fieldValue) {
        if (obj instanceof Map) {
            ((Map) obj).put(fieldName, fieldValue);
            return;
        }
        Field field = ReflectUtils.getField(obj, fieldName);
        if (field != null) {
            try {
                field.setAccessible(true);
                field.set(obj, fieldValue);
            } catch (IllegalArgumentException e) {
                logger.error(e.getMessage(), e);
            } catch (IllegalAccessException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
    
    /**
     * 利用反射获取指定对象的指定属性
     *
     * @param obj       目标对象
     * @param fieldName 目标属性
     * @return 目标属性的值
     */
    public static Object getFieldValue(Object obj, String fieldName) {
        if (obj instanceof Map) {
            return ((Map) obj).get(fieldName);
        }
        Object result = null;
        Field field = ReflectUtils.getField(obj, fieldName);
        if (field != null) {
            field.setAccessible(true);
            try {
                result = field.get(obj instanceof Class ? null : obj);
            } catch (IllegalArgumentException e) {
                logger.error(e.getMessage(), e);
            } catch (IllegalAccessException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return result;
    }
    
    /**
     * 利用反射获取指定对象里面的指定属性
     *
     * @param obj       目标对象或对象的class
     * @param fieldName 目标属性
     * @return 目标字段
     */
    public static Field getField(Object obj, String fieldName) {
        Class c;
        if (obj instanceof Class) {
            c = (Class) obj;
        } else {
            c = obj.getClass();
        }
        Field field = null;
        for (Class<?> clazz = c; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                field = clazz.getDeclaredField(fieldName);
                break;
            } catch (NoSuchFieldException e) {
                //这里不用做处理，子类没有该字段可能对应的父类有，都没有就返回null。
            }
        }
        return field;
    }
    
    /**
     * 获得类的相关field,method信息
     *
     * @param clazz
     * @return
     */
    public static PropertyDescriptor[] getBeanPropertyDescriptors(Class clazz) {
        BeanInfo info = null;
        try {
            info = Introspector.getBeanInfo(clazz, Object.class);
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
        List<Field> fields = getAllField(clazz);
        Map<String, Integer> fieldOrderMap = new HashMap<>(fields.size());
        int order = 0;
        for (Field field : fields) {
            String fieldName = field.getName();
            if (fieldOrderMap.get(fieldName) == null) {
                fieldOrderMap.put(fieldName, order);
                order++;
            }
        }
        PropertyDescriptor[] array = info.getPropertyDescriptors();
        Arrays.sort(array, (o1, o2) -> {
            Integer order1 = 1000;
            if (o1.getName() != null && fieldOrderMap.get(o1.getName()) != null) {
                order1 = fieldOrderMap.get(o1.getName());
            }
            Integer order2 = 1000;
            if (o2.getName() != null && fieldOrderMap.get(o2.getName()) != null) {
                order2 = fieldOrderMap.get(o2.getName());
            }
            return order1.compareTo(order2);
        });
        return array;
    }
    
    /**
     * 获得clazz的所有field
     *
     * @param clazz
     * @return
     */
    public static List<Field> getAllField(Class clazz) {
        List<Field> result = new ArrayList<>();
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Field declaredField : clazz.getDeclaredFields()) {
                result.add(declaredField);
            }
        }
        return result;
    }
    
    /**
     * 获得类的属性信息（具有get,set方法的属性）
     *
     * @param clazz
     * @return
     */
    public static List<PropertyDescriptor> getBeanPropertyDescriptorsHaveGetSetMethod(Class clazz) {
        List<PropertyDescriptor> descriptorList = new ArrayList<>();
        PropertyDescriptor[] propertyDescriptors = ReflectUtils.getBeanPropertyDescriptors(clazz);
        for (PropertyDescriptor descriptor : propertyDescriptors) {
            if (descriptor.getName() != null && descriptor.getWriteMethod() != null
                    && descriptor.getReadMethod() == null) {
                String readMethodName = null;
                if (descriptor.getPropertyType().equals(Boolean.class)) {
                    readMethodName = "is" + TruckUtils.capitalize(descriptor.getName());
                } else if (descriptor.getPropertyType().equals(boolean.class)) {
                    readMethodName = "get" + TruckUtils.capitalize(descriptor.getName());
                }
                if (readMethodName != null) {
                    Method readMethod = null;
                    try {
                        readMethod = ReflectUtils.findDeclaredMethod(clazz, readMethodName, new Class[] {});
                    } catch (NoSuchMethodException e) {
                    }
                    if (readMethod != null) {
                        try {
                            descriptor.setReadMethod(readMethod);
                        } catch (IntrospectionException e) {
                            ExceptionUtils.throwException(e);
                        }
                    }
                }
            }
            if (descriptor.getReadMethod() != null && descriptor.getWriteMethod() != null
                    && descriptor.getName() != null) {
                descriptorList.add(descriptor);
            }
        }
        return descriptorList;
    }
    
    public static Method findDeclaredMethod(final Class type, final String methodName, final Class[] parameterTypes)
            throws NoSuchMethodException {
        
        Class cl = type;
        while (cl != null) {
            try {
                return cl.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
                cl = cl.getSuperclass();
            }
        }
        throw new NoSuchMethodException(methodName);
    }
    
    private static Map<Class, PropertyDescriptor[]> propertyDescriptorsMap = new HashMap<>();
    
    /**
     * getBeanPropertyDescriptorsHaveGetSetMethod的缓存版本，
     *
     * @param clazz
     * @return
     */
    public static PropertyDescriptor[] getBeanPropertyDescriptorsHaveGetSetMethodAndCache(Class clazz) {
        PropertyDescriptor[] descriptors = propertyDescriptorsMap.get(clazz);
        if (descriptors == null) {
            synchronized (propertyDescriptorsMap) {
                descriptors = propertyDescriptorsMap.get(clazz);
                if (descriptors == null) {
                    List<PropertyDescriptor> list = getBeanPropertyDescriptorsHaveGetSetMethod(clazz);
                    descriptors = list.toArray(new PropertyDescriptor[list.size()]);
                    propertyDescriptorsMap.put(clazz, descriptors);
                }
            }
        }
        return descriptors;
    }
    
    public static Object invokeMethod(Method method, Object target, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (Exception ex) {
            ExceptionUtils.throwException(ex);
            return null;
        }
    }
    
    public static Method getMethod(Object obj, String methodName, Class<?>... paramTypes) {
        Class clazz;
        if (obj instanceof Class) {
            clazz = (Class) obj;
        } else {
            clazz = obj.getClass();
        }
        Class<?> searchType = clazz;
        while (searchType != null) {
            Method[] methods = (searchType.isInterface() ? searchType.getMethods() : getDeclaredMethods(searchType));
            for (Method method : methods) {
                if (methodName.equals(method.getName()) && (paramTypes == null || Arrays
                        .equals(paramTypes, method.getParameterTypes()))) {
                    return method;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }
    
    private static Method[] getDeclaredMethods(Class<?> clazz) {
        try {
            Method[] declaredMethods = clazz.getDeclaredMethods();
            List<Method> defaultMethods = findConcreteMethodsOnInterfaces(clazz);
            if (defaultMethods != null) {
                Method[] result = new Method[declaredMethods.length + defaultMethods.size()];
                System.arraycopy(declaredMethods, 0, result, 0, declaredMethods.length);
                int index = declaredMethods.length;
                for (Method defaultMethod : defaultMethods) {
                    result[index] = defaultMethod;
                    index++;
                }
                return result;
            }
            return declaredMethods;
        } catch (Throwable ex) {
            throw new IllegalStateException(
                    "Failed to introspect Class [" + clazz.getName() + "] from ClassLoader [" + clazz.getClassLoader()
                            + "]", ex);
        }
    }
    
    private static List<Method> findConcreteMethodsOnInterfaces(Class<?> clazz) {
        List<Method> result = null;
        for (Class<?> ifc : clazz.getInterfaces()) {
            for (Method ifcMethod : ifc.getMethods()) {
                if (!Modifier.isAbstract(ifcMethod.getModifiers())) {
                    if (result == null) {
                        result = new ArrayList<>();
                    }
                    result.add(ifcMethod);
                }
            }
        }
        return result;
    }
    
    public static <T extends Annotation> List<T> findAnnotation(AnnotatedElement annotatedElement,
            Class<T> annotationClass) {
        List<T> result = new ArrayList<>();
        if (annotatedElement == null) {
            return result;
        }
        Annotation[] annotations = annotatedElement.getAnnotations();
        if (annotations == null || annotations.length == 0) {
            return result;
        }
        T[] annotationsByType = annotatedElement.getAnnotationsByType(annotationClass);
        if (annotationsByType.length > 0) {
            result.addAll(Arrays.asList(annotationsByType));
        }
        for (Annotation annotation : annotations) {
            if (annotation.getClass().getName().startsWith("java.lang.annotation")) {
                continue;
            }
            Class baseClass = annotation.getClass().getInterfaces()[0];
            if (baseClass.getName().startsWith("java.lang.annotation")) {
                continue;
            }
            List<T> recursionAnnotation = findAnnotation(baseClass, annotationClass);
            if (TruckUtils.isNotEmpty(recursionAnnotation)) {
                result.addAll(recursionAnnotation);
            }
        }
        return result;
    }
}
