package com.malex.confiuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.malex.service.Task;
import java.util.HashMap;
import java.util.Map;
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
      RedisConnectionFactory connectionFactory, ObjectMapper customObjectMapper) {

    Map<String, RedisCacheConfiguration> configMap = new HashMap<>();
    configMap.put(TASK_CACHE, cacheConfigForSet(Task.class, customObjectMapper));

    return RedisCacheManager.builder(connectionFactory)
        .withInitialCacheConfigurations(configMap)
        .build();
  }

  private RedisCacheConfiguration cacheConfigForSet(Class<?> elementType, ObjectMapper mapper) {
    var valueSerializer =
        RedisSerializationContext.SerializationPair.fromSerializer(
            new CustomClassToSetSerializer(elementType, mapper));
    var keySerializer =
        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer());

    return RedisCacheConfiguration.defaultCacheConfig()
        .serializeKeysWith(keySerializer)
        .serializeValuesWith(valueSerializer)
        .disableCachingNullValues()
        /*
         * Use the given CacheKeyPrefix to compute the prefix for the actual Redis key given the cache name as function input
         */
        .computePrefixWith(cacheName -> cacheName + ":")
        .prefixCacheNameWith("sfsdf");
  }
}
