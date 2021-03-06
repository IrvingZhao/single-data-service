package com.xlb.service.data.client.config;

import com.xlb.service.data.client.listener.SingleDataListener;
import com.xlb.service.data.client.manager.SingleDataManager;
import com.xlb.service.data.client.util.http.HttpClient;
import com.xlb.service.data.client.util.http.config.ClientConfig;
import com.xlb.service.data.client.util.http.enums.KeyStoreType;
import com.xlb.service.data.constant.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.nio.charset.StandardCharsets;

@Configuration
@Slf4j
public class RedisListenerConfig {

    private final RedisConnectionFactory redisConnectionFactory;

    public RedisListenerConfig(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Value("${project.singleData.baseUrl}")
    private String baseUrl;

    @Value("${project.singleData.needCert}")
    private Boolean needCert;

    @Value("${project.singleData.clientStore}")
    private String clientStore;

    @Value("${project.singleData.clientStorePass}")
    private String clientStoreKey;

    @Value("${project.singleData.clientStoreType}")
    private KeyStoreType clientStoreType;

    @Bean
    public SingleDataManager dataManager() {
        var builder = ClientConfig.builder().charset(StandardCharsets.UTF_8);
        if (needCert) {
            log.info("enable client verification");
            builder.clientStore(clientStore)
                    .clientStoreKey(clientStoreKey)
                    .clientType(clientStoreType)
                    .trustStore("classpath:server.jks")
                    .trustStoreKey("server")
                    .trustType(KeyStoreType.JKS);
        }
        return new SingleDataManager(new HttpClient(builder.build()), baseUrl);
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
