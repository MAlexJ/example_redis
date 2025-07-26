package com.malex.rest;

import com.malex.publisher.StreamPublisher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api/messages")
@RequiredArgsConstructor
public class MessageController {

  private final StreamPublisher publisher;

  @PostMapping
  public ResponseEntity<Void> sendMessage(@RequestBody @Valid MessageRequest request) {
    publisher.publishEventAsJson(request.mapToEvent());
    return ResponseEntity.noContent().build();
  }
}
