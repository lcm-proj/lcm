/**
 * Lightweight Communications and Marshalling library.
 */

[CCode (cheader_filename = "lcm/lcm.h")]
namespace Lcm {
    /**
     * Core communication class.
     */
    [CCode (cname = "lcm_t", free_function = "lcm_destroy", cprefix = "lcm_")]
    [Compact]
    public class Lcm {
        /**
         * Core communication class constructor.
         *
         * @param provider Initializationg string specifying the LCM network provider.
         *                 If this is null, and the environment variable "LCM_DEFAULT_URL" is defined,
         *                 then the environment variable is used instead.
         *                 If this is null and the environment variable is not defined, then default settings are used.
         */
        [CCode (cname = "lcm_create")]
        public Lcm(string? provider = null);

        /**
         * Returns a file descriptor or socket that can be used with
         * select(), poll(), or other event loops for asynchronous
         * notification of incoming messages.
         *
         * @return File descriptor.
         */
        public int get_fileno();

        /**
         * Subscribe a callback function to a channel.
         *
         * The callback function will be invoked during calls to Lcm.handle() any time
         * a message on the specified channel is received.  Multiple callbacks can be
         * subscribed for the same channel.
         *
         * @param channel   the channel to listen on.  This can also be a GLib regular
         *                  expression, and is treated as a regex implicitly surrounded
         *                  by '^' and '$'
         * @param handler   the callback function to be invoked when a message is
         *                  received on the specified channel
         * @param user_data this will be passed to the callback function
         *
         * @return a Subscription to identify the new subscription,
         *         which can be passed to Lcm.unsubscribe().
         *         The LCM instance owns the subscription object.
         */
        public unowned Subscription subscribe(string channel, Subscription.Handler handler, void *user_data = null);

        /**
         * Unsubscribe a message handler.
         *
         * The callback function for the subscription will no longer be
         * invoked when messages on the corresponding channel are received.  After this
         * function returns, handler is no longer valid and should not be used
         * anymore.
         *
         * @param handler subsription object to destruct.
         * @return 0 on success, or -1 if handler is not a valid subscription.
         */
        public int unsubscribe(Subscription handler);

        /**
         * Publish a message, specified as a raw byte buffer.
         *
         * @param channel  The channel to publish on
         * @param data     The raw byte buffer
         *
         * @return 0 on success, -1 on failure.
         */
        public int publish(string channel, void[] data);

        /**
         * Wait for and dispatch the next incoming message.
         *
         * Message handlers are invoked one at a time from the thread that calls this
         * function, and in the order that they were subscribed.
         *
         * This function waits indefinitely.  If you want timeout behavior, (e.g., wait
         * 100ms for a message) then consider using Lcm.get_fileno() together with
         * select() or poll()
         *
         * Recursive calls to Lcm.handle() are not allowed -- do not call Lcm.handle() from
         * within a message handler.  All other functions are okay (e.g., it is okay to
         * call Lcm.publish() from within a message handler).
         *
         * @return 0 normally, or -1 when an error has occurred.
         */
        public int handle();

        /**
         * Wait for and dispatch the next incoming message, up to a time limit.
         *
         * This function is equivalent to Lcm.handle(), but if no messages are received
         * and handled by the time @p timeout_millis milliseconds elapses, then the
         * function returns.
         *
         * This function largely exists for convenience, and its behavior can be
         * replicated by using Lcm.get_fileno() and Lcm.handle() in conjunction with
         * select() or poll().
         *
         * @param timeout_millis the maximum amount of time to wait for a message, in
         *        milliseconds.  If 0, then dispatches any available messages and then
         *        returns immediately.  Values less than 0 are not allowed.
         *
         * @return >0 if a message was handled, 0 if the function timed out, and <0 if
         *         an error occured.
         */
        public int handle_timeout(int timeout_millis);
    }

    /**
     * An opaque data structure that identifies an LCM subscription
     *
     * Owned by Lcm object.
     */
    [CCode (cname = "lcm_subscription_t", cprefix = "lcm_subscription_")]
    [Compact]
    public class Subscription {
        /**
         * Adjusts the maximum number of received messages that can be queued up
         * for a subscription.
         *
         * Setting this to a low number may reduce overall latency at the expense of
         * dropping more messages.  Conversely, setting this to a high number may drop
         * fewer messages at the expense of increased latency.  A value of 0 indicates
         * no limit, and should be used carefully.
         *
         * @param num_messages the maximum queue size, in messages.  The default is 30.
         */
        public int set_queue_capacity(int num_messages);

        /**
         * Callback delegate type.
         *
         * Pass instances of this to Lcm.subscribe()
         *
         * TODO: Find how to adjust rbuf to be const ptr, channel to be const char ptr.
         * Now gcc produce warnings.
         *
         * @param rbuf the message timestamp and payload
         * @param channel the channel the message was received on
         * @param user_data the user-specified parameter passed to LCM.subscribe()
         */
        [CCode (cname = "lcm_msg_handler_t", has_target = false)]
        public delegate void Handler(RecvBuf rbuf, string channel, void *user_data);
    }

    /**
     * Received messages are passed to user programs using this data structure.
     * Each instance represents one message.
     */
    [CCode (cname = "lcm_recv_buf_t")]
    public struct RecvBuf {
        //! the data received (raw bytes)
        [CCode (array_length_cname = "data_size", array_length_type = "uint32_t")]
        void[] data;
        //! timestamp (micrseconds since the epoch) at which the first data bytes of the message were received.
        int64 recv_utime;
        //! LCM that owns this buffer
        unowned Lcm lcm;
    }

    /**
     * Library version
     */
    [CCode (cname = "int", has_type_id = false)]
    public enum Version {
        [CCode (cname = "LCM_MAJOR_VERSION")]
        MAJOR,
        [CCode (cname = "LCM_MINOR_VERSION")]
        MINOR,
        [CCode (cname = "LCM_MICRO_VERSION")]
        MICRO
    }

    /* -*- lcm/eventlog.h -*- */

    /* TODO: test event log and write doc strings */

    [CCode (cname = "lcm_eventlog_t", free_function = "lcm_eventlog_destroy", cprefix = "lcm_eventlog_")]
    [Compact]
    public class EventLog {
        [CCode (cname = "lcm_eventlog_create")]
        public EventLog(string path, string mode);

        public Event read_next_event();

        public int seek_to_timestamp(int64 ts);

        public int write_event(Event event);

        [CCode (cname = "lcm_eventlog_event_t", destroy_function = "lcm_eventlog_free_event")]
        public struct Event {
            int64 eventnum;
            int64 timestamp;
            [CCode (array_length_cname = "channellen", array_length_type = "int32_t")]
            uint8[] channel;
            [CCode (array_length_cname = "datalen", array_length_type = "int32_t")]
            void[] data;
        }
    }

    /* -*- other types -*- */

    /**
     * intptr_t for use in hash calculation.
     * Copy from dova-core integer.vapi
     */
    [CCode (cname = "intptr_t")]
    [IntegerType (rank = 8)]
    public struct CoreTypes.intptr {
        public string to_string () {
            long l = (long) this;
            return l.to_string ();
        }
    }
}
