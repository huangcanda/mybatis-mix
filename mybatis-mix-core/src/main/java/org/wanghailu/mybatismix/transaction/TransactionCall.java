package org.wanghailu.mybatismix.transaction;

/**
 * 事务动作
 * @author cdhuang
 * @date 2023/3/24
 */
public interface TransactionCall<V> {
    V execute();
}
