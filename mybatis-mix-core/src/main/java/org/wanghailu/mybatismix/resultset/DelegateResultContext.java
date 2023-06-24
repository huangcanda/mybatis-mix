package org.wanghailu.mybatismix.resultset;

import org.apache.ibatis.session.ResultContext;

/**
 * @author cdhuang
 * @date 2022/12/1
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
