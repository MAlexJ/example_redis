package com.malex.publisher;

import com.malex.publisher.event.MessageEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisStreamCommands;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisStreamPublisher {

  private static final String STREAM_KEY = "message-stream";

  private final RedisTemplate<String, MessageEvent> redisTemplate;

  // keep latest 10 messages
  private static final long MAX_STREAM_LENGTH = 10L;

  public void publishEvent(MessageEvent event) {
    ObjectRecord<String, MessageEvent> record =
        StreamRecords.objectBacked(event).withStreamKey(STREAM_KEY);

    var options =
        RedisStreamCommands.XAddOptions.maxlen(MAX_STREAM_LENGTH)
            .approximateTrimming(true); // explicitly set approximate mode

    redisTemplate.opsForStream().add(record, options);
  }
}
