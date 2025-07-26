package com.malex.configuration;

import com.malex.publisher.event.MessageEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableConfigurationProperties(StreamProperties.class)
public class TemplateConfiguration {

  @Bean
  public RedisTemplate<String, MessageEvent> redisTemplate(
      RedisConnectionFactory connectionFactory, RedisSerializer<MessageEvent> redisSerializer) {

    RedisTemplate<String, MessageEvent> template = new RedisTemplate<>();

    template.setConnectionFactory(connectionFactory);

    // Key, Value serializer
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(redisSerializer);

    // Hash: Key , Value serializer
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(redisSerializer);

    return template;
  }
}
