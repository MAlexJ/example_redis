### Redis Streams

Introduction to Redis streams

link: https://redis.io/docs/latest/develop/data-types/streams/

A Redis stream is a data structure that acts like an append-only log but also implements several operations
to overcome some limits of a typical append-only log.

These include random access in O(1) time and complex consumption strategies, such as consumer groups.
You can use streams to record and simultaneously syndicate events in real time.
Examples of Redis stream use cases include:

* Event sourcing (e.g., tracking user actions, clicks, etc.)
* Sensor monitoring (e.g., readings from devices in the field)
* Notifications (e.g., storing a record of each user's notifications in a separate stream)

Redis generates a unique ID for each stream entry.
You can use these IDs to retrieve their associated entries later or to read
and process all later entries in the stream.
Note that because these IDs are related to time, the ones shown here may vary and will be different
from the IDs you see in your own Redis instance.

Redis streams support several trimming strategies (to prevent streams from growing unbounded)
and more than one consumption strategy (see XREAD, XREADGROUP, and XRANGE).

#### Basic commands

* XADD - adds a new entry to a stream.
* XREAD - reads one or more entries, starting at a given position and moving forward in time.
* XRANGE - returns a range of entries between two supplied entry IDs.
* XLEN - returns the length of a stream.
