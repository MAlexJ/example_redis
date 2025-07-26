# Publisher JSON

A Spring Boot application for publishing JSON-formatted events to a Redis stream.
It provides both a REST API and a simple web UI for sending messages,
which are then published to a configured Redis stream.

## Features

- Publishes messages as JSON to a Redis stream
- REST API for programmatic access
- Minimal web UI for manual message sending
- Configurable Redis and stream properties

## Quick Start

1. **Configure Redis Connection**

   Edit `src/main/resources/application.yaml` or set environment variables to configure your Redis connection:

   ```yaml
   spring:
     data:
       redis:
         host: ${REDIS_HOST:localhost}
         port: ${REDIS_PORT:6379}
         username: ${REDIS_USERNAME:}
         password: ${REDIS_PASSWORD:}
   
   redis:
     stream:
       name: message-stream-json   # Name of the Redis stream
       key: message                # Field key for the message
       max-length: 3               # Max number of messages to keep in the stream
   ```

   You can override these values using environment variables (e.g., `REDIS_HOST`, `REDIS_PORT`, etc.).

2. **Run the Application**

   ```bash
   ./gradlew bootRun
   ```
   The app will start on port 8080 by default.

## Sending Events

### REST API

- **Endpoint:** `POST /v1/events`
- **Request Body:**
  ```json
  {
    "title": "Your message title",
    "content": "Your message content"
  }
  ```
- **Response:** `204 No Content` on success

### Web UI

- Open [http://localhost:8080](http://localhost:8080) in your browser.
- Fill in the title and content, then click "Send Message".

## Message Format

Each message is published to the Redis stream as a JSON object with the following structure:

```json
{
  "title": "...",
  "content": "...",
  "timestamp": "2024-01-01T12:00:00"
}
```

## Project Structure

- `src/main/java/com/malex/publisher/StreamPublisher.java` – Publishes events to Redis stream
- `src/main/java/com/malex/rest/MessageController.java` – REST API controller
- `src/main/resources/application.yaml` – Main configuration file
- `src/main/resources/static/index.html` – Web UI

## Configuration Beans

The application uses several important configuration beans to ensure correct serialization and communication with Redis:

- **`StreamProperties`**
    - Binds properties with the prefix `redis.stream` from `application.yaml` (or environment variables).
    - Holds the stream name, key, and max length for Redis stream trimming.
    - Example:
      ```yaml
      redis:
        stream:
          name: message-stream-json
          key: message
          max-length: 3
      ```

- **`SerializerConfiguration`**
    - Provides a `GenericJackson2JsonRedisSerializer<Objects>` bean for serializing and deserializing messages as JSON.
    - Configure the serializer to:
        - Make all fields (including private) visible for serialization.
        - Restrict deserialization to classes in the `com.malex.producer` package for security.
        - Handle Java 8+ date/time types (e.g., `LocalDateTime`) as ISO-8601 strings.
        - Avoid writing dates as numeric timestamps.

- **`TemplateConfiguration`**
    - Provides a `RedisTemplate<String, MessageEvent>` bean for interacting with Redis streams.
    - Configure the template to use string keys and JSON-serialized values (using the above serializer).
    - Ensures both key/value and hash key/value pairs are correctly serialized for Redis operations.

These beans ensure that messages are safely and correctly serialized to JSON
and published to the configured Redis stream.

## Requirements

- Java 24+
- Redis or Redis cloud service
