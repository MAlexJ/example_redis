package com.malex.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
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

  private final ObjectMapper mapper;

  // keep latest 10 messages
  private static final long MAX_STREAM_LENGTH = 10L;

  /**
   * Publishes a {@link MessageEvent} to a Redis stream.
   *
   * <p>The event is serialized using the configured RedisTemplate serializer and added to the
   * stream under the key {@code message-stream}. The stream is trimmed to keep only the latest
   * {@value MAX_STREAM_LENGTH} entries.
   *
   * @param event the message event to publish
   */
  public void publishEvent(MessageEvent event) {
    // Create a Redis stream record backed by the MessageEvent object
    ObjectRecord<String, MessageEvent> objectRecord =
        StreamRecords.objectBacked(event).withStreamKey(STREAM_KEY);

    // Configure stream options: trim the stream to the last MAX_STREAM_LENGTH entries
    XAddOptions options = XAddOptions.maxlen(MAX_STREAM_LENGTH).approximateTrimming(false);

    // Add the record to the Redis stream and get the generated record ID
    var recordId = redisTemplate.opsForStream().add(objectRecord, options);

    // Log the ID of the added record
    log.info("RecordId: {}", recordId);
  }
}
