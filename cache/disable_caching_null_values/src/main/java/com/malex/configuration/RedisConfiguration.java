package com.malex.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfiguration {

  public static final String TASK_CACHE = "task_cache";
  public static final String TASK_CACHE_KEY = "'task'";

  @Bean
  public ObjectMapper customObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    // support LocalDateTime, records
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return objectMapper;
  }

  @Bean
  public RedisCacheManager redisCacheConfiguration(
      RedisConnectionFactory connectionFactory, RedisCacheConfiguration configuration) {
    return RedisCacheManager.builder(connectionFactory)
        .withCacheConfiguration(TASK_CACHE, configuration)
        .build();
  }

  @Bean
  public RedisCacheConfiguration configuration(ObjectMapper mapper) {
    var valueSerializer =
        RedisSerializationContext.SerializationPair.fromSerializer(
            new CustomTaskListSerializer<>(mapper));
    var keySerializer =
        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer());

    return RedisCacheConfiguration.defaultCacheConfig()
        .serializeKeysWith(keySerializer)
        .serializeValuesWith(valueSerializer)
        .entryTtl(Duration.ofMinutes(5))
        .disableCachingNullValues()
        /*
         * Use the given CacheKeyPrefix to compute the prefix for the actual Redis key given the cache name as function input
         */
        .computePrefixWith(cacheName -> cacheName + ":");
  }
}
