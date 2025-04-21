package com.malex.controller;

import com.malex.service.Task;
import java.time.LocalDateTime;

public record TaskRequest(String id, String name) {

  Task toTask() {
    return new Task(id, name, LocalDateTime.now());
  }
}
