package com.malex.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.malex.publisher.event.MessageEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisPublisher {

  private final ChannelTopic topic;

  private final ObjectMapper objectMapper;

  private final RedisTemplate<String, String> redisTemplate;

  public void sendMessage(MessageEvent message) {
    try {
      String json = objectMapper.writeValueAsString(message);
      redisTemplate.convertAndSend(topic.getTopic(), json);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Serialization JSON error", e);
    }
  }
}
