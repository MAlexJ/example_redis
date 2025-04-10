package com.malex.publisher;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisPublisher {

  private final String redisChannel;

  private final StringRedisTemplate redisTemplate;

  public void sendMessage(String message) {
    redisTemplate.convertAndSend(redisChannel, message);
  }
}
