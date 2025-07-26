package com.malex.rest;

import com.malex.publisher.event.MessageEvent;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record MessageRequest(@NotBlank String title, @NotBlank String content) {

  public MessageEvent mapToEvent() {
    return new MessageEvent(title, content, LocalDateTime.now());
  }
}
