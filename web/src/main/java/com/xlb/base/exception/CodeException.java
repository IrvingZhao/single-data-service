package com.xlb.base.exception;

/**
 * 带编码错误异常接口，用于使用者扩展其他异常信息
 */
public interface CodeException {

    String getCode();

    String getMessage();

}
