package org.wanghailu.mybatismix.keygenerator;

import java.util.UUID;

/**
 * 基于uuid进行主键生成
 * @author cdhuang
 * @date 2023/4/18
 */
public class UuidKeyGenerator extends BaseKeyGenerator<String> {
    
    @Override
    public String generateKey(Class entityType, String fieldName, Class keyType) {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
