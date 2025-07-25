package com.malex.controller.dto;

import com.malex.publisher.event.MessageEvent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record MessageDto(
    @NotBlank String sender, @NotBlank String content, @NotNull LocalDateTime timestamp) {

  public MessageEvent toEvent() {
    return new MessageEvent(sender, content, timestamp);
  }
}
