package com.malex.publisher;

import com.malex.configuration.StreamProperties;
import com.malex.publisher.event.MessageEvent;
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
public class StreamPublisher {

  private final RedisTemplate<String, MessageEvent> redisTemplate;
  private final StreamProperties streamProperties;

  /*
   * Publishes a MessageEvent to a Redis stream in JSON format.
   *
   * The event is serialized into a JSON string and stored in the stream as single field "data".
   * The stream is trimmed to keep only the latest {@code MAX_STREAM_LENGTH} entries.
   */
  public void publishEventAsJson(MessageEvent event) {
    var streamName = streamProperties.getName();
    var streamKey = streamProperties.getKey();

    // Create a stream record with the specified key and message map
    Map<String, Object> messageMap = Map.of(streamKey, event);
    MapRecord<String, String, Object> mapRecord = MapRecord.create(streamName, messageMap);

    // Publish the record to the Redis stream
    var recordId = redisTemplate.opsForStream().add(mapRecord, getOptions());

    // log record
    log.info("Published event to stream. RecordId: {}", recordId);
  }

  /*
   * Set stream trimming options: keep only the latest N messages
   */
  private XAddOptions getOptions() {
    return XAddOptions.maxlen(streamProperties.getMaxLength()).approximateTrimming(false);
  }
}
