### Poll timeout configuration

#### pollTimeout(Duration.ofSeconds(1))

* This sets the polling/block timeout for Redis using the BLOCK option in the XREADGROUP command.
* In this case, the container waits up to 1 second for new messages before polling again.
* This reduces CPU usage vs. constant polling and helps deliver messages quickly when they arrive.
* Equivalent to: BLOCK 1000 in raw Redis CLI.

```
@Bean
public StreamMessageListenerContainer<String, ObjectRecord<String, String>>
    messageListenerContainer(RedisConnectionFactory redisConnectionFactory) {

  // Configure a poll timeout for the BLOCK option during reading.
  var options =
      StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
          .pollTimeout(Duration.ofSeconds(1)) // <- Set the maximum time to wait for new messages (BLOCK 1000 ms)
          .targetType(String.class)           // <- Define the expected data type of the stream record body
          .build();

  return StreamMessageListenerContainer.create(redisConnectionFactory, options); // Create the listener container with the specified options
}
```

#### Pre configuration

1. create consumer group: `message-group`

2. read stream: `message-stream`

