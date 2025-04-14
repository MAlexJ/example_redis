package com.malex.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
  public Jackson2JsonRedisSerializer<MessageEvent> jsonRedisSerializer() {
    // Custom ObjectMapper
    ObjectMapper objectMapper = new ObjectMapper();

    /*
     * Make ALL fields (including private ones) visible for serialization and deserialization
     * PropertyAccessor.ALL = fields, getters/setters, etc.
     * JsonAutoDetect.Visibility.ANY = even private fields are included
     */
    objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

    /*
     * Enable default typing for polymorphic type handling (e.g. when you have abstract types, interfaces)
     * This will include class metadata like "@class": "com.example.MyClass" in the JSON
     * NON_FINAL = only apply this to non-final classes (not String, Integer, etc.)
     * BasicPolymorphicTypeValidator is a safe way to whitelist which classes can be deserialized
     */
    objectMapper.activateDefaultTyping(
        BasicPolymorphicTypeValidator.builder().build(), ObjectMapper.DefaultTyping.NON_FINAL);

    // Register JavaTimeModule to handle Java 8+ date/time types (LocalDate, LocalDateTime, etc.)
    objectMapper.registerModule(new JavaTimeModule());

    /*
     * Disable writing dates as numeric timestamps (like 1688741820000)
     * Instead, dates will be written as readable ISO-8601 strings (e.g. "2023-07-07T15:30:00")
     */
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

    // Key, Value serializer
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(jsonRedisSerializer);

    // Hash: Key , Value serializer
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(jsonRedisSerializer);

    return template;
  }
}
