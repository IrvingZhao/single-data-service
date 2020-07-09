package com.xlb.service.data.core.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SingleDataConfig {
    private String name; // 配置名称
    private String type; // 配置类型
    private Map<String, String> config; // 参数
}
