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
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

@Slf4j
@Configuration
public class RedisStreamConfiguration {

  @Value("${server.port}")
  private int serverPort;

  public static final String STREAM_KEY = "ack-auto-stream";
  private static final String CONSUMER_GROUP = "ack-auto-stream-group";

  private final String consumerName = "ack-auto-stream-consumer-name-%s".formatted(serverPort);

  @Bean
  public Subscription redisStreamListener(
      StringRedisTemplate redisTemplate, RedisConnectionFactory redisConnectionFactory) {

    // Create consumer group if not exists
    createConsumerGroupIfNeeded(redisTemplate);

    // Explicitly specify type parameters here
    var options =
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
            .pollTimeout(Duration.ofSeconds(1))
            .build();

    var container = StreamMessageListenerContainer.create(redisConnectionFactory, options);

    container.start();

    return container.receiveAutoAck(
        Consumer.from(CONSUMER_GROUP, consumerName),
        StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed()),
        message -> log.info("Received message: {}", message.getValue()));
  }

  private void createConsumerGroupIfNeeded(StringRedisTemplate stringRedisTemplate) {
    try {
      stringRedisTemplate.opsForStream().createGroup(STREAM_KEY, CONSUMER_GROUP);
    } catch (RedisSystemException e) {
      if (e.getCause() instanceof RedisBusyException) {
        log.info("Group '{}' already exists", STREAM_KEY);
      } else {
        throw e;
      }
    }
  }
}
