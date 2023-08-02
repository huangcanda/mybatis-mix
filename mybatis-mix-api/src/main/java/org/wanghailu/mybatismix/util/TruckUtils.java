package org.wanghailu.mybatismix.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wanghailu.mybatismix.annotation.OrderedItem;
import org.wanghailu.mybatismix.exception.MybatisMixException;
import org.wanghailu.mybatismix.support.OrderItem;
import org.wanghailu.mybatismix.support.SerializableFunction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.SerializedLambda;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Locale.ENGLISH;

/**
 * 杂七杂八的工具类放这里，避免引入其他工具类包，减少依赖
 *
 * @author cdhuang
 * @date 2022/12/15
 */
public class TruckUtils {
    
    private static Logger logger = LoggerFactory.getLogger(TruckUtils.class);
    
    public static final char UNDERLINE = '_';
    
    public static String lineSeparator = System.getProperty("line.separator");
    
    public static void assertTrue(boolean state, String message, Object... args) {
        if (!state) {
            throw new IllegalArgumentException(String.format(message, args));
        }
    }
    
    public static void assertNotNull(Object object, String message, Object... args) {
        assertTrue(object != null, message, args);
    }
    
    public static boolean isEmptyPrimaryKey(Object primaryKeyValue) {
        return primaryKeyValue == null || (primaryKeyValue instanceof String && PrivateStringUtils
                .isEmpty((String) primaryKeyValue));
    }
    
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        } else if (obj instanceof String) {
            return ((String) obj).isEmpty();
        } else if (obj instanceof Collection) {
            return ((Collection) obj).isEmpty();
        } else if (obj instanceof Map) {
            return ((Map) obj).isEmpty();
        } else if (obj.getClass().isArray()) {
            return Array.getLength(obj) == 0;
        } else {
            return false;
        }
    }
    
    public static boolean isNotEmpty(Object coll) {
        return !isEmpty(coll);
    }
    
    
    public static boolean isEmpty(Collection coll) {
        return coll == null || coll.isEmpty();
    }
    
    public static boolean isNotEmpty(Collection coll) {
        return !isEmpty(coll);
    }
    
    public static String capitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        return name.substring(0, 1).toUpperCase(ENGLISH) + name.substring(1);
    }
    
    public static String uncapitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        return name.substring(0, 1).toLowerCase(ENGLISH) + name.substring(1);
    }
    
    /**
     * 获得get或set方法对应的属性名
     *
     * @param name
     * @return
     */
    public static String methodToProperty(String name) {
        if (name.startsWith("is")) {
            name = name.substring(2);
        } else if (name.startsWith("get") || name.startsWith("set")) {
            name = name.substring(3);
        } else {
            throw new RuntimeException(
                    "Error parsing method name '" + name + "'.  Didn't start with 'is', 'get' or 'set'.");
        }
        if (name.length() == 1) {
            name = name.toLowerCase(Locale.ENGLISH);
        } else if (name.length() > 1 && !Character.isUpperCase(name.charAt(1))) {
            name = name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1);
        }
        return name;
    }
    
    /**
     * 驼峰转下划线
     *
     * @param param
     * @return
     */
    public static String camelToUnderline(String param) {
        if (param == null || param.length() == 0) {
            return "";
        }
        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append(UNDERLINE);
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }
    
    /**
     * 下划线转驼峰
     *
     * @param param
     * @return
     */
    public static String underlineToCamel(String param) {
        if (param == null || param.length() == 0) {
            return "";
        }
        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        // "_" 后转大写标志,默认字符前面没有"_"
        Boolean flag = false;
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (c == UNDERLINE) {
                flag = true;
                continue;   //标志设置为true,跳过
            } else {
                if (flag == true) {
                    //表示当前字符前面是"_" ,当前字符转大写
                    sb.append(Character.toUpperCase(param.charAt(i)));
                    flag = false;
                } else {
                    sb.append(Character.toLowerCase(param.charAt(i)));
                }
            }
        }
        return sb.toString();
    }
    
    public static SerializedLambda getLambdaInfo(SerializableFunction func) {
        try {
            Method method = func.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            return (SerializedLambda) method.invoke(func);
        } catch (Exception e) {
            ExceptionUtils.throwException(e);
            return null;
        }
    }
    
    
    public static String getLambdaFuncFieldName(SerializableFunction func) {
        SerializedLambda serializedLambda = getLambdaInfo(func);
        String fieldName = methodToProperty(serializedLambda.getImplMethodName());
        return fieldName;
    }
    
    
    /**
     * 分割list 比如一个长度为 1500的list，按照1000的长度进行分割，则分割成两个长度为750的list
     *
     * @param source
     * @param splitSize
     * @param <T>
     * @return
     */
    public static <T> List<T>[] averageSplit(List<T> source, int splitSize) {
        if (splitSize < 1) {
            throw new IllegalArgumentException("error splitSize: " + splitSize);
        }
        
        if (isEmpty(source)) {
            return null;
        }
        int n = source.size() / splitSize + 1;
        List[] result = new List[n];
        //(先计算出余数)
        int remaider = source.size() % n;
        //然后是商
        int number = source.size() / n;
        //偏移量
        int offset = 0;
        for (int i = 0; i < n; i++) {
            List<T> value;
            if (remaider > 0) {
                value = source.subList(i * number + offset, (i + 1) * number + offset + 1);
                remaider--;
                offset++;
            } else {
                value = source.subList(i * number + offset, (i + 1) * number + offset);
            }
            result[i] = value;
        }
        return result;
    }
    
    public static int streamCopy(InputStream in, OutputStream out) throws IOException {
        TruckUtils.assertNotNull(in, "No InputStream specified");
        TruckUtils.assertNotNull(out, "No OutputStream specified");
        
        int byteCount = 0;
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
            byteCount += bytesRead;
        }
        out.flush();
        return byteCount;
    }
    
    public static int streamCopyAndClose(InputStream in, OutputStream out) {
        try {
            return streamCopy(in, out);
        } catch (Exception e) {
            ExceptionUtils.throwException(e);
            return 0;
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                // ignore
            }
            try {
                out.close();
            } catch (IOException ex) {
                // ignore
            }
        }
    }
    
    public static byte[] copyInputStreamToByteArray(InputStream in) {
        if (in == null) {
            return new byte[0];
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
        streamCopyAndClose(in, out);
        return out.toByteArray();
    }
    
    public static Object convertSimpleType(Object value, Class<?> type) {
        if (value == null || type == null || type.isAssignableFrom(value.getClass())) {
            return value;
        }
        if (String.class.equals(value.getClass())) {
            String strValue = (String) value;
            convertSimpleType(strValue, type);
        } else if (value instanceof Number) {
            Number numberValue = (Number) value;
            convertSimpleType(numberValue, type);
        }
        return value;
    }
    
    public static Object convertSimpleType(Number numberValue, Class<?> type) {
        if (numberValue == null || type == null || type.isAssignableFrom(numberValue.getClass())) {
            return numberValue;
        }
        if (Long.class.equals(type)) {
            return numberValue.longValue();
        } else if (Integer.class.equals(type)) {
            return numberValue.intValue();
        } else if (Short.class.equals(type)) {
            return numberValue.shortValue();
        } else if (Double.class.equals(type)) {
            return numberValue.doubleValue();
        } else if (Float.class.equals(type)) {
            return numberValue.floatValue();
        } else if (BigDecimal.class.equals(type)) {
            return BigDecimal.valueOf(numberValue.longValue());
        } else if (BigInteger.class.equals(type)) {
            return BigInteger.valueOf(numberValue.longValue());
        } else if (Date.class.equals(type)) {
            return new Date(numberValue.longValue()).toInstant();
        } else if (LocalDateTime.class.equals(type)) {
            Instant instant = Instant.ofEpochMilli(numberValue.longValue());
            return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        } else if (LocalDate.class.equals(type)) {
            Instant instant = Instant.ofEpochMilli(numberValue.longValue());
            return instant.atZone(ZoneId.systemDefault()).toLocalDate();
        } else if (LocalTime.class.equals(type)) {
            Instant instant = Instant.ofEpochMilli(numberValue.longValue());
            return instant.atZone(ZoneId.systemDefault()).toLocalTime();
        } else {
            throw new MybatisMixException("numberValue convert fail, not support type:" + type.getName());
        }
    }
    
    public static Object convertSimpleType(String strValue, Class<?> type) {
        if (strValue == null || type == null || type.isAssignableFrom(strValue.getClass())) {
            return strValue;
        }
        if (Long.class.equals(type)) {
            return Long.parseLong(strValue);
        } else if (Integer.class.equals(type)) {
            return Integer.parseInt(strValue);
        } else if (Short.class.equals(type)) {
            return Short.parseShort(strValue);
        } else if (Double.class.equals(type)) {
            return Double.parseDouble(strValue);
        } else if (Float.class.equals(type)) {
            return Float.parseFloat(strValue);
        } else if (BigDecimal.class.equals(type)) {
            return new BigDecimal(strValue);
        } else if (BigInteger.class.equals(type)) {
            return new BigInteger(strValue);
        } else {
            throw new MybatisMixException("numberValue convert fail, not support type:" + type.getName());
        }
    }
    
    public static long getCountSizeByResultList(List countResultList) {
        long totalRecord = 0;
        if (countResultList != null && countResultList.size() > 0 && countResultList.get(0) != null) {
            totalRecord = Long.parseLong(countResultList.get(0).toString());
        }
        return totalRecord;
    }
    
    /**
     * 获得本地IP地址
     *
     * @return
     */
    public static String getLocalAddress() {
        try {
            // Traversal Network interface to get the first non-loopback and non-private address
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            ArrayList<String> ipv4Result = new ArrayList<String>();
            ArrayList<String> ipv6Result = new ArrayList<String>();
            while (enumeration.hasMoreElements()) {
                final NetworkInterface networkInterface = enumeration.nextElement();
                final Enumeration<InetAddress> en = networkInterface.getInetAddresses();
                while (en.hasMoreElements()) {
                    final InetAddress address = en.nextElement();
                    if (!address.isLoopbackAddress()) {
                        if (address instanceof Inet6Address) {
                            ipv6Result.add(normalizeHostAddress(address));
                        } else {
                            ipv4Result.add(normalizeHostAddress(address));
                        }
                    }
                }
            }
            
            // prefer ipv4
            if (!ipv4Result.isEmpty()) {
                for (String ip : ipv4Result) {
                    if (ip.startsWith("127.0") || ip.startsWith("192.168")) {
                        continue;
                    }
                    
                    return ip;
                }
                
                return ipv4Result.get(ipv4Result.size() - 1);
            } else if (!ipv6Result.isEmpty()) {
                return ipv6Result.get(0);
            }
            //If failed to find,fall back to localhost
            final InetAddress localHost = InetAddress.getLocalHost();
            return normalizeHostAddress(localHost);
        } catch (Exception e) {
            logger.error("Failed to obtain local address", e);
        }
        
        return null;
    }
    
    private static String normalizeHostAddress(final InetAddress localHost) {
        if (localHost instanceof Inet6Address) {
            return "[" + localHost.getHostAddress() + "]";
        } else {
            return localHost.getHostAddress();
        }
    }
    
    /**
     * 获得当前进程的id
     *
     * @return
     */
    public static int getPid() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        // format: "pid@hostname"
        String name = runtime.getName();
        try {
            return Integer.parseInt(name.substring(0, name.indexOf('@')));
        } catch (Exception e) {
            return -1;
        }
    }
    
    /**
     * 泛型类型推断
     *
     * @param mapperInterface
     * @param superInterface
     * @return
     */
    public static Type getMapperGenericType(Class mapperInterface, Class superInterface) {
        Type[] genericInterfaces = mapperInterface.getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
                Class rawType = (Class) parameterizedType.getRawType();
                if (rawType.getName().equals(superInterface.getName())) {
                    if (parameterizedType.getActualTypeArguments().length > 0) {
                        return parameterizedType.getActualTypeArguments()[0];
                    }
                    return null;
                }
                Type entityType = getMapperGenericType(rawType, superInterface);
                if (entityType != null) {
                    if (entityType instanceof Class) {
                        return entityType;
                    } else if (entityType instanceof TypeVariable) {
                        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                        if (actualTypeArguments != null && actualTypeArguments.length > 0) {
                            Map<TypeVariable<Class>, Type> typeParameterMap = new LinkedHashMap<>();
                            TypeVariable<Class>[] typeParameters = rawType.getTypeParameters();
                            int index = 0;
                            for (TypeVariable<Class> typeParameter : typeParameters) {
                                if (index < actualTypeArguments.length) {
                                    typeParameterMap.put(typeParameter, actualTypeArguments[index]);
                                }
                            }
                            Type actualTypeArgument = typeParameterMap.get(entityType);
                            return actualTypeArgument;
                        }
                    }
                    return entityType;
                }
            } else if (genericInterface instanceof Class) {
                Type entityType = getMapperGenericType((Class) genericInterface, superInterface);
                if (entityType != null) {
                    return entityType;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 递归获得所有接口类
     *
     * @param mapperInterface
     * @return
     */
    public static List<Class> getAllInterfaces(Class mapperInterface) {
        List<Class> result = new ArrayList<>();
        Class[] superInterface = mapperInterface.getInterfaces();
        if (superInterface != null && superInterface.length > 0) {
            for (Class aClass : superInterface) {
                result.add(aClass);
                result.addAll(getAllInterfaces(aClass));
            }
        }
        return result;
    }
    
    /**
     * 进行排序，不破坏原本的无序 //TODO
     *
     * @param list
     */
    public static void listSort(List list) {
        Collections.sort(list, Comparator.comparing(x -> {
            OrderedItem orderedItem = x.getClass().getAnnotation(OrderedItem.class);
            if (orderedItem != null) {
                return orderedItem.value();
            } else if (x instanceof OrderItem) {
                return ((OrderItem) x).getOrder();
            }
            return 0;
        }));
    }
}
