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

class LCMEncodable {
    public:
        virtual int getEncodedSize() const = 0;
        virtual void encode(void *buf, int datastart, int max_data_len) 
            const = 0;
};

class LCMSubscription {
    friend class LCM;
    protected:
        lcm_subscription_t *c_subs;
};

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
            T *msg = T::decode(rbuf->data, 0, rbuf->data_size);
            subs->handler(rbuf, channel, msg, subs->context);
            delete msg;
        }
};

class LCM {
    public:
        LCM(std::string lcm_url) {
            this->lcm = lcm_create(lcm_url.c_str());
        }
        ~LCM() {
            lcm_destroy(this->lcm);

            // TODO delete subscribers
        }

        int publish(std::string channel, void *data, int datalen) {
            lcm_publish(this->lcm, channel.c_str(), data, datalen);
        }
        int publish(std::string channel, const LCMEncodable *msg) {
            int datalen = msg->getEncodedSize();
            uint8_t *buf = new uint8_t[datalen];
            msg->encode(buf, 0, datalen);
            this->publish(channel, buf, datalen);
        }

        void unsubscribe(LCMSubscription *subscription) {
            // TODO check, update subscriber list
            lcm_unsubscribe(this->lcm, subscription->c_subs);
            delete subscription;
        }

        int fileno() {
            return lcm_get_fileno(this->lcm);
        }
        void handle() {
            lcm_handle(this->lcm);
        }

        // TODO generic subscribe

        template <class T, class U> 
        LCMSubscription *subscribe(std::string channel,
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

    private:
        lcm_t *lcm;

        // TODO subscriber list
};

}
#endif
