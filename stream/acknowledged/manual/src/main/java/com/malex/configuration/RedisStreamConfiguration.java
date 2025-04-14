package com.malex.configuration;

import com.malex.listener.RedisStreamListener;
import io.lettuce.core.RedisBusyException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisStreamConfiguration {

  @Value("${server.port}")
  private int serverPort;

  public static final String STREAM_KEY = "ack-manual-stream";
  public static final String CONSUMER_GROUP = "ack-manual-stream-group";

  private final String consumerName = "ack-manual-stream-consumer-name-%s".formatted(serverPort);

  private final RedisStreamListener streamListener;

  @Bean
  public Subscription manualAckSubscription(
      StringRedisTemplate redisTemplate, RedisConnectionFactory redisConnectionFactory) {

    // Create consumer group if it doesn't exist
    createConsumerGroupIfNeeded(redisTemplate);

    var options =
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
            .pollTimeout(Duration.ofSeconds(1))
            .build();

    var container = StreamMessageListenerContainer.create(redisConnectionFactory, options);

    Subscription subscription =
        container.receive(
            Consumer.from(CONSUMER_GROUP, consumerName),
            StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed()),
            streamListener // Injected listener class
            );

    container.start();

    return subscription;
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
