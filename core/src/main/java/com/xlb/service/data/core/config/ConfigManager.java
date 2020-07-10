package com.xlb.service.data.core.config;

/**
 * 配置管理器
 */
public interface ConfigManager {

    /**
     * 获取配置信息
     *
     * @param name 配置名
     */
    SingleDataConfig getConfig(String name);

}
