package org.wanghailu.mybatismix.logging;

import org.apache.ibatis.logging.Log;

/**
 * 空日志打印器，啥也不打印。即屏蔽mybatis的sql日志打印
 * @author cdhuang
 * @date 2021/9/24
 */
public class EmptyLogger implements Log {
    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void error(String s, Throwable e) {

    }

    @Override
    public void error(String s) {

    }

    @Override
    public void debug(String s) {

    }

    @Override
    public void trace(String s) {

    }

    @Override
    public void warn(String s) {

    }
}
