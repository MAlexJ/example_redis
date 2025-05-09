package com.malex.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.malex.publisher.event.MessageEvent;

@Configuration
public class RedisConfiguration {

    @Bean
    public RedisTemplate<String, MessageEvent> redisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, MessageEvent> template = new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);

        // Key , Value serializer
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());

        // Hash Key , Value serializer
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());

        return template;
    }
}
