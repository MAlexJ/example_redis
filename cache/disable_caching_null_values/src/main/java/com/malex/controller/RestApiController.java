package com.malex.controller;

import com.malex.service.RedisCacheService;
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
  public ResponseEntity<TasksResponse> findAll() {
    var tasks = redisCacheService.getAllTasks().stream().map(TaskResponse::new).toList();
    return ResponseEntity.ok(new TasksResponse(tasks));
  }

  @PostMapping
  public ResponseEntity<Void> save(@RequestBody TaskRequest request) {

    if (request.name().equals("null")) {
      redisCacheService.addTask(null);
    } else {
      redisCacheService.addTask(request.toTask());
    }
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping
  public ResponseEntity<Void> clear() {
    redisCacheService.removeAllTasks();
    return ResponseEntity.noContent().build();
  }
}
