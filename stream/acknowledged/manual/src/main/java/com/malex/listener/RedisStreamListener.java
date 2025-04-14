package com.malex.listener;

import static com.malex.configuration.RedisStreamConfiguration.CONSUMER_GROUP;
import static com.malex.configuration.RedisStreamConfiguration.STREAM_KEY;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStreamListener
    implements StreamListener<String, MapRecord<String, String, String>> {

  private final StringRedisTemplate redisTemplate;

  @Override
  public void onMessage(MapRecord<String, String, String> message) {
    try {
      log.info("Received message: {}", message.getValue());

      // Manually acknowledge the message
      redisTemplate.opsForStream().acknowledge(STREAM_KEY, CONSUMER_GROUP, message.getId());

    } catch (Exception e) {
      log.error("Error processing message: {}, error: {} ", message.getId(), e.getMessage());
      // Handle errors (retry, log, etc.)
    }
  }
}
