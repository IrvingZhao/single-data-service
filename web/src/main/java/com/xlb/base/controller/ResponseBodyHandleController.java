package com.xlb.base.controller;

/**
 * 自动校验返回错误标志controller
 */
public interface ResponseBodyHandleController {

    default <T> SingleResponse<T> wrapResponse(T data) {
        return new SingleResponse<T>(data);
    }

}
