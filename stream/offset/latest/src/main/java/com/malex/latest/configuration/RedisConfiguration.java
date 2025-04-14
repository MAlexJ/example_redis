package com.malex.latest.configuration;

import com.malex.latest.subsctiber.MessageStreamListener;
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

  private static final String CONSUMER_GROUP = "message-group-offset-latest";

  private final String consumerName = "consumer-offset-latest-%s".formatted(serverPort);

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
        // Create a consumer with a unique name within the specified consumer group
        Consumer.from(CONSUMER_GROUP, consumerName),

        /* error ->  StreamOffset.create(MESSAGE_STREAM_JSON, ReadOffset.latest()),
         *
         * Caused by: io.lettuce.core.RedisCommandExecutionException: ERR The $ ID is meaningless in
         * the context of XREADGROUP: you want to read the history of this consumer by specifying a
         * proper ID, or use the > ID to get new messages. The $ ID would just return an emptyresult set.
         */

        // Tell Redis to start reading the latest offset
        StreamOffset.create(MESSAGE_STREAM_JSON, ReadOffset.from(">")),

        // The listener that will handle each incoming message
        messageListener);
  }

  private void createConsumerGroupIfNeeded(StringRedisTemplate stringRedisTemplate) {
    try {
      stringRedisTemplate.opsForStream().createGroup(MESSAGE_STREAM_JSON, CONSUMER_GROUP);
    } catch (RedisSystemException e) {
      if (e.getCause() instanceof RedisBusyException) {
        log.info("Group '{}' already exists", MESSAGE_STREAM_JSON);
      } else {
        throw e;
      }
    }
  }
}
