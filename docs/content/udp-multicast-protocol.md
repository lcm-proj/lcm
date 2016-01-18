LCM UDP Multicast Protocol Description {#udp_multicast_protocol}
====

Conceptually, an LCM message consists of a channel name and a payload.  The
channel name is a string identifying the semantic grouping of the message (e.g.
"ODOMETRY").  The payload is a binary blob of data that corresponds to the
contents of the message.  Usually, the payload is the binary encoding of a
marshalled LCM type.

Each LCM message is transmitted using one or more UDP datagrams to a UDP
multicast group and port.  The address and port must have been agreed on
beforehand -- LCM does not have an automatic discovery mechanism.  To publish a
message on a channel, an LCM client packages it up as described in the next two
sections, and then transmits it directly to the multicast group.  If the size
of the LCM message is small, then it can be transmitted using a single UDP
datagram.  Otherwise, the message must be fragmented and transmitted in several
parts.

To subscribe to a messaging channel, an LCM client joins the multicast group
and inspects each message transmitted to the multicast group, retaining
messages of interest and discarding the remainder.

# Small messages

A small message is defined as one where the LCM small message header, channel
name, and the payload can fit into a single UDP datagram.  Small messages have
an upper size limit of 64 kB, after which they must be fragmented.

The header for a small message is 8 bytes long and has the following form:

     0      7 8     15 16    23 24    31 
    +--------+--------+--------+--------+
    | short_header_magic                |
    +--------+--------+--------+--------+
    | sequence_number                   |
    +--------+--------+--------+--------+

`short_header_magic` is an unsigned 32-bit integer with value  `0x4c433032`

`sequence_number` is a monotonically increasing (subject to integer wraparound) unsigned 32-bit number identifying the message.

Both values are encoded in network byte order (Big-Endian).

The header is followed by the null-terminated UTF-8 encoding of the channel name.

The channel name is followed by the payload.

# Fragmented messages

Large messages can be split into multiple fragments for transmission.  Each
fragment is transmitted in a single UDP datagram.  The very first fragment
consists of a header, followed by the channel name, followed by the first
payload bytes.  Subsequent fragments consist of a header followed by payload
data.

The header for each fragment is 20 bytes long and has the following form:

     0      7 8     15 16    23 24    31 
    +--------+--------+--------+--------+
    | fragment_header_magic             |
    +--------+--------+--------+--------+
    | sequence_number                   |
    +--------+--------+--------+--------+
    | payload_size                      |
    +--------+--------+--------+--------+
    | fragment_offset                   |
    +--------+--------+--------+--------+
    | fragment_number | n_fragments     |
    +--------+--------+--------+--------+

`fragment_header_magic` is an unsigned 32-bit integer with value `0x4c433033`

`sequence_number` is a monotonically increasing (subject to integer wraparound)
unsigned 32-bit integer identifying the message.  The same counter should be
used for fragments and for short messages, and all fragments for a single LCM
message should have the same sequence number.

`payload_size` is an unsigned 32-bit integer indicating the length of the
payload, in bytes.  This does not include the length of the channel name.

`fragment_offset` is an unsigned 32-bit integer indicating the payload byte
offset of the fragment data within the message payload.

`fragment_number` is an unsigned 16-bit integer with value 0 for the first
fragment, 1 for the second, and so on.

`n_fragments` is an unsigned 16-bit integer with value corresponding to the
total number of fragments in the message.

All header fields are packed in network byte order (Big-Endian).

The null-terminated UTF-8 encoding of the channel immediately follows the
header for the very first fragment of a message.  This is followed by the first
bytes of the message payload.  In subsequent fragments, the payload data
immediately follows the fragment header.
