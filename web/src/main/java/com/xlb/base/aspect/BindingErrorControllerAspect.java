package com.xlb.base.aspect;

import com.xlb.base.exception.CodeUnCheckException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.validation.BindingResult;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * <p>使用Spring数据绑定时，自动拦截数据绑定错误信息，并抛出异常</p>
 * <p>需自行创建bean类，并配置切片信息</p>
 * <p>{@link BindingErrorControllerAspect#errorCode}绑定失败时，所对应的默认编码</p>
 */
@Getter
@Setter
public class BindingErrorControllerAspect implements MethodBeforeAdvice {

    private String errorCode = "100000";

    @Override
    public void before(Method method, Object[] objects, Object o) throws Throwable {
        for (Object item : objects) {
            if (item instanceof BindingResult) {
                BindingResult error = (BindingResult) item;
                if (error.hasFieldErrors()) {
                    throw new CodeUnCheckException(errorCode, Objects.requireNonNull(error.getFieldError()).getDefaultMessage());
                }
            }
        }
    }
}