package com.malex;

import static com.malex.configuration.RedisStreamConfiguration.STREAM_KEY;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.StringRedisTemplate;

@RequiredArgsConstructor
@SpringBootApplication
public class AutoAckApplication implements CommandLineRunner {

  public static void main(String[] args) {
    SpringApplication.run(AutoAckApplication.class, args);
  }

  private final StringRedisTemplate redisTemplate;

  @Override
  public void run(String... args) throws Exception {
    var message = Map.of("field_1", "value1", "field+_2", "value2");
    redisTemplate.opsForStream().add(STREAM_KEY, message);
  }
}
