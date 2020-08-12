package com.xlb.service.data.core.config;

import com.xlb.service.data.core.data.SingleDataFactory;
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
    private SingleDataFactory.Type type; // 配置类型
    private Map<String, String> config; // 参数
}
