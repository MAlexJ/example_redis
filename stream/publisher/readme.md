### Publisher

#### Want to read it manually from Redis CLI?

```
XRANGE message-stream-json - +
```

This reads all messages from the beginning to the end of the stream.

If you want only the latest N messages:

```
XREVRANGE message-stream-json + - COUNT 3
```

Summary:

1. RedisInsight Go to "Streams" → message-stream-json → View stream entries
2. Redis CLI XRANGE message-stream-json - + → to read all
3. XREVRANGE message-stream-json + - COUNT 3 → read latest 3