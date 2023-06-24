package org.wanghailu.mybatismix.resultset;

import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;

/**
 * @author cdhuang
 * @date 2022/12/1
 */
public class DelegateResultHandler implements ResultHandler {
    
    private ResultHandler delegate;
    
    public DelegateResultHandler(ResultHandler delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public void handleResult(ResultContext resultContext) {
        delegate.handleResult(new DelegateResultContext(resultContext));
    }
}
