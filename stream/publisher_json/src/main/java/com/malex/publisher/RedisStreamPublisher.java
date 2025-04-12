package com.malex.publisher;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.malex.publisher.event.MessageEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStreamPublisher {

    private static final String STREAM_CHAT = "message-stream-json";

    private static final String STREAM_JSON_KEY = "message";

    private final RedisTemplate<String, MessageEvent> redisTemplate;

    // keep latest 10 messages
    private static final long MAX_STREAM_LENGTH = 3L;

    // Set stream trimming options: keep only the latest N messages
    private final XAddOptions options = XAddOptions.maxlen(MAX_STREAM_LENGTH).approximateTrimming(false);


    /**
     * Publishes a MessageEvent to a Redis stream in JSON format.
     *
     * <p>The event is serialized into a JSON string and stored in the stream as a single field
     * "data". The stream is trimmed to keep only the latest {@code MAX_STREAM_LENGTH} entries.
     *
     * @param event the MessageEvent to publish
     */
    public void publishEventAsJson(MessageEvent event) {

        // Create a stream record with the specified key and message map
        Map<String, Object> messageMap = Map.of(STREAM_JSON_KEY, event);
        MapRecord<String, String, Object> mapRecord = MapRecord.create(STREAM_CHAT, messageMap);

        // Publish the record to the Redis stream
        var recordId = redisTemplate.opsForStream().add(mapRecord, options);

        // log record
        log.info("Published event to stream. RecordId: {}", recordId);
    }
}
