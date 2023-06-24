package org.wanghailu.mybatismix.logging;

import org.apache.ibatis.logging.Log;

/**
 * @author cdhuang
 * @date 2022/2/24
 */
public class DelegateLogger implements Log {

    protected Log delegate;

    public DelegateLogger(Log delegate) {
        this.delegate = delegate;
    }

    protected Log getDelegate(){
        return delegate;
    }

    @Override
    public boolean isDebugEnabled() {
        return getDelegate().isDebugEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return getDelegate().isTraceEnabled();
    }

    @Override
    public void error(String s, Throwable e) {
        getDelegate().error(s, e);
    }

    @Override
    public void error(String s) {
        getDelegate().error(s);
    }

    @Override
    public void debug(String s) {
        getDelegate().debug(s);
    }

    @Override
    public void trace(String s) {
        getDelegate().trace(s);
    }

    @Override
    public void warn(String s) {
        getDelegate().warn(s);
    }
}
