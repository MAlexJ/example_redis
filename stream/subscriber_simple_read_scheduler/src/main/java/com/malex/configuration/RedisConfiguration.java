package com.malex.configuration;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.malex.consumer.event.MessageEvent;

@Configuration
public class RedisConfiguration {

    @Bean
    public Jackson2JsonRedisSerializer<MessageEvent> jsonRedisSerializer() {
        // Custom ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        objectMapper.activateDefaultTyping(BasicPolymorphicTypeValidator.builder().build(),
                ObjectMapper.DefaultTyping.NON_FINAL);

        // Better for LocalDateTime
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Jackson serializer with pre-configured mapper
        return new Jackson2JsonRedisSerializer<>(objectMapper, MessageEvent.class);
    }

    @Bean
    public RedisTemplate<String, MessageEvent> redisTemplate(RedisConnectionFactory connectionFactory,
                                                             Jackson2JsonRedisSerializer<MessageEvent> jsonRedisSerializer) {

        RedisTemplate<String, MessageEvent> template = new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);

        // Key , Value serializer
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jsonRedisSerializer);

        // Hash Key , Value serializer
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jsonRedisSerializer);

        return template;
    }
}
