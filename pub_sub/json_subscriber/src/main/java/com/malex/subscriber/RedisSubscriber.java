package com.malex.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.malex.sse.SseEmitterPool;
import com.malex.subscriber.event.MessageEvent;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

  private final ObjectMapper objectMapper;

  private final SseEmitterPool emitterPool;

  @Override
  public void onMessage(Message message, byte[] pattern) {
    var json = new String(message.getBody(), StandardCharsets.UTF_8);
    try {
      var event = objectMapper.readValue(json, MessageEvent.class);

      log.info(
          "New message from {}: {}, time: {}", event.sender(), event.content(), event.timestamp());

      emitterPool.sendToAll(event);

    } catch (Exception e) {
      log.error("Failed to parse message: {}", json, e);
    }
  }
}
