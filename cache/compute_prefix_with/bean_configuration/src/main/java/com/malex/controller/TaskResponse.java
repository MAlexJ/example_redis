package com.malex.controller;

import com.malex.service.Task;
import java.time.LocalDateTime;

public record TaskResponse(String id, String name, LocalDateTime dateTime) {

  public TaskResponse(Task task) {
    this(task.id(), task.name(), task.dateTime());
  }
}
