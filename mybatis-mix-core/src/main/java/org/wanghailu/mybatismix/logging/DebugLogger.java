package org.wanghailu.mybatismix.logging;

import org.apache.ibatis.logging.Log;

/**
 * @author cdhuang
 * @date 2022/2/24
 */
public class DebugLogger extends DelegateLogger{


    public DebugLogger(Log delegate) {
        super(delegate);
    }

    @Override
    public boolean isTraceEnabled() {
        return super.isDebugEnabled();
    }

    @Override
    public void trace(String s) {
        super.trace(s);
    }

    @Override
    public void warn(String s) {
        super.warn(s);
    }
}
