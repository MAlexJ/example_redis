package com.malex.subsctiber;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessageStreamListener implements StreamListener<String, ObjectRecord<String, String>> {

  @Override
  public void onMessage(ObjectRecord<String, String> message) {
    log.info("Received form stream: {}", message.getStream());
    log.info("Received message: {}", message.getValue());
  }
}
