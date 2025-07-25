package com.malex.publisher;

import com.malex.publisher.event.MessageEvent;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStreamPublisher {

  private final String streamName;
  private final String streamKey;
  private final long maxStreamLength;

  private final RedisTemplate<String, MessageEvent> redisTemplate;

  private XAddOptions options;

  public RedisStreamPublisher(
      RedisTemplate<String, MessageEvent> redisTemplate,
      @Value("${redis.stream.name}") String streamName,
      @Value("${redis.stream.key}") String streamKey,
      @Value("${redis.stream.max-length}") long maxStreamLength) {
    this.redisTemplate = redisTemplate;
    this.streamName = streamName;
    this.streamKey = streamKey;
    this.maxStreamLength = maxStreamLength;
    this.options = XAddOptions.maxlen(maxStreamLength).approximateTrimming(false);
  }

  /*
   * Publishes a MessageEvent to a Redis stream in JSON format.
   *
   * The event is serialized into a JSON string and stored in the stream as single field "data".
   * The stream is trimmed to keep only the latest {@code MAX_STREAM_LENGTH} entries.
   */
  public void publishEventAsJson(MessageEvent event) {

    // Create a stream record with the specified key and message map
    Map<String, Object> messageMap = Map.of(streamKey, event);
    MapRecord<String, String, Object> mapRecord = MapRecord.create(streamName, messageMap);

    // Publish the record to the Redis stream
    var recordId = redisTemplate.opsForStream().add(mapRecord, options);

    // log record
    log.info("Published event to stream. RecordId: {}", recordId);
  }
}
