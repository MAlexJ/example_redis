### Compute Prefix With

Spring's RedisCache class has an internal method called computePrefixWith that adds a prefix
to every cache entry to prevent key collisions and help organize keys.

By default, it prefixes keys like:

`<cacheName>::<key>`

Example:

For `@Cacheable(cacheNames = "task_cache", key = "'task'")`, it stores in Redis:

`task_cache::task`

This is the result of Spring's internal computePrefixWith().

How to customize key prefix in application.yml

```
spring:
    cache:
        redis:
            key-prefix: myapp::
            use-key-prefix: true
```

Then your keys will look like:

        ```myapp::task_cache::task```

You can also disable the prefix altogether:

```
spring:
    cache:
        redis:
            use-key-prefix: false 
```

If you want even more control (e.g. dynamic prefixes):

You can define a custom RedisCacheManager:

```
@Configuration
public class CacheConfig {

@Bean
public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
        .prefixCacheNameWith("myapp::")
        .entryTtl(Duration.ofMinutes(10));

    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(config)
        .build();
    }
}
```

This lets you control TTL, serialization, prefixing, etc.