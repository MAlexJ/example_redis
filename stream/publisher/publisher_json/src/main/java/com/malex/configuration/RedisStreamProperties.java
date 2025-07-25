package com.malex.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "redis.stream")
public class RedisStreamProperties {
    private String name;
    private String key;
    private long maxLength;
} 