### Project Description

This project demonstrates how to integrate Spring Boot with Redis for publishing messages using a type-safe,
secure, and customizable configuration.

It features a robust setup for serializing and deserializing complex Java event objects (MessageEvent) with Jackson,
ensuring both performance and security best practices.

The configuration is designed for use in a publisher application, enabling efficient
and safe communication with Redis streams or channels.

#### Note:

The Redis client (or a tool you use to inspect Redis) is showing you the raw bytes as Base64.

in redis:

```
Stream Data

"Y29tLm1hbGV4LnB1Ymxpc2hlci5ldmVudC5NZXNzYWdlRXZlbnQ="
"TWVzc2FnZQ=="
"ZnJvbSBtZQ=="
"MjAyNS0wNy0yNVQyMToxNjo0Mi4wODc="
"Y29tLm1hbGV4LnB1Ymxpc2hlci5ldmVudC5NZXNzYWdlRXZlbnQ="
"aGk="
"R2doaGRzZw=="
"MjAyNS0wNy0yNVQyMTowMTozOC40MjY="

Entry ID
_class
content
sender

timestamp
00:16:42 26 Jul 2025
1753478202691-0
```

#### Why is this happening?

Jackson2JsonRedisSerializer by default serializes objects to JSON,
but if you use it with the default RedisTemplate configuration,
sometimes the data may be further encoded (e.g., by the Redis client or by the way you store/retrieve data).
