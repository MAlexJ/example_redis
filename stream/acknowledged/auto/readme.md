### Acknowledgment Options in Spring Boot (Spring Data Redis Streams)

When you're using Spring Boot 3+ with Spring Data Redis Streams,
you have exactly two acknowledgment options:

1. Automatic Acknowledgment

   Spring automatically calls **XACK** as soon as the message is delivered to your StreamListener.

   You don’t need to call acknowledge() yourself.

    * Risky: If your processing fails or throws an exception, Redis will still consider the message as processed
      and you will lose it.

    * Good for very fast or non-critical processing (like logging or fire-and-forget).

```
StreamReadRequest.builder(streamOffset).consumer(consumer).autoAcknowledge(true)
```

2. Manual Acknowledgment

   Spring won’t call XACK automatically.

   You manually confirm the message with:

   `redisTemplate.opsForStream().acknowledge(group, record);`

    * Safe and recommended when processing must be reliable.
    * Supports retry and deduplication patterns.

```
StreamReadRequest.builder(streamOffset).consumer(consumer).autoAcknowledge(false)
```

📌 Summary: Acknowledge Types in Spring Boot 3+ with Redis Streams

Acknowledgment Type Supported? Description
autoAcknowledge(true)    ✅ Yes Spring calls XACK immediately after message delivery. Risky if processing fails.
autoAcknowledge(false) + manual XACK ✅ Yes You manually call acknowledge() after successful processing. Safer.
Automatic retry / claim ❌ Not built-in You have to implement with XPENDING, XCLAIM, etc.
Idempotent / exactly-once style 🟢 Yes (custom)    Implement your own deduplication via Redis keys.