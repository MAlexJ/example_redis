package com.malex.controller;

import com.malex.controller.dto.MessageDto;
import com.malex.publisher.RedisStreamPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api")
@RequiredArgsConstructor
public class RestApiController {

  private final RedisStreamPublisher publisher;

  @PostMapping("/messages")
  public ResponseEntity<Void> sendMessage(@RequestBody MessageDto dto) {
    publisher.publishEventAsJson(dto.toEvent());
    return ResponseEntity.noContent().build();
  }
}
