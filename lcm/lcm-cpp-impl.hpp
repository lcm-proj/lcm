// DO NOT EVER INCLUDE THIS HEADER FILE YOURSELF

#ifndef __lcm_cpp_impl_ok__
#error "Don't include this file"
#endif

// =============== implementation ===============

class Subscription {
    public:
        virtual ~Subscription() {}
    friend class LCM;
    protected:
        lcm_subscription_t *c_subs;
};

template <class MessageType, class ContextClass> 
class LCMTypedSubscription : public Subscription {
    friend class LCM;
    private:
        ContextClass context;
        void (*handler)(const ReceiveBuffer *rbuf, std::string channel, 
                const MessageType*msg, ContextClass context);
        static void cb_func(const lcm_recv_buf_t *rbuf, const char *channel, 
                void *user_data)
        {
            typedef LCMTypedSubscription<MessageType,ContextClass> SubsClass;
            SubsClass *subs = static_cast<SubsClass *> (user_data);
            MessageType msg;
            int status = msg.decode(rbuf->data, 0, rbuf->data_size);
            if (status < 0) {
                fprintf (stderr, "error %d decoding %s!!!\n", status,
                        MessageType::getTypeName());
                return;
            }
            const ReceiveBuffer rb = {
                rbuf->data,
                rbuf->data_size,
                rbuf->recv_utime
            };
            subs->handler(&rb, channel, &msg, subs->context);
            msg.decodeCleanup();;
        }
};

template <class ContextClass> 
class LCMUntypedSubscription : public Subscription {
    friend class LCM;
    private:
        ContextClass context;
        void (*handler)(const ReceiveBuffer *rbuf, std::string channel, 
                ContextClass context);
        static void cb_func(const lcm_recv_buf_t *rbuf, const char *channel, 
                void *user_data)
        {
            typedef LCMUntypedSubscription<ContextClass> SubsClass;
            SubsClass *subs = static_cast<SubsClass *> (user_data);
            const ReceiveBuffer rb = {
                rbuf->data,
                rbuf->data_size,
                rbuf->recv_utime
            };
            subs->handler(&rb, channel, subs->context);
        }
};

template <class MessageType> 
class LCMMHSubscription : public Subscription {
    friend class LCM;
    private:
        MessageHandler<MessageType>* handler;
        static void cb_func(const lcm_recv_buf_t *rbuf, const char *channel, 
                void *user_data)
        {
            LCMMHSubscription<MessageType> *subs = 
                static_cast<LCMMHSubscription<MessageType> *>(user_data);
            MessageType msg;
            int status = msg.decode(rbuf->data, 0, rbuf->data_size);
            if (status < 0) {
                fprintf (stderr, "error %d decoding %s!!!\n", status,
                        MessageType::getTypeName());
                return;
            }
            const ReceiveBuffer rb = {
                rbuf->data,
                rbuf->data_size,
                rbuf->recv_utime
            };
            subs->handler->handleMessage(&rb, channel, &msg);
            msg.decodeCleanup();
        }
};

// specialization of LCMMHSubscription template class for class
// message handlers that do not want the message automatically decoded.
//
// To use this, subclass MessageHandler<void>.  e.g.,
//
// class MyHandler : public MessageHandler<void> {
//     public handleMessage(const ReceiveBuffer* rbuf, std::string chan,
//         void* msg_data) {
//         printf("first byte of message: 0x%X\n", ((uint8_t*)rbuf)->data[0]);
//     }
// }
template<> 
class LCMMHSubscription<void> : public Subscription {
    friend class LCM;
    private:
        MessageHandler<void>* handler;
        static void cb_func(const lcm_recv_buf_t *rbuf, const char *channel, 
                void *user_data)
        {
            LCMMHSubscription<void> *subs = 
                static_cast<LCMMHSubscription<void> *>(user_data);
            const ReceiveBuffer rb = {
                rbuf->data,
                rbuf->data_size,
                rbuf->recv_utime
            };
            subs->handler->handleMessage(&rb, channel, rbuf->data);
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
    for(int i=0, num_subscribers=this->subscriptions.size(); 
            i<num_subscribers; i++) {
        delete this->subscriptions[i];
    }
}

inline int 
LCM::publish(std::string channel, void *data, int datalen) {
    return lcm_publish(this->lcm, channel.c_str(), data, datalen);
}

template<class MessageType>
inline int 
LCM::publish(std::string channel, const MessageType *msg) {
    unsigned int datalen = msg->getEncodedSize();
    uint8_t *buf = new uint8_t[datalen];
    msg->encode(buf, 0, datalen);
    return this->publish(channel, buf, datalen);
}

inline void 
LCM::unsubscribe(Subscription *subscription) {
    std::vector<Subscription*>::iterator iter;
    std::vector<Subscription*>::iterator eiter = subscriptions.end();
    for(iter=subscriptions.begin(); iter!= eiter; ++iter) {
        if(*iter == subscription) {
            lcm_unsubscribe(lcm, subscription->c_subs);
            subscriptions.erase(iter);
            delete subscription;
            break;
        }
    }
}

inline int 
LCM::fileno() {
    return lcm_get_fileno(this->lcm);
}

inline void 
LCM::handle() {
    lcm_handle(this->lcm);
}

template <class MessageType>
Subscription* 
LCM::subscribe(std::string channel, MessageHandler<MessageType>* handler)
{
    LCMMHSubscription<MessageType> *subs = new LCMMHSubscription<MessageType>();
    subs->handler = handler;
    subs->c_subs = lcm_subscribe(this->lcm, channel.c_str(), 
            LCMMHSubscription<MessageType>::cb_func, subs);
    subscriptions.push_back(subs);
    return subs;
}

template <class MessageType, class ContextClass> 
Subscription*
LCM::subscribeFunction(std::string channel,
        void (*handler)(const ReceiveBuffer *rbuf, 
            std::string channel, 
            const MessageType *msg, ContextClass context),
        ContextClass context) {

    typedef LCMTypedSubscription<MessageType, ContextClass> SubsClass;
    SubsClass *sub = new SubsClass();
    sub->c_subs = lcm_subscribe(lcm, channel.c_str(), SubsClass::cb_func, sub);
    sub->handler = handler;
    sub->context = context;
    subscriptions.push_back(sub);
    return sub;
}

template <class ContextClass> 
Subscription*
LCM::subscribeFunction(std::string channel,
        void (*handler)(const ReceiveBuffer *rbuf, 
                       std::string channel, 
                       ContextClass context),
        ContextClass context) {
    typedef LCMUntypedSubscription<ContextClass> SubsClass;
    SubsClass *sub = new SubsClass();
    sub->c_subs = lcm_subscribe(lcm, channel.c_str(), SubsClass::cb_func, sub);
    sub->handler = handler;
    sub->context = context;
    subscriptions.push_back(sub);
    return sub;
}

