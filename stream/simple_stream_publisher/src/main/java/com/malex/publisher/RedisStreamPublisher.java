package com.malex.publisher;

import com.malex.publisher.event.MessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
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

    XAddOptions options = XAddOptions.maxlen(MAX_STREAM_LENGTH).approximateTrimming(false);

    var recordId = redisTemplate.opsForStream().add(record, options);
    log.info("RecordId: {}", recordId);
  }
}
