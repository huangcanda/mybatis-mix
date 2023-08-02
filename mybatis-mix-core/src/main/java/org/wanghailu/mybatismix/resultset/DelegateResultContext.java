package org.wanghailu.mybatismix.resultset;

import org.apache.ibatis.session.ResultContext;

/**
 * 装饰ResultContext
 */
public class DelegateResultContext implements ResultContext {
    
    private ResultContext delegate;
    
    public DelegateResultContext(ResultContext delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public Object getResultObject() {
        Object obj = delegate.getResultObject();
        HandleResultValue.handle(obj);
        return obj;
    }
    
    
    
    @Override
    public int getResultCount() {
        return delegate.getResultCount();
    }
    
    @Override
    public boolean isStopped() {
        return delegate.isStopped();
    }
    
    @Override
    public void stop() {
        delegate.stop();
    }
}
