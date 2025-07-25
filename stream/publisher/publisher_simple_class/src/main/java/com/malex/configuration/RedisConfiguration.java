package com.malex.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.malex.publisher.event.MessageEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfiguration {

  @Bean
  public Jackson2JsonRedisSerializer<MessageEvent> jsonRedisSerializer(ObjectMapper objectMapper) {
    // Clone the injected ObjectMapper to avoid global side effects
    ObjectMapper redisMapper = objectMapper.copy();

    /*
     * Make ALL fields (including private ones) visible for serialization and deserialization
     * PropertyAccessor.ALL = fields, getters/setters, etc.
     * JsonAutoDetect.Visibility.ANY = even private fields are included
     */
    redisMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

    /*
     * Security of Default Typing:
     * Restrict deserialization to only classes in the com.malex.publisher.event package.
     * This is a security measure to prevent deserialization attacks.
     */
    BasicPolymorphicTypeValidator ptv =
        BasicPolymorphicTypeValidator.builder().allowIfSubType("com.malex.publisher.event").build();

    /*
     * Enable default typing for polymorphic type handling (e.g. when you have abstract types, interfaces)
     * This will include class metadata like "@class": "com.example.MyClass" in the JSON
     * NON_FINAL = only apply this to non-final classes (not String, Integer, etc.)
     * BasicPolymorphicTypeValidator is a safe way to whitelist which classes can be deserialized
     */
    redisMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);

    // Better for LocalDateTime
    redisMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // Jackson serializer with pre-configured mapper
    return new Jackson2JsonRedisSerializer<>(redisMapper, MessageEvent.class);
  }

  /*
   * RedisTemplate bean for MessageEvent objects.
   *
   * Note: By default, RedisTemplate does not handle null values. If you need to cache nulls,
   * consider additional logic.
   *
   * Performance: Jackson's serialization is flexible but may be slower than simple string/byte serialization.
   * Monitor if high throughput is required.
   */
  @Bean(name = "messageEventRedisTemplate")
  public RedisTemplate<String, MessageEvent> redisTemplate(
      RedisConnectionFactory connectionFactory,
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
