package com.malex.controller;

import java.util.List;

public record TasksResponse(List<TaskResponse> tasks, int total) {

  public TasksResponse(List<TaskResponse> tasks) {
    this(tasks, tasks.size());
  }
}
