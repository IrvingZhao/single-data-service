package com.xlb.base.handler;

import com.xlb.base.annotation.SuccessMessage;
import com.xlb.base.controller.ResponseBodyHandleController;
import com.xlb.base.controller.SingleResponse;
import com.xlb.base.exception.CodeException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>基于切片拦截请求，并自动封装数据，捕获异常</p>
 * <p>被拦截的Controller需实现{@link ResponseBodyHandleController}类</p>
 */
@ControllerAdvice(assignableTypes = ResponseBodyHandleController.class)
@RestControllerAdvice(assignableTypes = ResponseBodyHandleController.class)
@Slf4j
public class ResponseBodyMessageHandle implements ResponseBodyAdvice {

    @Getter
    @Setter
    private String successMsg;

    @Getter
    @Setter
    private String successCode = "000000";

    @Getter
    @Setter
    private String exceptionCode = "100000";

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        //检查方法是否具有RestController 或者 方法具有 ResponseBody 注解，并且消息格式化为Jackson格式化才会启用
        return (returnType.getMember().getDeclaringClass().getAnnotation(RestController.class) != null ||
                returnType.getMethodAnnotation(ResponseBody.class) != null)
                && (MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType) || StringHttpMessageConverter.class.isAssignableFrom(converterType));
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        String code;
        String msg;
        SuccessMessage successMessage = returnType.getAnnotatedElement().getAnnotation(SuccessMessage.class);
        if (successMessage == null) {
            successMessage = returnType.getContainingClass().getAnnotation(SuccessMessage.class);
        }
        if (successMessage != null) {
            code = successMessage.code();
            msg = successMessage.msg();
        } else {
            code = this.successCode;
            msg = this.successMsg;
        }
        Map<String, Object> resultValue = new HashMap<>();
        resultValue.put("success", true);
        resultValue.put("code", code);
        resultValue.put("msg", msg);
        if (body instanceof SingleResponse) {
            resultValue.put("data", ((SingleResponse<?>) body).getData());
        } else {
            resultValue.put("data", body);
        }
        return resultValue;
    }

    @ExceptionHandler
    @ResponseBody
    public Map<String, Object> exceptionHandle(Throwable throwable) {
        log.error("controller error", throwable);
        Map<String, Object> resultValue = new HashMap<>();
        resultValue.put("success", false);
        if (throwable instanceof CodeException) {
            CodeException codeException = (CodeException) throwable;
            resultValue.put("code", codeException.getCode());
        } else {
            resultValue.put("code", exceptionCode);
        }
        resultValue.put("msg", throwable.getMessage());
        return resultValue;
    }
}
