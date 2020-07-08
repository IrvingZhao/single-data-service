package com.xlb.base.exception;

import lombok.Getter;
import lombok.Setter;

import java.text.MessageFormat;

/**
 * 带错误编码的检查异常
 */
@Setter
@Getter
public class CodeCheckException extends Exception implements CodeException {

    private String code;

    public CodeCheckException(ErrorCode errorCode, Object... args) {
        super(MessageFormat.format(errorCode.getMsg(), args));
        this.code = errorCode.getCode();
    }

    public CodeCheckException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(MessageFormat.format(errorCode.getMsg(), args), cause);
        this.code = errorCode.getCode();
    }

    public CodeCheckException(ErrorCode errorCode, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Object... args) {
        super(MessageFormat.format(errorCode.getMsg(), args), cause, enableSuppression, writableStackTrace);
        this.code = errorCode.getCode();
    }

    public CodeCheckException(String code) {
        this.code = code;
    }

    public CodeCheckException(String code, String message, Object... args) {
        super(MessageFormat.format(message, args));
        this.code = code;
    }

    public CodeCheckException(String code, String message, Throwable cause, Object... args) {
        super(MessageFormat.format(message, args), cause);
        this.code = code;
    }

    public CodeCheckException(String code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public CodeCheckException(String code, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Object... args) {
        super(MessageFormat.format(message, args), cause, enableSuppression, writableStackTrace);
        this.code = code;
    }
}
