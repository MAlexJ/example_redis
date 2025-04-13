package com.malex.latest.subsctiber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.malex.latest.subsctiber.event.MessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageStreamListener
    implements StreamListener<String, MapRecord<String, String, String>> {

  private static final String STREAM_JSON_KEY = "message";

  private final ObjectMapper objectMapper;

  @Override
  public void onMessage(MapRecord<String, String, String> message) {
    log.info("Received form stream: {}", message.getStream());
    try {
      String json = message.getValue().get(STREAM_JSON_KEY);
      MessageEvent messageEvent = objectMapper.readValue(json, MessageEvent.class);
      log.info("Received message event: {}", messageEvent);
    } catch (JsonProcessingException e) {
      log.error(e.getMessage(), e);
    }
  }
}
