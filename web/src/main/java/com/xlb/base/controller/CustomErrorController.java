package com.xlb.base.controller;

import com.xlb.base.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class CustomErrorController extends BasicErrorController {

    private final static Map<Class<? extends Throwable>, ErrorCode> ERROR_CODE_MAP = new HashMap<>(20);

    public static void addExceptionCode(Class<? extends Throwable> type, ErrorCode code) {
        ERROR_CODE_MAP.put(type, code);
    }

    public CustomErrorController(ErrorAttributes errorAttributes, ErrorProperties errorProperties, List<ErrorViewResolver> errorViewResolvers) {
        super(errorAttributes, errorProperties, errorViewResolvers);
        this.errorAttributes = errorAttributes;
    }

    private ErrorAttributes errorAttributes;

    private String systemErrorCode = "001111";
    private String systemErrorMsg = "测试错误码";

    @RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ErrorCode> errorCode(HttpServletRequest request) {
        Throwable exception = errorAttributes.getError(new ServletWebRequest(request));

        ErrorCode errorCode = ERROR_CODE_MAP.entrySet().parallelStream().filter((item) -> item.getKey().isInstance(exception))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseGet(() -> new DefaultErrorCode(systemErrorCode, systemErrorMsg));

        return new ResponseEntity<>(errorCode, HttpStatus.OK);
    }

    @AllArgsConstructor
    @Getter
    private static class DefaultErrorCode implements ErrorCode {
        private final String code;
        private final String msg;
    }

}
