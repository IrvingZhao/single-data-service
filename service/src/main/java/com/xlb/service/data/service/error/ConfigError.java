package com.xlb.service.data.service.error;

import com.xlb.base.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public enum ConfigError implements ErrorCode {
    CONFIG_KEY_EXITS("020001", "[{0}]已存在"),
    CONFIG_KEY_NOT_FOUND("020002", "[{0}]未找到"),
    ;
    private final String code;
    private final String msg;
}
