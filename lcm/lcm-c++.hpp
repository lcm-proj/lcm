#ifndef __lcm_cpp_hpp__
#define __lcm_cpp_hpp__

/**
 * C++ wrappers for liblcm.  Primarily intended for use with the C++ message
 * bindings (once they exist).
 *
 * TODO usage documentation
 */

#include <string>
#include <lcm/lcm.h>

namespace lcm {

class LCMSubscription {
    friend class LCM;
    protected:
        lcm_subscription_t *c_subs;
};

class LCM {
    public:
        inline LCM(std::string lcm_url);

        inline ~LCM();

        inline int publish(std::string channel, void *data, int datalen);

        template<class T>
        inline int publish(std::string channel, const T *msg);

        void unsubscribe(LCMSubscription *subscription);

        inline int fileno();

        inline void handle();

        // TODO generic subscribe

        template <class T, class U> 
        LCMSubscription *subscribe(std::string channel,
                void (*handler)(const lcm_recv_buf_t *rbuf, 
                    std::string channel, 
                    const T *msg, U context),
                U context);

    private:
        lcm_t *lcm;

        // TODO subscriber list
};

// =============== implementation ===============

template <class T, class U> 
class TypedLCMSubscription : public LCMSubscription {
    friend class LCM;
    private:
        U context;
        void (*handler)(const lcm_recv_buf_t *rbuf, std::string channel, 
                const T*msg, U context);
        static void c_handler(const lcm_recv_buf_t *rbuf, const char *channel, 
                void *user_data)
        {
            TypedLCMSubscription<T,U> *subs = 
                static_cast<TypedLCMSubscription<T,U> *>(user_data);
            T msg;
            msg.decode(rbuf->data, 0, rbuf->data_size);
            subs->handler(rbuf, channel, &msg, subs->context);
            msg.decodeCleanup();;
        }
};

inline 
LCM::LCM(std::string lcm_url)
{
    this->lcm = lcm_create(lcm_url.c_str());
}

inline 
LCM::~LCM() {
    lcm_destroy(this->lcm);

    // TODO delete subscribers
}

inline int 
LCM::publish(std::string channel, void *data, int datalen) {
    return lcm_publish(this->lcm, channel.c_str(), data, datalen);
}

template<class T>
inline int 
LCM::publish(std::string channel, const T *msg) {
    unsigned int datalen = msg->getEncodedSize();
    uint8_t *buf = new uint8_t[datalen];
    msg->encode(buf, 0, datalen);
    return this->publish(channel, buf, datalen);
}

void 
LCM::unsubscribe(LCMSubscription *subscription) {
    // TODO check, update subscriber list
    lcm_unsubscribe(this->lcm, subscription->c_subs);
    delete subscription;
}

inline int 
LCM::fileno() {
    return lcm_get_fileno(this->lcm);
}

inline void 
LCM::handle() {
    lcm_handle(this->lcm);
}

template <class T, class U> 
inline LCMSubscription *
LCM::subscribe(std::string channel,
        void (*handler)(const lcm_recv_buf_t *rbuf, 
            std::string channel, 
            const T *msg, U context),
        U context) {

    TypedLCMSubscription<T, U> *subs = new TypedLCMSubscription<T, U>();
    subs->c_subs = lcm_subscribe(this->lcm, channel.c_str(), 
            TypedLCMSubscription<T,U>::c_handler, subs);
    subs->handler = handler;
    subs->context = context;

    // TODO add subscription to internal list.

    return subs;
}

}
#endif
