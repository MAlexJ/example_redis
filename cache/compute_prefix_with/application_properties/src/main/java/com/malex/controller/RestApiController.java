package com.malex.controller;

import com.malex.service.RedisCacheService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class RestApiController {

  private final RedisCacheService redisCacheService;

  @GetMapping
  public ResponseEntity<List<TaskResponse>> getTasks() {
    return ResponseEntity.ok(
        redisCacheService.getAllTasks().stream().map(TaskResponse::new).toList());
  }

  @PostMapping
  public ResponseEntity<Void> saveTask(@RequestBody TaskRequest task) {
    redisCacheService.addTask(task.toTask());
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping
  public ResponseEntity<Void> removeAllTasks() {
    redisCacheService.removeAllTasks();
    return ResponseEntity.noContent().build();
  }
}
