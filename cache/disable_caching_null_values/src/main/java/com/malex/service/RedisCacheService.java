package com.malex.service;

import static com.malex.configuration.RedisConfiguration.TASK_CACHE;
import static com.malex.configuration.RedisConfiguration.TASK_CACHE_KEY;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RedisCacheService {

  private final CopyOnWriteArrayList<Task> tasks = new CopyOnWriteArrayList<>();

  @Cacheable(cacheNames = TASK_CACHE, key = TASK_CACHE_KEY)
  public List<Task> getAllTasks() {
    return tasks;
  }

  public List<Task> addTask(Task task) {
    tasks.add(task);
    return tasks;
  }

  @CacheEvict(cacheNames = TASK_CACHE, key = TASK_CACHE_KEY)
  public void removeAllTasks() {
    // Removes all of the elements from this set
    tasks.clear();
  }

  @Scheduled(cron = "${caching.scheduled.tasks.cache-eviction-cron}")
  @CachePut(cacheNames = TASK_CACHE, key = TASK_CACHE_KEY)
  public List<Task> refreshCheatersCache() {
    log.debug(">>> Refreshing cheaters cache...");
    return tasks;
  }
}
