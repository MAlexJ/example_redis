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

    /*
     * Make ALL fields (including private ones) visible for serialization and deserialization
     * PropertyAccessor.ALL = fields, getters/setters, etc.
     * JsonAutoDetect.Visibility.ANY = even private fields are included
     */
    objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

    /*
     * Security of Default Typing:
     * Risk: As before, enabling default typing can be a security risk.
     *
     * This code is a security measure to ensure that Jackson can deserialize only your own event classes
     * when using polymorphic type handling with Redis. This is a best practice when enabling default typing.
     */
    BasicPolymorphicTypeValidator ptv =
        BasicPolymorphicTypeValidator.builder().allowIfSubType("com.malex.publisher.event").build();

    /*
     * Enable default typing for polymorphic type handling (e.g. when you have abstract types, interfaces)
     * This will include class metadata like "@class": "com.example.MyClass" in the JSON
     * NON_FINAL = only apply this to non-final classes (not String, Integer, etc.)
     * BasicPolymorphicTypeValidator is a safe way to whitelist which classes can be deserialized
     */
    objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);

    // Better for LocalDateTime
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // Jackson serializer with pre-configured mapper
    return new Jackson2JsonRedisSerializer<>(objectMapper, MessageEvent.class);
  }

  @Bean
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
