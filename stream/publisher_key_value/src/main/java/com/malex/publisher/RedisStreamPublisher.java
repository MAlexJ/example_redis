package com.malex.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.malex.publisher.event.MessageEvent;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions;
import org.springframework.data.redis.connection.stream.MapRecord;
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

  /*
   * Publishes a MessageEvent to a Redis stream by storing each field separately as key-value pairs
   * instead of serializing the whole object into JSON.
   *
   * This format is easier to query and debug from Redis CLI or other tools.
   *
   * @param event the message event to publish
   */
  public void publishEventAsFields(MessageEvent event) {
    // Create a map with individual fields of MessageEvent
    Map<String, String> messageMap = new HashMap<>();
    messageMap.put("sender", String.valueOf(event.sender()));
    messageMap.put("content", event.content());
    // time in UTC format
    messageMap.put("timestamp", event.timestamp().atZone(ZoneOffset.UTC).toInstant().toString());

    // Create a Redis Stream record
    MapRecord<String, String, String> record = MapRecord.create(STREAM_KEY, messageMap);

    // Trimming options: keep only the latest N entries
    XAddOptions options = XAddOptions.maxlen(MAX_STREAM_LENGTH).approximateTrimming(false);

    // Add to Redis stream
    var recordId = redisTemplate.opsForStream().add(record, options);
    log.info("Published event with fields to stream. RecordId: {}", recordId);
  }
}
