package com.malex.configuration;

import io.lettuce.core.RedisBusyException;
import java.time.Duration;
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
public class RedisConfiguration {

  @Value("${server.port}")
  private int serverPort;

  @Value("${redis.stream.name}")
  private String stream;

  @Value("${redis.stream.consumer.group}")
  private String consumerGroup;

  private final String consumerName =
      "consumer-subscriber-without-duplicates-%s".formatted(serverPort);

  @Bean
  public StreamMessageListenerContainer<String, MapRecord<String, String, String>>
      listenerContainer(RedisConnectionFactory redisConnectionFactory) {

    // Configure a poll timeout for the BLOCK option during reading.
    var options =
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
            .pollTimeout(Duration.ofSeconds(1))
            .build();

    return StreamMessageListenerContainer.create(redisConnectionFactory, options);
  }

  @Bean
  public Subscription streamListenerContainer(
      StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer,
      StringRedisTemplate stringRedisTemplate,
      StreamListener<String, MapRecord<String, String, String>> messageListener) {

    // Create a consumer group if it does not already exist
    createConsumerGroupIfNeeded(stringRedisTemplate);

    // Start the listener container
    listenerContainer.start();

    // Subscribe to the stream
    return listenerContainer.receive(
        Consumer.from(consumerGroup, consumerName),
        /*
         * Create a stream offset for the MESSAGE_STREAM_JSON stream,
         * starting from the last consumed message in the consumer group.
         */
        StreamOffset.create(stream, ReadOffset.lastConsumed()),
        messageListener);
  }

  private void createConsumerGroupIfNeeded(StringRedisTemplate stringRedisTemplate) {
    StreamOperations<String, Object, Object> streamOps = stringRedisTemplate.opsForStream();

    try {
      streamOps.createGroup(
          stream,
          // Start reading from the very beginning of the stream
          ReadOffset.from("0"),
          consumerGroup);
      log.info("Consumer group '{}' created", consumerGroup);
    } catch (RedisSystemException e) {
      if (e.getCause() instanceof RedisBusyException) {
        log.info("Group '{}' already exists", consumerGroup);
      } else {
        throw e;
      }
    }
  }
}
