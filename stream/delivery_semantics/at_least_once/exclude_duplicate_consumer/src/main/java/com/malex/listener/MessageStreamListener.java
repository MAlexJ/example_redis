package com.malex.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageStreamListener
    implements StreamListener<String, MapRecord<String, String, String>> {

  @Value("${redis.stream.field.name}")
  private String fieldName;

  @Value("${redis.stream.consumer.group}")
  private String consumerGroup;

  @Value("${redis.stream.processed-message-ttl-minutes}")
  private int processedMessageTtlSeconds;

  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;

  /*
   * Redis stream message listener that implements at-least-once message delivery
   * with duplicate protection using Redis as a marker store.
   *
   * This method ensures that each message is processed only once even if Redis re-delivers
   * the same message due to a crash, restart, or lack of acknowledgement.
   */
  @Override
  public void onMessage(MapRecord<String, String, String> objectRecord) {
    // Extract the unique message ID from the stream record
    String messageId = objectRecord.getId().getValue();

    /*
     * Construct a Redis key to mark the message as processed
     *
     * verify key in CLI: SCAN 0 MATCH processed:* COUNT 100
     */
    String processedKey = "processed:" + messageId;

    // Check if this message has already been processed (exists in Redis)
    boolean alreadyProcessed = redisTemplate.hasKey(processedKey);
    if (alreadyProcessed) {
      // If the message is already processed, log and acknowledge it to remove from pending
      log.info("Duplicate message: {}", messageId);
      acknowledge(objectRecord);
      return;
    }

    // Extract the actual message payload (key-value map)
    Map<String, String> eventMap = objectRecord.getValue();

    try {
      // Deserialize the message to MessageEvent
      MessageEvent messageEvent = deserializeToMessageEvent(eventMap);

      // Business logic goes here — e.g., process the event
      log.info(" >>> Processing MessageEvent: {}", messageEvent);

      // Mark the message as processed in Redis with configurable TTL
      redisTemplate
          .opsForValue()
          .set(processedKey, "done", Duration.ofMinutes(processedMessageTtlSeconds));

      // Acknowledge the message to Redis (XACK) to remove it from the Pending Entries List (PEL)
      acknowledge(objectRecord);

    } catch (Exception e) {
      // If an error occurs, do NOT acknowledge the message
      // Redis will keep it in the pending list and allow reprocessing
      log.error("Error while processing message: {}", messageId, e);
    }
  }

  /**
   * Acknowledges the message to Redis, indicating successful processing. This removes the message
   * from the Consumer Group's Pending Entries List (PEL).
   *
   * @param objectRecord the Redis stream record to acknowledge
   */
  private void acknowledge(MapRecord<String, String, String> objectRecord) {
    // Send XACK to Redis, confirming that the message has been successfully handled
    redisTemplate.opsForStream().acknowledge(consumerGroup, objectRecord);
  }

  /**
   * Deserializes the Redis stream map to a MessageEvent object.
   *
   * @param map the message map from Redis stream
   * @return the deserialized MessageEvent
   * @throws JsonProcessingException if deserialization fails
   */
  private MessageEvent deserializeToMessageEvent(Map<String, String> map)
      throws JsonProcessingException {

    /*
     * Get the JSON string from the event map
     * The producer sends the MessageEvent as JSON in a field named "event"
     */
    var jsonString = map.get(fieldName);
    if (jsonString == null) {
      throw new IllegalArgumentException("No 'event' field found in message map");
    }

    // Deserialize JSON to MessageEvent
    return objectMapper.readValue(jsonString, MessageEvent.class);
  }
}
