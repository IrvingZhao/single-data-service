package com.xlb.service.data.core.exception;

/**
 * 刷新时参数异常
 */
public class RefreshParamException extends RuntimeException {
    public RefreshParamException() {
        super();
    }

    public RefreshParamException(String message) {
        super(message);
    }

    public RefreshParamException(String message, Throwable cause) {
        super(message, cause);
    }

    public RefreshParamException(Throwable cause) {
        super(cause);
    }

    protected RefreshParamException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
