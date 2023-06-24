package org.wanghailu.mybatismix.exception;

/**
 * 统一异常
 * @author cdhuang
 * @date 2022/12/27
 */
public class MybatisMixException extends RuntimeException {
    
    public MybatisMixException(String message) {
        super(message);
    }
    
    public MybatisMixException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public MybatisMixException(Throwable cause) {
        super(cause);
    }
}
