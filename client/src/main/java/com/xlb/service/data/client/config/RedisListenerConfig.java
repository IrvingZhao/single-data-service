package com.xlb.service.data.client.config;

import com.xlb.service.data.client.listener.SingleDataListener;
import com.xlb.service.data.client.manager.SingleDataManager;
import com.xlb.service.data.constant.Constant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisListenerConfig {

    private final RedisConnectionFactory redisConnectionFactory;

    public RedisListenerConfig(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Value("${project.singleData.baseUrl}")
    private String baseUrl;

    @Bean
    public SingleDataManager dataManager() {
        return new SingleDataManager(baseUrl);
    }

    @Bean
    public SingleDataListener dataListener(SingleDataManager dataManager) {
        return new SingleDataListener(dataManager);
    }

    @Bean
    public PatternTopic subscribeTopic() {
        return new PatternTopic(Constant.SINGLE_DATA_UPDATE_CHANNEL_PREFIX + "*");
    }

    @Bean
    public RedisMessageListenerContainer messageListenerContainer(PatternTopic subscribeTopic, SingleDataListener dataListener) {
        RedisMessageListenerContainer result = new RedisMessageListenerContainer();
        result.setConnectionFactory(redisConnectionFactory);

        result.addMessageListener(dataListener, subscribeTopic);

        return result;
    }


}
