package com.xlb.service.data.web.config;

import com.xlb.service.data.core.config.ConfigManager;
import com.xlb.service.data.core.data.SingleDataFactory;
import com.xlb.service.data.core.manager.SingleDataManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class ManagerConfig {
    @Bean
    public SingleDataManager singleDataManager(ConfigManager configManager, StringRedisTemplate stringRedisTemplate) {
        SingleDataFactory factory = new SingleDataFactory();
        factory.setConfigManager(configManager);

        SingleDataManager dataManager = new SingleDataManager();
        dataManager.setDataFactory(factory);
        dataManager.setStringRedisTemplate(stringRedisTemplate);
        return dataManager;
    }
}
