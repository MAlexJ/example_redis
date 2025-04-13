### Offset Latest for GROUP

#### issue

```
StreamOffset.create(MESSAGE_STREAM_JSON, ReadOffset.latest()),
```

* Error Explanation

This means that ReadOffset.latest() (which maps to $) is not allowed when using XREADGROUP.

```
Caused by: io.lettuce.core.RedisCommandExecutionException: ERR The $ ID is meaningless in the context of XREADGROUP: you want to read the history of this consumer by specifying a proper ID, or use the > ID to get new messages. The $ ID would just return an empty result set.
	at io.lettuce.core.internal.ExceptionFactory.createExecutionException(ExceptionFactory.java:151) ~[lettuce-core-6.4.2.RELEASE.jar:6.4.2.RELEASE/f4dfb40]
	at io.lettuce.core.internal.ExceptionFactory.createExecutionException(ExceptionFactory.java:120) ~[lettuce-core-6.4.2.RELEASE.jar:6.4.2.RELEASE/f4dfb40]

```

##### Why This Happens

ReadOffset.latest() (i.e. $) is valid only when you use XREAD, not in a consumer group (XREADGROUP).
For consumer groups, > must be used to read new messages that haven’t been delivered to any consumer yet.

#### The Fix: Use ReadOffset.from(">")

Update your code like this:

```
@Bean
public Subscription subscription(
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer,
        StreamListener<String, MapRecord<String, String, String>> messageListener) {

    listenerContainer.start();

    return listenerContainer.receiveAutoAck(
            Consumer.from("message-group", "consumer-" + UUID.randomUUID()),
            StreamOffset.create("message-stream-json", ReadOffset.from(">")), // ✅ Use ">" instead of latest()
            messageListener
    );
}
```

Summary: Offsets in Redis Streams

------------------------------------------------------------------------------------------------------------
Offset	                    Description	                                         When to use
------------------------------------------------------------------------------------------------------------
ReadOffset.from("0")        Read everything from the beginning For replaying or debugging
ReadOffset.lastConsumed()    Resume from last acknowledged message (in group)    Standard for consumer groups
ReadOffset.latest()            ❌ Invalid in XREADGROUP, causes the error Only valid in basic XREAD
ReadOffset.from(">")        ✅ Read only new messages for consumer group Correct for real-time processing
