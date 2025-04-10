package com.malex.configuration;

import com.malex.subscriber.RedisSubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
public class RedisSubscriberConfiguration {

  private final RedisSubscriber subscriber;

  @Bean
  public RedisMessageListenerContainer redisContainer(
      RedisConnectionFactory factory, ChannelTopic topic) {

    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(factory);
    container.addMessageListener(subscriber, topic);
    return container;
  }
}
