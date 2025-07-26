package com.malexj.rest;

import com.malexj.producer.MessageStreamPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/events")
@RequiredArgsConstructor
public class MessageController {

  private final MessageStreamPublisher publisher;

  @PostMapping
  public ResponseEntity<Void> sendMessage(@RequestBody MessageRequest request) {
    publisher.publishEventAsJson(request.mapToEvent());
    return ResponseEntity.noContent().build();
  }

  @ExceptionHandler
  public ResponseEntity<Void> handleException(Exception ex) {
    log.error(ex.getMessage(), ex);
    return ResponseEntity.badRequest().build();
  }
}
