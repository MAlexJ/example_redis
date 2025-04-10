package com.malex.subscriber;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisSubscriber {

  public void handleMessage(String message) {
    log.info("received message: {}", message);
  }
}
