package com.malex.configuration;

import com.malex.subsctiber.MessageStreamListener;
import io.lettuce.core.RedisBusyException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisConfiguration {

  @Value("${server.port}")
  private int serverPort;

  private static final String MESSAGE_STREAM_JSON = "message-stream-json";

  private static final String MESSAGE_GROUP = "message-group-offset-beginning-of-stream";

  private final String consumerName = "consumer-offset-%s".formatted(serverPort);

  @Bean
  public StreamMessageListenerContainer<String, MapRecord<String, String, String>>
      messageListenerContainer(RedisConnectionFactory redisConnectionFactory) {

    // Configure a poll timeout for the BLOCK option during reading.
    var options =
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
            .pollTimeout(Duration.ofSeconds(1))
            .build();

    return StreamMessageListenerContainer.create(redisConnectionFactory, options);
  }

  /**
   * Registers a Redis stream consumer and starts listening for messages.
   *
   * @param messageListener the listener that handles, see {@link MessageStreamListener
   *     implementation} for more details.
   */
  @Bean
  public Subscription subscription(
      StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer,
      StreamListener<String, MapRecord<String, String, String>> messageListener,
      StringRedisTemplate stringRedisTemplate) {

    // Create a consumer group if it does not already exist
    createConsumerGroupIfNeeded(stringRedisTemplate);

    // Start the listener container to begin consuming messages from the Redis stream
    listenerContainer.start();

    // Subscribe to the stream and automatically acknowledge messages
    return listenerContainer.receiveAutoAck(
        /*
         * Create a consumer with a unique name within the specified consumer group
         * group – name of the consumer group, must not be null or empty.
         * name – name of the consumer, must not be null or empty.
         *              */
        Consumer.from(MESSAGE_GROUP, consumerName),

        // Tell Redis to start reading from the earliest available message
        StreamOffset.create(MESSAGE_STREAM_JSON, ReadOffset.lastConsumed()),

        // The listener that will handle each incoming message
        messageListener);
  }

  private void createConsumerGroupIfNeeded(StringRedisTemplate stringRedisTemplate) {
    StreamOperations<String, Object, Object> streamOps = stringRedisTemplate.opsForStream();

    try {
      streamOps.createGroup(
          MESSAGE_STREAM_JSON,
          ReadOffset.from("0"), // Start reading from the very beginning of the stream
          MESSAGE_GROUP);
      log.info("Consumer group '{}' created", MESSAGE_GROUP);
    } catch (RedisSystemException e) {
      if (e.getCause() instanceof RedisBusyException) {
        log.info("Group '{}' already exists", MESSAGE_GROUP);
      } else {
        throw e;
      }
    }
  }
}
