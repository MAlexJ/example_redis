### Redis Streams — Delivery Semantics

Redis Streams support at-least-once delivery semantics,
but with proper patterns and tradeoffs,
you can simulate exactly-once (to some extent) or at-most-once using your own logic.

```
----------------------------------------------------------------------------------------------------------------------
Type	        Supported by Redis Streams?	                    Notes
----------------------------------------------------------------------------------------------------------------------
At-most-once	❌ Not natively	                Would require discarding messages on failure - not typical

At-least-once	✅ Yes (default)	            Each message is delivered at least once, 
                                                might be redelivered if not acknowledged

Exactly-once	❌ Not natively	                You must build it manually using idempotent processing
----------------------------------------------------------------------------------------------------------------------
```

#### What Redis Streams actually provide

1. Reliable storage:

* Messages persist in the stream until explicitly deleted (not auto-deleted after reading).
* Stream supports message IDs, so you can resume from where you left off.

2. Consumer groups:

* Messages are distributed to one consumer per group.
* Redis tracks unacknowledged messages in the Pending Entries List (PEL).
* You must call XACK to acknowledge processing.