package com.malex.consumer;

import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.malex.consumer.event.MessageEvent;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class RedisStreamConsumer {

    private static final String STREAM_KEY = "stream:message-stream";


    private final RedisTemplate<String, MessageEvent> redisTemplate;

    private final ObjectMapper objectMapper;

    private StreamOperations<String, String, String> streamOps;

    @PostConstruct
    public void init() {
        this.streamOps = redisTemplate.opsForStream();
    }

    @Scheduled(fixedDelay = 1000)
    public void consumeStream() {
        try {
            List<MapRecord<String, String, String>> records = streamOps.read(StreamOffset.fromStart(STREAM_KEY));

            for (MapRecord<String, String, String> record : records) {
                Object rawMessage = record.getValue().get("message");

                if (rawMessage instanceof MessageEvent message) {
                    log.info("Consumed event: {}", message);
                } else {
                    log.warn("Unexpected message type: {}, value: {}",
                            rawMessage != null ? rawMessage.getClass().getName() : "null", rawMessage);
                }
            }
        } catch (Exception e) {
            log.error("Failed to consume from Redis stream", e);
        }
    }
}
