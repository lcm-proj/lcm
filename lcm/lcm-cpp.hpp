#ifndef __lcm_cpp_hpp__
#define __lcm_cpp_hpp__

/**
 * C++ wrappers for liblcm.  Primarily intended for use with the C++ message
 * bindings.
 *
 * TODO usage documentation
 */

#include <string>
#include <vector>
#include <lcm/lcm.h>

namespace lcm {

class Subscription;

/**
 * Stores the raw bytes and timestamp of a received message.
 */
struct ReceiveBuffer {
    void *data;
    uint32_t data_size;
    int64_t recv_utime;
};

template<class MessageType>
class MessageHandler {
    public:
        virtual void handleMessage(const ReceiveBuffer* rbuf,
                std::string channel, const MessageType* msg) = 0;
        virtual ~MessageHandler() {}
};

class LCM {
    public:
        inline LCM();

        inline ~LCM();

        /**
         * Initializes the LCM instance and connects it to the specified LCM
         * network.  See the documentation on lcm_handle() in the C API for
         * details on how lcm_url is formatted.
         *
         * @return 0 on success, -1 on failure.
         */
        inline int init(std::string lcm_url="");

        /**
         * Publishes a message.
         *
         * @return 0 on success, -1 on failure.
         */
        inline int publish(std::string channel, void *data, int datalen);

        /**
         * Publishes a message.  TODO
         *
         * @return 0 on success, -1 on failure.
         */
        template<class MessageType>
        inline int publish(std::string channel, const MessageType *msg);

        inline void unsubscribe(Subscription* subscription);

        inline int fileno();

        inline int handle();

        /**
         * Subscribe an object to a channel, with automatic message decoding.
         */
        template <class MessageType>
        Subscription* subscribe(std::string channel, 
                MessageHandler<MessageType>* handler);

        /** 
         * Subscribe a function callback to a channel, with automatic message
         * decoding.
         */
        template <class MessageType, class ContextClass> 
        Subscription* subscribeFunction(std::string channel,
                void (*handler)(const ReceiveBuffer* rbuf, 
                                std::string channel, 
                                const MessageType *msg, 
                                ContextClass context),
                ContextClass context);

        /**
         * Subscribe a function callback to a channel, without automatic
         * message decoding.
         */
        template <class ContextClass>
        Subscription* subscribeFunction(std::string channel,
                void (*handler)(const ReceiveBuffer* rbuf,
                                std::string channel,
                                ContextClass context),
                ContextClass context);

    private:
        lcm_t *lcm;

        std::vector<Subscription*> subscriptions;
};

// TODO make wrappers around lcm_eventlog_t, lcm_eventlog_event_t

//struct LogEvent {
//    int64_t eventnum;
//    int64_t timestamp;
//    int32_t channellen;
//    int32_t datalen;
//    char* channel;
//    void* data;
//};
//
//class LCMLog {
//    public:
//
//    private:
//
//};

#define __lcm_cpp_impl_ok__
#include "lcm-cpp-impl.hpp"
#undef __lcm_cpp_impl_ok__

}
#endif
