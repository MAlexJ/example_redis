package com.malex.controller;

import com.malex.sse.SseEmitterPool;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class SseController {

  private final SseEmitterPool emitterPool;

  @GetMapping("/sse/subscribe")
  public SseEmitter subscribe() {
    return emitterPool.addEmitter();
  }
}
