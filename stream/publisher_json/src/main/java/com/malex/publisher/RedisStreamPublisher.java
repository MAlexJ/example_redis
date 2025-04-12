package com.malex.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.malex.publisher.event.MessageEvent;
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

  private final ObjectMapper mapper;

  // keep latest 10 messages
  private static final long MAX_STREAM_LENGTH = 10L;

  /**
   * Publishes a MessageEvent to a Redis stream in JSON format.
   *
   * <p>The event is serialized into a JSON string and stored in the stream as a single field
   * "data". The stream is trimmed to keep only the latest {@code MAX_STREAM_LENGTH} entries.
   *
   * @param event the MessageEvent to publish
   */
  public void publishEventAsJson(MessageEvent event) {
    try {
      // Convert the MessageEvent object to a JSON string
      String json = mapper.writeValueAsString(event);

      // Create a record map with a single field "data" containing the JSON
      Map<String, String> messageMap = new HashMap<>();
      messageMap.put("data", json);

      // Create a stream record with the specified key and message map
      MapRecord<String, String, String> mapRecord = MapRecord.create(STREAM_KEY, messageMap);

      // Set stream trimming options: keep only the latest N messages
      XAddOptions options = XAddOptions.maxlen(MAX_STREAM_LENGTH).approximateTrimming(false);

      // Publish the record to the Redis stream
      var recordId = redisTemplate.opsForStream().add(mapRecord, options);
      log.info("Published event to stream. RecordId: {}", recordId);

    } catch (JsonProcessingException e) {
      // Log an error if JSON serialization fails
      log.error("Failed to serialize MessageEvent to JSON", e);
    }
  }
}
