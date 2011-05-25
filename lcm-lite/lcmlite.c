#include <stdint.h>
#include <string.h>
#include <stdio.h>

#include "lcmlite.h"

#define MAGIC_LCM2 0x4c433032
#define MAGIC_LCM3 0x4c433033

// header can contain a channel plus our usual header.
#define MAXIMUM_HEADER_LENGTH 300

static inline void encode_u32(uint8_t *p, uint32_t v)
{
    // big endian. p[3] gets lowest 8 bits.
    p[3] = v & 0xff;
    v >>= 8;
    p[2] = v & 0xff;
    v >>= 8;
    p[1] = v & 0xff;
    v >>= 8;
    p[0] = v & 0xff;
}

static inline uint32_t decode_u32(const uint8_t *p)
{
    uint32_t v = 0;

    // big endian. p[0] gets most significant bits.
    v |= p[0];
    v <<= 8;
    v |= p[1];
    v <<= 8;
    v |= p[2];
    v <<= 8;
    v |= p[3];

    return v;
}

static inline void encode_u16(uint8_t *p, uint16_t v)
{
    // big endian. p[1] gets lowest 8 bits.
    p[1] = v & 0xff;
    v >>= 8;
    p[0] = v & 0xff;
}

static inline uint16_t decode_u16(const uint8_t *p)
{
    uint16_t v = 0;

    // big endian. p[0] gets most significant bits.
    v |= p[0];
    v <<= 8;
    v |= p[1];

    return v;
}

// Called by lcmlite internally when a packet is decoded. (Provides a
// common delivery code path for fragmented and non-fragmented
// packets.)
static void deliver_packet(lcmlite_t *lcm, const char *channel, const void *buf, int buf_len)
{
//    printf("deliver packet, channel %-30s, size %10d\n", channel, buflen);

    for (lcmlite_subscription_t *sub = lcm->first_subscription; sub != NULL; sub = sub->next) {

        int good = 1;

        for (int pos = 0; 1; pos++) {

            // special case: does the channel have a wildcard-like expression in it?
            if (sub->channel[pos] == '.' && sub->channel[pos+1] == '*')
                break;

            if (sub->channel[pos] == channel[pos]) {
                // end of string? if so, we're done.
                if (channel[pos] == 0)
                    break;

                // proceed to the next letter
                pos++;
                continue;
            }

            // not a match.
            good = 0;
            break;
        }

        if (good)
            sub->callback(lcm, channel, buf, buf_len, sub->user);
    }
}

// The caller allocates permanent storage for LCMLite. This initializes
int lcmlite_init(lcmlite_t *lcm, void (*transmit_packet)(const void *_buf, int buf_len, void *user), void *transmit_user)
{
    memset(lcm, 0, sizeof(lcmlite_t));
    lcm->transmit_packet = transmit_packet;
    lcm->transmit_user = transmit_user;
    
    return 0;
}

/** Call this function whenever an LCM UDP packet is
 * received. Registered LCM handlers will be called
 * synchronously. When the function returns, the buffer can be safely
 * reused. Returns non-zero if the packet was not decoded properly,
 * but no special action is required by the caller.
 *
 * from_addr is opaque, but should uniquely identify the sender's IP
 * address and port.
 **/
