package com.malex.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfiguration {

  @Bean
  public String redisChannel() {
    return "r_chanel";
  }

  @Bean
  public StringRedisTemplate template(RedisConnectionFactory connectionFactory) {

    /*
     * String-focused extension of RedisTemplate.
     * Since most operations against Redis are String based, this class provides a dedicated class that minimizes configuration
     * of its more generic template especially in terms of serializers.
     *
     * used:
     *
     *  this.setKeySerializer(RedisSerializer.string());
     *  this.setValueSerializer(RedisSerializer.string());
     *
     *  this.setHashKeySerializer(RedisSerializer.string());
     *  this.setHashValueSerializer(RedisSerializer.string());
     */

    return new StringRedisTemplate(connectionFactory);
  }
}
