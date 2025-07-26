package com.malexj.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class SerializerConfiguration {

  @Bean
  public RedisSerializer<Object> redisSerializer(ObjectMapper objectMapper) {
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
     * Restrict deserialization to only classes in the "com.example.models"  package.
     * This is a security measure to prevent deserialization attacks.
     */
    BasicPolymorphicTypeValidator ptv =
        BasicPolymorphicTypeValidator.builder().allowIfSubType("com.malexj.producer").build();

    /*
     * Enable default typing for polymorphic type handling (e.g. when you have abstract types, interfaces)
     * This will include class metadata like "@class": "com.example.MyClass" in the JSON
     * NON_FINAL = only apply this to non-final classes (not String, Integer, etc.)
     * BasicPolymorphicTypeValidator is a safe way to whitelist which classes can be deserialized
     */
    redisMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);

    /*
     * Register JavaTimeModule to handle Java 8+ date/time types (LocalDate, LocalDateTime, etc.)
     */
    redisMapper.registerModule(new JavaTimeModule());

    /*
     * Disable writing dates as numeric timestamps (like 1688741820000)
     * Instead, dates will be written as readable ISO-8601 strings (e.g. "2023-07-07T15:30:00")
     */
    redisMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // JSON serializer with pre-configured mapper
    return new GenericJackson2JsonRedisSerializer(redisMapper);
  }
}
