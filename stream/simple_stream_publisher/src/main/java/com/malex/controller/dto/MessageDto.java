package com.malex.controller.dto;

import com.malex.publisher.event.MessageEvent;
import java.time.LocalDateTime;

public record MessageDto(String sender, String content, LocalDateTime timestamp) {

  public MessageEvent toEvent() {
    return new MessageEvent(sender, content, timestamp);
  }
}
