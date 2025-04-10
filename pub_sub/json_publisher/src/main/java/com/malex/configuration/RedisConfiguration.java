package com.malex.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.listener.ChannelTopic;

@Configuration
public class RedisConfiguration {

  // Define the Redis channel/topic
  @Bean
  public ChannelTopic topic() {
    return new ChannelTopic("r_topic");
  }
}
