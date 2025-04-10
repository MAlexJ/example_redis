package com.malex.controller;

import com.malex.controller.dto.MessageDto;
import com.malex.publisher.RedisPublisher;
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

  private final RedisPublisher redisPublisher;

  @PostMapping("/messages")
  public ResponseEntity<Void> sendMessage(@RequestBody MessageDto dto) {
    redisPublisher.sendMessage(dto.toEvent());
    return ResponseEntity.noContent().build();
  }
}
