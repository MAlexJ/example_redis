package com.malex.consumer;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.malex.consumer.event.MessageEvent;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class RedisStreamConsumer {

    private static final String STREAM_KEY = "stream:message-stream";

    private final RedisTemplate<String, MessageEvent> redisTemplate;

    private StreamOperations<String, String, MessageEvent> streamOps;

    @PostConstruct
    public void init() {
        this.streamOps = redisTemplate.opsForStream();
    }

    @SuppressWarnings("unchecked")
    @Scheduled(fixedDelay = 3000)
    public void consumeStream() {
        Optional.ofNullable(streamOps.read(StreamOffset.fromStart(STREAM_KEY)))
                .ifPresent(mapRecord -> mapRecord.forEach(readEvent()));
    }

    private Consumer<MapRecord<String, String, MessageEvent>> readEvent() {
        return event -> {
            RecordId id = event.getId();
            Map<String, MessageEvent> eventMap = event.getValue();
            log.info("Consumed record id : {}, event : {}", id, eventMap);
        };
    }
}
