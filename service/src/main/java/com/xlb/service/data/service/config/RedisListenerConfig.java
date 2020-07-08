package com.xlb.service.data.service.config;

import com.xlb.service.data.constant.Constant;
import com.xlb.service.data.service.listener.DataUpdateListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.support.collections.RedisProperties;

@Configuration
public class RedisListenerConfig {
    private final RedisConnectionFactory redisConnectionFactory;

    public RedisListenerConfig(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Bean
    public PatternTopic subscribeTopic() {
        return new PatternTopic(Constant.SINGLE_DATA_UPDATE_CHANNEL_PREFIX + "*");
    }

    @Bean
    public RedisMessageListenerContainer messageListenerContainer(PatternTopic subscribeTopic, DataUpdateListener dataUpdateListener) {
        RedisMessageListenerContainer result = new RedisMessageListenerContainer();
        result.setConnectionFactory(redisConnectionFactory);

        result.addMessageListener(dataUpdateListener, subscribeTopic);

        return result;
    }
}
