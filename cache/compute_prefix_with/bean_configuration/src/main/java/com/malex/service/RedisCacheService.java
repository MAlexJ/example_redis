package com.malex.service;

import static com.malex.confiuration.RedisConfiguration.TASK_CACHE;
import static com.malex.confiuration.RedisConfiguration.TASK_CACHE_KEY;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheService {

  private final CopyOnWriteArraySet<Task> set = new CopyOnWriteArraySet<>();

  @Cacheable(cacheNames = TASK_CACHE, key = TASK_CACHE_KEY)
  public Set<Task> getAllTasks() {
    try {
      TimeUnit.SECONDS.sleep(2);
    } catch (InterruptedException e) {
      log.error("Error while sleeping", e);
      Thread.currentThread().interrupt();
    }
    return set;
  }

  /*
   * Problem: Cache stores different value types
   *
   * You’ve got a mismatch:
   *
   * - getAllTasks() is caching and returning a Set<Task>
   * - addTask() is updating the cache with a Task
   *
   * That means:
   *
   * After calling addTask(), the cache now contains a Task, not a Set<Task>
   * Next time getAllTasks() hits Redis, you’ll get a ClassCastException or unexpected behavior
   *
   * Recommended Fix
   * Return the full updated Set<Task> so the cache remains consistent:
   *
   */
  @CachePut(cacheNames = TASK_CACHE, key = TASK_CACHE_KEY)
  public Set<Task> addTask(Task task) {
    set.add(task);

    /*
     * This way:
     * The value under Redis key task_cache::task always contains a Set<Task>
     * It stays compatible with getAllTasks() and refreshCheatersCache()
     */
    return set;
  }

  @CacheEvict(cacheNames = TASK_CACHE, key = TASK_CACHE_KEY)
  public void removeAllTasks() {
    // Removes all of the elements from this set
    set.clear();
  }

  @Scheduled(cron = "${caching.scheduled.tasks.cache-eviction-cron}")
  @CachePut(cacheNames = TASK_CACHE, key = TASK_CACHE_KEY)
  public Set<Task> refreshCheatersCache() {
    log.debug(">>> Refreshing cheaters cache...");
    return set;
  }
}
