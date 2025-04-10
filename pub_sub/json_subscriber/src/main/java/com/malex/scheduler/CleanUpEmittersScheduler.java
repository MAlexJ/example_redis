package com.malex.scheduler;

import com.malex.sse.SseEmitterPool;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CleanUpEmittersScheduler {

  private final SseEmitterPool emitterPool;

  /*
   * Run clean up every 3- second
   */
  @Scheduled(fixedRate = 30000)
  public void cleanUpEmitters() {
    emitterPool.cleanUp();
  }
}
