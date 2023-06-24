package org.wanghailu.mybatismix.resultset;

import org.wanghailu.mybatismix.model.ExactUpdateEnable;
import org.wanghailu.mybatismix.util.MybatisContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cdhuang
 * @date 2022/12/1
 */
public class HandleResultValue {
    
    public static void handle(Object obj) {
        String databaseId = MybatisContext.getDatabaseId();
        if (obj instanceof ExactUpdateEnable) {
            ExactUpdateEnable exactUpdateRecord = (ExactUpdateEnable) obj;
            exactUpdateRecord.wipeUpdateFields();
            //TODO 特殊处理，需进行抽象
        } else if (obj instanceof Map && "oracle".equals(databaseId)) {
            Map<String, Object> resultMap = (Map) obj;
            Map<String, Object> additionResultMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                String key = entry.getKey();
                if (isAllUpperCase(key)) {
                    additionResultMap.put(key.toLowerCase(), entry.getValue());
                }
            }
            resultMap.putAll(additionResultMap);
        }
    }
    
    public static boolean isAllUpperCase(String word) {
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (Character.isLowerCase(c)) {
                return false;
            }
        }
        return true;
    }
    
}