package com.malex.confiuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.malex.service.Task;

import java.io.IOException;
import java.util.Set;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

public class CustomTaskSetSerializer<T> implements RedisSerializer<T> {

  private final ObjectMapper objectMapper;

  private final JavaType javaType;

  public CustomTaskSetSerializer(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.javaType = objectMapper.getTypeFactory().constructCollectionType(Set.class, Task.class);
  }

  @Override
  public byte[] serialize(T value) throws SerializationException {
    if (value == null) return new byte[0];
    try {
      return objectMapper.writeValueAsBytes(value);
    } catch (JsonProcessingException e) {
      throw new SerializationException("Serialization failed", e);
    }
  }

  @Override
  public T deserialize(byte[] bytes) throws SerializationException {
    if (bytes == null || bytes.length == 0) return null;
    try {
      return objectMapper.readValue(bytes, javaType);
    } catch (IOException e) {
      throw new SerializationException("Deserialization failed", e);
    }
  }
}
