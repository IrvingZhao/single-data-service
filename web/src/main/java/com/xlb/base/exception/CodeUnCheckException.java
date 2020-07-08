package com.xlb.base.exception;

import lombok.Getter;
import lombok.Setter;

import java.text.MessageFormat;

/**
 * 带有错误编码的非检查异常
 */
@Getter
@Setter
public class CodeUnCheckException extends RuntimeException implements CodeException {

    private String code;

    public CodeUnCheckException(ErrorCode errorCode, Object... args) {
        super(MessageFormat.format(errorCode.getMsg(), args));
        this.code = errorCode.getCode();
    }

    public CodeUnCheckException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(MessageFormat.format(errorCode.getMsg(), args), cause);
        this.code = errorCode.getCode();
    }

    public CodeUnCheckException(ErrorCode errorCode, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Object... args) {
        super(MessageFormat.format(errorCode.getMsg(), args), cause, enableSuppression, writableStackTrace);
        this.code = errorCode.getCode();
    }

    public CodeUnCheckException(String code) {
        this.code = code;
    }

    public CodeUnCheckException(String code, String message, Object... args) {
        super(MessageFormat.format(message, args));
        this.code = code;
    }

    public CodeUnCheckException(String code, String message, Throwable cause, Object... args) {
        super(MessageFormat.format(message, args), cause);
        this.code = code;
    }

    public CodeUnCheckException(String code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public CodeUnCheckException(String code, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Object... args) {
        super(MessageFormat.format(message, args), cause, enableSuppression, writableStackTrace);
        this.code = code;
    }
}
