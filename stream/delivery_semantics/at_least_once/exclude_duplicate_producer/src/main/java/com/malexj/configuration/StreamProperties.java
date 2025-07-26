package com.malexj.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "redis.stream")
public class StreamProperties {

  private String name;

  private String key;

  private long maxLength;
}
