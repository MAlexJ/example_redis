package com.malex.controller;

import com.malex.service.Task;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

public record TaskRequest(String name) {

  public TaskRequest {
    Objects.requireNonNull(name, "Name must not be null");
  }

  public Task toTask() {
    return new Task(UUID.randomUUID().toString(), name, LocalDateTime.now(ZoneId.of("UTC")));
  }
}
