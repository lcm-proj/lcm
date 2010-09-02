#ifndef __lcm_cpp_hpp__
#define __lcm_cpp_hpp__

/**
 * C++ wrappers for liblcm.  Primarily intended for use with the C++ message
 * bindings (once they exist).
 *
 * TODO usage documentation
 */

#include <string>
#include <vector>
#include <lcm/lcm.h>

namespace lcm {

class Subscription;

// TODO make wrappers around lcm_recv_buf_t, lcm_eventlog_t, lcm_eventlog_event_t

template<class MessageType>
class MessageHandler {
    public:
        virtual void handleMessage(const lcm_recv_buf_t* rbuf,
                std::string channel, const MessageType* msg) = 0;
        virtual ~MessageHandler() {}
};

class LCM {
    public:
        inline LCM(std::string lcm_url);

        inline ~LCM();

        inline int publish(std::string channel, void *data, int datalen);

        template<class MessageType>
        inline int publish(std::string channel, const MessageType *msg);

        inline void unsubscribe(Subscription* subscription);

        inline int fileno();

        inline void handle();

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
                void (*handler)(const lcm_recv_buf_t* rbuf, 
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
                void (*handler)(const lcm_recv_buf_t* rbuf,
                                std::string channel,
                                ContextClass context),
                ContextClass context);

    private:
        lcm_t *lcm;

        std::vector<Subscription*> subscriptions;
};

#define __lcm_cpp_impl_ok__
#include "lcm-cpp-impl.hpp"
#undef __lcm_cpp_impl_ok__

}
#endif
