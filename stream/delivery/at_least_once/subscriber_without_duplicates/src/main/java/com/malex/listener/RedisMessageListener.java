package com.malex.listener;

import static com.malex.configuration.RedisConfiguration.CONSUMER_GROUP;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisMessageListener
    implements StreamListener<String, MapRecord<String, String, String>> {

  private final RedisTemplate<String, String> redisTemplate;

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

    // Extract the actual message payload (key-value map)
    var event = objectRecord.getValue();

    // Construct a Redis key to mark the message as processed
    String processedKey = "processed:" + messageId;

    // Check if this message has already been processed (exists in Redis)
    Boolean alreadyProcessed = redisTemplate.hasKey(processedKey);
    if (Boolean.TRUE.equals(alreadyProcessed)) {
      // If the message is already processed, log and acknowledge it to remove from pending
      log.info("Duplicate message: {}", messageId);
      acknowledge(objectRecord);
      return;
    }

    try {
      // Business logic goes here — e.g., process the event
      log.info("Processing event: {}", event);

      // Mark the message as processed in Redis with a TTL (e.g. 5 seconds)
      // You can increase the TTL (e.g. 1 hour or 24 hours) depending on use case
      redisTemplate.opsForValue().set(processedKey, "1", Duration.ofSeconds(5));

      // Acknowledge the message to Redis (XACK) to remove it from the Pending Entries List (PEL)
      acknowledge(objectRecord);

    } catch (Exception e) {
      // If an error occurs, do NOT acknowledge the message
      // Redis will keep it in the pending list and allow reprocessing
      log.error("Error while processing message: {}", messageId, e);
    }
  }

  /**
   * Acknowledges the message to Redis, indicating successful processing.
   * This removes the message from the Consumer Group's Pending Entries List (PEL).
   *
   * @param objectRecord the Redis stream record to acknowledge
   */
  private void acknowledge(MapRecord<String, String, String> objectRecord) {
    // Send XACK to Redis, confirming that the message has been successfully handled
    redisTemplate.opsForStream().acknowledge(CONSUMER_GROUP, objectRecord);
  }
}
