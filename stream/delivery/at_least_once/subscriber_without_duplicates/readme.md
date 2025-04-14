### Redis Stream Consumer with At-Least-Once Handling and Duplicate Protection

#### Duplicate Protection

Redis Streams offer at-least-once delivery, meaning:

* You may receive the same message more than once.

* By storing processed message IDs (like in this example),
  you ensure your handler behaves as "exactly-once" at the application level.

code:

```
@Override
public void onMessage(MapRecord<String, String, String> objectRecord) {
    // Extract the unique message ID from the stream record
    String messageId = objectRecord.getId().getValue();

    // Extract the actual message payload (key-value map)
    var event = objectRecord.getValue();

    // Construct a Redis key to mark the message as processed
    String processedKey = "processed:" + messageId;

    // Check if this message has already been processed (exists in Redis)
    Boolean alreadyProcessed = redisTemplate.hasKey(processedKey);
    if (Boolean.TRUE.equals(alreadyProcessed)) {
        // If the message is already processed, log and acknowledge it to remove from pending
        log.info("Duplicate message: {}", messageId);
        acknowledge(objectRecord);
        return;
    }

    try {
        // Business logic goes here — e.g., process the event
        log.info("Processing event: {}", event);

        // Mark the message as processed in Redis with a TTL (e.g. 5 seconds)
        // You can increase the TTL (e.g. 1 hour or 24 hours) depending on use case
        redisTemplate.opsForValue().set(processedKey, "1", Duration.ofSeconds(5));

        // Acknowledge the message to Redis (XACK) to remove it from the Pending Entries List (PEL)
        acknowledge(objectRecord);

    } catch (Exception e) {
        // If an error occurs, do NOT acknowledge the message
        // Redis will keep it in the pending list and allow reprocessing
        log.error("Error while processing message: {}", messageId, e);
    }
}

/**
 * Acknowledges the message to Redis, indicating successful processing.
 * This removes the message from the Consumer Group's Pending Entries List (PEL).
 *
 * @param objectRecord the Redis stream record to acknowledge
 */
private void acknowledge(MapRecord<String, String, String> objectRecord) {
    // Send XACK to Redis, confirming that the message has been successfully handled
    redisTemplate.opsForStream().acknowledge(CONSUMER_GROUP, objectRecord);
}
```

How it works:

```
----------------------------------------------------------------------------------------------------------------------
Step	                                            Description
----------------------------------------------------------------------------------------------------------------------
messageId = objectRecord.getId().getValue()	        Get the unique Redis Stream message ID

processedKey = "processed:" + messageId	            Generate a Redis key to track processing

hasKey(...)	                                        Check if this message has already been processed

set(..., Duration.ofSeconds(5))	                    Mark the message as processed with a short TTL 
                                                    (can be longer in production)

acknowledge(...)	                                Tell Redis that processing is complete (XACK)

No XACK	                                            If an error happens before XACK, 
                                                    Redis will keep the message in the Pending List and retry later
----------------------------------------------------------------------------------------------------------------------
```