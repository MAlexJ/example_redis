package com.malexj.producer;

import com.malexj.configuration.StreamProperties;
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
public class MessageStreamPublisher {

  private final RedisTemplate<String, MessageEvent> redisTemplate;
  private final StreamProperties streamProperties;

  /*
   * Publishes a MessageEvent to a Redis stream in JSON format.
   *
   * The event is serialized into a JSON string and stored in the stream as single field "data".
   * The stream is trimmed to keep only the latest {@code MAX_STREAM_LENGTH} entries.
   */
  public void publishEventAsJson(MessageEvent event) {
    // Create a stream record with the specified key and message map
    var mapRecord = createStreamRecord(event);

    // Publish the record to the Redis stream
    var recordId = redisTemplate.opsForStream().add(mapRecord, getOptions());

    // log record
    log.info("Published event to stream. RecordId: {}", recordId);
  }

  private MapRecord<String, String, Object> createStreamRecord(MessageEvent event) {
    var streamName = streamProperties.getName();
    var streamKey = streamProperties.getKey();
    Map<String, Object> messageMap = Map.of(streamKey, event);
    return MapRecord.create(streamName, messageMap);
  }

  /*
   * Set stream trimming options: keep only the latest N messages
   */
  private XAddOptions getOptions() {
    return XAddOptions.maxlen(streamProperties.getMaxLength()).approximateTrimming(false);
  }
}
