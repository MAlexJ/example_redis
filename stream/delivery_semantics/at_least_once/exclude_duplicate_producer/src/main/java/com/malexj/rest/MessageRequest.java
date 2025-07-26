package com.malexj.rest;

import com.malexj.producer.MessageEvent;
import java.time.LocalDateTime;
import java.util.Objects;

public record MessageRequest(String title, String content) {

  public MessageRequest {
    Objects.requireNonNull(title, "the title is required");
    Objects.requireNonNull(content, "the content is required");
  }

  public MessageEvent mapToEvent() {
    return new MessageEvent(title, content, LocalDateTime.now());
  }
}
