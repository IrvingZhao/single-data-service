package com.xlb.base.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作成功后的相应消息配置
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SuccessMessage {

    /**
     * 返回编码
     */
    String code() default "000000";

    /**
     * 返回消息
     */
    String msg();

}
