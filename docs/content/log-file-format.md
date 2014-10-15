LCM Log File format {#log_file_format}
====

Conceptually, an LCM Log file is an ordered list of events.  Each event has
four fields:

Field Name     | Field Description
---------------|------------------
event number | monotonically increasing 64-bit integer that identifies each event.  It should start at zero, and increase in increments of one.
timestamp    | monotonically increasing 64-bit integer that identifies the number of microseconds since the epoch (00:00:00 UTC on January 1, 1970) at which the event was received.
channel      | UTF-8 string identifying the LCM channel on which the message was received.
data         | binary blob consisting of the exact message received.

# Event Encoding

Each event is encoded as a binary structure consisting of a header, followed by the channel and the data.

The header is 28 bytes and has the following format:

     0      7 8     15 16    23 24    31
    +--------+--------+--------+--------+
    |   LCM Sync Word                   |
    +--------+--------+--------+--------+
    |   Event Number (upper 32 bits)    |
    +--------+--------+--------+--------+
    |   Event Number (lower 32 bits)    |
    +--------+--------+--------+--------+
    |   Timestamp (upper 32 bits)       |
    +--------+--------+--------+--------+
    |   Timestamp (lower 32 bits)       |
    +--------+--------+--------+--------+
    |   Channel Length                  |
    +--------+--------+--------+--------+
    |   Data Length                     |
    +--------+--------+--------+--------+

`LCM Sync Word` is an unsigned 32-bit integer with value `0xEDA1DA01`

`Event Number` and `Timestamp` fields of the header are as described above.

`Channel Length` is an unsigned 32-bit integer describing the length of the
channel name.

`Data Length` is an unsigned 32-bit integer describing the length of the
data.

Each header is immediately followed by the UTF-8 encoding of the LCM channel,
and then the message data.  The channel is not NULL-terminated.

All integers are packed in network order (big endian)
