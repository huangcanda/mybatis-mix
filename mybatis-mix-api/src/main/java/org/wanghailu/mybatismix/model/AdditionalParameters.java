package org.wanghailu.mybatismix.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 条件参数中，额外的参数
 *
 * @author cdhuang
 * @date 2022/6/30
 */
public class AdditionalParameters {
    
    private String namePrefix = "";
    
    private Map<String, Object> params = new HashMap<>();
    
    private int index = 0;
    
    public AdditionalParameters() {
    }
    
    public AdditionalParameters(String namePrefix) {
        this.namePrefix = namePrefix;
    }
    
    public Map<String, Object> getParams() {
        return params;
    }
    
    public String setParam(Object param) {
        String parameterKey = "__val__" + index;
        params.put(parameterKey, param);
        index++;
        return "#{" + namePrefix + "params." + parameterKey + "}";
    }
    
    public String getNamePrefix() {
        return namePrefix;
    }
    
    public void setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
    }
    
    public void clear() {
        params.clear();
        index = 0;
    }
}
