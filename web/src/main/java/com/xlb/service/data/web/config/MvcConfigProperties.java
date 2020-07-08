package com.xlb.service.data.web.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = MvcConfigProperties.PREFIX)
public class MvcConfigProperties {

    static final String PREFIX = "project.mvc";

    /**
     * default error code.
     */
    private String defaultErrorCode;

    /**
     * controller error point expression
     */
    private String errorExpression;

}
