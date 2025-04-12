package com.malex.configuration;

import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

import com.malex.subsctiber.MessageStreamListener;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisConfiguration {

    private static final String MESSAGE_STREAM_JSON = "message-stream-json";

    private static final String MESSAGE_GROUP = "message-group";

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> messageListenerContainer(
            RedisConnectionFactory redisConnectionFactory) {

        // Configure a poll timeout for the BLOCK option during reading.
        var options = StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                .pollTimeout(Duration.ofSeconds(1)).build();

        return StreamMessageListenerContainer.create(redisConnectionFactory, options);
    }

    /**
     * Registers a Redis stream consumer and starts listening for messages.
     *
     * @param messageListener the listener that handles, see {@link MessageStreamListener
     *                        implementation} for more details.
     */
    @Bean
    public Subscription subscription(
            StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer,
            StreamListener<String, MapRecord<String, String, String>> messageListener) {

        // Start the listener container to begin consuming messages from the Redis stream
        listenerContainer.start();

        // Subscribe to the stream and automatically acknowledge messages
        return listenerContainer.receiveAutoAck(
                // Create a consumer with a unique name within the specified consumer group
                Consumer.from(MESSAGE_GROUP, "consumer-" + UUID.randomUUID()),
                // Tell Redis to start reading from the last consumed message
                StreamOffset.create(MESSAGE_STREAM_JSON, ReadOffset.lastConsumed()),
                // The listener that will handle each incoming message
                messageListener);
    }
}
