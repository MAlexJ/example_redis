### Disable null value

1. @Cacheable(..., unless = "#result == null")
   Where it works: at the annotation level (Spring Cache abstraction)
   What it does: prevents caching of a method result if the result is null.
   Why it matters: even if disableCachingNullValues() is configured, Spring might still try to put null in the cache,
   which could cause exceptions or unexpected behavior.


2. RedisCacheConfiguration.disableCachingNullValues()
   Where it works: in the RedisCacheConfiguration, managed by RedisCacheManager.
   What it does: blocks any attempt to store null in Redis.
   If null does reach this point, an IllegalArgumentException will be thrown.

```
Behavior	                        Only unless	         Only disableCachingNullValues()	Both
Prevents caching of null results	✅	                 ✅ (but may throw)	                 ✅
Avoids serialization issues	        ❌	                 ✅	                                 ✅
Guarantees no null in Redis	        ⚠️	                 ✅	                                 ✅
No exceptions on null values	    ✅	                 ❌ (could throw exception)     	 ✅
```

Best Practice

Always use both:

```
@Cacheable(cacheNames = TASK_CACHE, key = TASK_CACHE_KEY, unless = "#result == null")
```

And in your configuration:

```
RedisCacheConfiguration.defaultCacheConfig()
...
.disableCachingNullValues()
```
