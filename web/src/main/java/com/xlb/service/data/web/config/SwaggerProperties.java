package com.xlb.service.data.web.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = SwaggerProperties.SWAGGER_PREFIX)
@Getter
@Setter
public class SwaggerProperties {
    static final String SWAGGER_PREFIX = "project.swagger";
    private String title;
    private String description;
}