package com.malexj.configuration;

import com.malexj.producer.MessageEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableConfigurationProperties(StreamProperties.class)
public class TemplateConfiguration {

  @Bean
  public RedisTemplate<String, MessageEvent> redisTemplate(
      RedisConnectionFactory connectionFactory,
      Jackson2JsonRedisSerializer<MessageEvent> jsonRedisSerializer) {

    var template = new RedisTemplate<String, MessageEvent>();

    // connection configuration
    template.setConnectionFactory(connectionFactory);

    // Key: key and value serializer
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(jsonRedisSerializer);

    // Hash: key and value serializer
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(jsonRedisSerializer);

    return template;
  }
}
