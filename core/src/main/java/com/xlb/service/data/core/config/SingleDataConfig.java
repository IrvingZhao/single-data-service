package com.xlb.service.data.core.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class SingleDataConfig {
    private String name; // 配置名称
    private String type; // 配置类型
    private Map<String, String> config; // 参数
    private String data; // 原有数据
}
