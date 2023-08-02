package org.wanghailu.mybatismix.mapper.jpaquery;

import org.apache.ibatis.reflection.ParamNameResolver;
import org.wanghailu.mybatismix.model.AdditionalParameters;

import java.util.HashMap;
import java.util.Map;

/**
 * mapper方法参数支持
 * @author cdhuang
 * @date 2023/4/3
 */
public class MapperMethodArgsMap {
    
    public static final String ADDITIONAL_PARAMETERS_KEY ="$additionalParameters";
    
    private Map<String, Object> argsMap;
    
    protected AdditionalParameters additionalParameters;
    
    public MapperMethodArgsMap(Object args) {
        if(args instanceof Map){
            this.argsMap = (Map<String, Object>) args;
        }else{
            this.argsMap = new HashMap<>();
            argsMap.put(ParamNameResolver.GENERIC_NAME_PREFIX + "1",args);
        }
       
    }
    
    public Object set(int index, Object element) {
        return argsMap.put(ParamNameResolver.GENERIC_NAME_PREFIX + index, element);
    }
    
    public String getKeyName(int index) {
        return "#{" + ParamNameResolver.GENERIC_NAME_PREFIX + index + "}";
    }
    
    public Object get(int index) {
        return argsMap.get(ParamNameResolver.GENERIC_NAME_PREFIX + index);
    }
    
    public Object putMap(String key, Object value) {
        return argsMap.put(key, value);
    }
    
    public AdditionalParameters getAdditionalParameters() {
        if (additionalParameters == null) {
            additionalParameters = new AdditionalParameters(ADDITIONAL_PARAMETERS_KEY+".");
            argsMap.put(ADDITIONAL_PARAMETERS_KEY, additionalParameters);
        }
        return additionalParameters;
    }
}
