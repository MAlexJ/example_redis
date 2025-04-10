package com.malex.controller;

import com.malex.publisher.RedisPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class RestApiController {

  private final RedisPublisher publisher;

  @GetMapping("/{message}")
  public ResponseEntity<Void> send(@PathVariable String message) {
    publisher.sendMessage(message);
    return ResponseEntity.noContent().build();
  }
}
