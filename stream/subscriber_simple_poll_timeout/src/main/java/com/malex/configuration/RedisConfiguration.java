package com.malex.configuration;

import com.malex.subsctiber.MessageStreamListener;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisConfiguration {

  private final String streamKey = "message-stream";
  private final String groupName = "message-group";

  @Bean
  public StreamMessageListenerContainer<String, ObjectRecord<String, String>>
      messageListenerContainer(RedisConnectionFactory redisConnectionFactory) {

    // Configure a poll timeout for the BLOCK option during reading.
    var options =
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
            .pollTimeout(Duration.ofSeconds(1))
            .targetType(String.class)
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
      StreamMessageListenerContainer<String, ObjectRecord<String, String>> listenerContainer,
      StreamListener<String, ObjectRecord<String, String>> messageListener) {

    // Start the listener container to begin consuming messages from the Redis stream
    listenerContainer.start();

    // Subscribe to the stream and automatically acknowledge messages
    return listenerContainer.receiveAutoAck(
        // Create a consumer with a unique name within the specified consumer group
        Consumer.from(groupName, "consumer-" + UUID.randomUUID()),
        // Tell Redis to start reading from the last consumed message
        StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
        // The listener that will handle each incoming message
        messageListener);
  }
}