int lcmlite_receive_packet(lcmlite_t *lcm, const void *_buf, int buf_len, uint64_t from_addr)
{
    uint8_t *buf = (uint8_t*) _buf;
    int buf_pos = 0;

    // not even a header's length
    if (buf_len < 4)
        return -1;

    uint32_t magic = decode_u32(&buf[buf_pos]);  buf_pos += 4;

    if (magic == MAGIC_LCM2) {

        uint32_t msg_seq = decode_u32(&buf[buf_pos]);  buf_pos += 4;
        (void) msg_seq; // quiet unused variable warning.
        
        // copy out zero-terminated string holding the channel #.
        char channel[LCM_MAX_CHANNEL_LENGTH];
        int channel_len = 0;

        while (buf[buf_pos] != 0) {
            // malformed packet.
            if (buf_pos >= buf_len || channel_len >= LCM_MAX_CHANNEL_LENGTH)
                return -2;

            channel[channel_len++] = buf[buf_pos++];
        }
        channel[channel_len] = 0;
        buf_pos++; // skip the zero.

        deliver_packet(lcm, channel, &buf[buf_pos], buf_len - buf_pos);

    } else if (magic == MAGIC_LCM3) {

        if (LCM3_NUM_BUFFERS == 0)
            return -3;

        uint32_t msg_seq = decode_u32(&buf[buf_pos]);           buf_pos += 4;
        uint32_t msg_size = decode_u32(&buf[buf_pos]);          buf_pos += 4;
        uint32_t fragment_offset = decode_u32(&buf[buf_pos]);   buf_pos += 4;
        uint32_t fragment_id = decode_u16(&buf[buf_pos]);       buf_pos += 2;
        uint32_t fragments_in_msg = decode_u16(&buf[buf_pos]);  buf_pos += 2;

        int payload_len = buf_len - buf_pos;

        // printf("%08x:%08x %d / %d\n", from_addr, msg_seq, fragment_id, fragments_in_msg);

        // validate packet metadata
        if (msg_size > LCM3_MAX_PACKET_SIZE)
            return -4;

        if (fragments_in_msg > LCM3_MAX_FRAGMENTS)
            return -5;

        if (fragment_id >= fragments_in_msg || fragment_offset + payload_len > msg_size)
            return -6;

        // find the fragment. Use a simple linear search; this is
        // cheap in comparison to how much work we're spending to
        // decode the large packet...
        struct fragment_buffer *fbuf = NULL;

        // try to find a reassembly buffer for this from_addr that's already in progress
        for (int idx = 0; idx < LCM3_NUM_BUFFERS; idx++) {

            if (lcm->fragment_buffers[idx].from_addr == from_addr &&
                lcm->fragment_buffers[idx].msg_seq == msg_seq) {
                fbuf = &lcm->fragment_buffers[idx];
                break;
            }
        }

        if (fbuf == NULL) {
            // didn't find one. Pick a new buffer to use.

            // Priorities:
            //   1) an idle (complete) buffer
            //   2) the incomplete buffer that received a valid fragment the longest time ago.
            int32_t max_age = -1; // low scores are good.
            for (int idx = 0; idx < LCM3_NUM_BUFFERS; idx++) {
                if (lcm->fragment_buffers[idx].fragments_remaining == 0) {
                    fbuf = &lcm->fragment_buffers[idx];
                    break;
                } else {
                    int32_t age = lcm->last_fragment_count - lcm->fragment_buffers[idx].last_fragment_count;
                    if (age > max_age) {
                        fbuf = &lcm->fragment_buffers[idx];
                        max_age = age;
                    }
                }
            }

            if (fbuf == NULL)
                return -7; // this should never happen

            // initialize the fragment buffer
            for (int i = 0; i < fragments_in_msg; i++) {
                fbuf->frag_received[i] = 0;
            }

            fbuf->from_addr = from_addr;
            fbuf->msg_seq = msg_seq;
            fbuf->fragments_remaining = fragments_in_msg;
        }

        // now, handle this fragment
        fbuf->last_fragment_count = lcm->last_fragment_count;
        lcm->last_fragment_count++;

        if (fragment_id == 0) {
            // this fragment contains the channel name plus data
            int channel_len = 0;
            while (buf[buf_pos] != 0) {
                if (buf_pos >= buf_len || channel_len >= LCM_MAX_CHANNEL_LENGTH)
                    return -8;
                fbuf->channel[channel_len++] = buf[buf_pos++];
            }
            fbuf->channel[channel_len] = 0;
            buf_pos++; // skip the zero.
        }

        if (buf_pos < buf_len)
            memcpy(&fbuf->buf[fragment_offset], &buf[buf_pos], buf_len - buf_pos);

        // record reception of this packet
        if (fbuf->frag_received[fragment_id] == 0) {
            fbuf->frag_received[fragment_id] = 1;
            fbuf->fragments_remaining--;

            if (fbuf->fragments_remaining == 0) {
                deliver_packet(lcm, fbuf->channel, fbuf->buf, msg_size);
            }
        }
    }

    return 0;
}

void lcmlite_subscribe(lcmlite_t *lcm, lcmlite_subscription_t *sub)
{
    sub->next = lcm->first_subscription;
    lcm->first_subscription = sub;
}

int lcmlite_publish(lcmlite_t *lcm, const char *channel, const void *_buf, int buf_len)
{
    if (buf_len < LCM_PUBLISH_BUFFER_SIZE - MAXIMUM_HEADER_LENGTH) {
        // publish non-fragmented message
        uint32_t buf_pos = 0;

        encode_u32(&lcm->publish_buffer[buf_pos], MAGIC_LCM2);      buf_pos += 4;
        encode_u32(&lcm->publish_buffer[buf_pos], lcm->msg_seq);    buf_pos += 4;
        lcm->msg_seq++;

        // copy channel
        while (*channel != 0) {
            lcm->publish_buffer[buf_pos++] = *channel;
            channel++;
        }
        lcm->publish_buffer[buf_pos++] =0 ;

        memcpy(&lcm->publish_buffer[buf_pos], _buf, buf_len);       buf_pos += buf_len;

        lcm->transmit_packet(lcm->publish_buffer, buf_pos, lcm->transmit_user);

        return 0;
    } else {
        // send fragmented message
        uint32_t msg_seq = lcm->msg_seq;
        lcm->msg_seq++;

        uint32_t fragment_offset = 0;
        uint32_t max_fragment_size = LCM_PUBLISH_BUFFER_SIZE - MAXIMUM_HEADER_LENGTH;
        uint32_t fragment_id = 0;
        uint32_t fragments_in_msg = (buf_len + max_fragment_size - 1) / max_fragment_size;

        while (fragment_offset < buf_len) {
            uint32_t buf_pos = 0;

            encode_u32(&lcm->publish_buffer[buf_pos], MAGIC_LCM3);      buf_pos += 4;
            encode_u32(&lcm->publish_buffer[buf_pos], msg_seq);         buf_pos += 4;
            encode_u32(&lcm->publish_buffer[buf_pos], buf_len);         buf_pos += 4;
            encode_u32(&lcm->publish_buffer[buf_pos], fragment_offset); buf_pos += 4;
            encode_u16(&lcm->publish_buffer[buf_pos], fragment_id);     buf_pos += 2;
            encode_u16(&lcm->publish_buffer[buf_pos], fragments_in_msg);     buf_pos += 2;

            // copy channel
            if (fragment_id == 0) {
                while (*channel != 0) {
                    lcm->publish_buffer[buf_pos++] = *channel;
                    channel++;
                }
                lcm->publish_buffer[buf_pos++] =0 ;
            }

            uint32_t this_fragment_size = buf_len - fragment_offset;
            if (this_fragment_size > max_fragment_size)
                this_fragment_size = max_fragment_size;

            memcpy(&lcm->publish_buffer[buf_pos], &((char*) _buf)[fragment_offset], this_fragment_size);
            buf_pos += this_fragment_size;

            lcm->transmit_packet(lcm->publish_buffer, buf_pos, lcm->transmit_user);

            fragment_offset += this_fragment_size;
            fragment_id++;
        }
    }
    
    return 0;
}
