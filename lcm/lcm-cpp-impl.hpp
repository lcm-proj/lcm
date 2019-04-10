// DO NOT EVER INCLUDE THIS HEADER FILE YOURSELF

#ifndef __lcm_cpp_impl_ok__
#error "Don't include this file"
#endif

// =============== implementation ===============

int Subscription::setQueueCapacity(int num_messages)
{
    return lcm_subscription_set_queue_capacity(c_subs, num_messages);
}

int Subscription::getQueueSize() const
{
    return lcm_subscription_get_queue_size(c_subs);
}

template <class MessageType, class ContextClass>
class LCMTypedSubscription : public Subscription {
    friend class LCM;

  private:
    ContextClass context;
    void (*handler)(const ReceiveBuffer *rbuf, const std::string &channel, const MessageType *msg,
                    ContextClass context);
    static void cb_func(const lcm_recv_buf_t *rbuf, const char *channel, void *user_data)
    {
        typedef LCMTypedSubscription<MessageType, ContextClass> SubsClass;
        SubsClass *subs = static_cast<SubsClass *>(user_data);
        MessageType msg;
        int status = msg.decode(rbuf->data, 0, rbuf->data_size);
        if (status < 0) {
            fprintf(stderr, "error %d decoding %s!!!\n", status, MessageType::getTypeName());
            return;
        }
        const ReceiveBuffer rb = {rbuf->data, rbuf->data_size, rbuf->recv_utime};
        subs->handler(&rb, channel, &msg, subs->context);
    }
};

template <class ContextClass>
class LCMUntypedSubscription : public Subscription {
    friend class LCM;

  private:
    ContextClass context;
    void (*handler)(const ReceiveBuffer *rbuf, const std::string &channel, ContextClass context);
    static void cb_func(const lcm_recv_buf_t *rbuf, const char *channel, void *user_data)
    {
        typedef LCMUntypedSubscription<ContextClass> SubsClass;
        SubsClass *subs = static_cast<SubsClass *>(user_data);
        const ReceiveBuffer rb = {rbuf->data, rbuf->data_size, rbuf->recv_utime};
        subs->handler(&rb, channel, subs->context);
    }
};

template <class MessageType, class MessageHandlerClass>
class LCMMHSubscription : public Subscription {
    friend class LCM;

  private:
    MessageHandlerClass *handler;
    void (MessageHandlerClass::*handlerMethod)(const ReceiveBuffer *rbuf,
                                               const std::string &channel, const MessageType *msg);
    static void cb_func(const lcm_recv_buf_t *rbuf, const char *channel, void *user_data)
    {
        LCMMHSubscription<MessageType, MessageHandlerClass> *subs =
            static_cast<LCMMHSubscription<MessageType, MessageHandlerClass> *>(user_data);
        MessageType msg;
        int status = msg.decode(rbuf->data, 0, rbuf->data_size);
        if (status < 0) {
            fprintf(stderr, "error %d decoding %s!!!\n", status, MessageType::getTypeName());
            return;
        }
        const ReceiveBuffer rb = {rbuf->data, rbuf->data_size, rbuf->recv_utime};
        std::string chan_str(channel);
        (subs->handler->*subs->handlerMethod)(&rb, chan_str, &msg);
    }
};

template <class MessageHandlerClass>
class LCMMHUntypedSubscription : public Subscription {
    friend class LCM;

  private:
    MessageHandlerClass *handler;
    void (MessageHandlerClass::*handlerMethod)(const ReceiveBuffer *rbuf,
                                               const std::string &channel);
    static void cb_func(const lcm_recv_buf_t *rbuf, const char *channel, void *user_data)
    {
        LCMMHUntypedSubscription<MessageHandlerClass> *subs =
            static_cast<LCMMHUntypedSubscription<MessageHandlerClass> *>(user_data);
        const ReceiveBuffer rb = {rbuf->data, rbuf->data_size, rbuf->recv_utime};
        std::string chan_str(channel);
        (subs->handler->*subs->handlerMethod)(&rb, chan_str);
    }
};

#if LCM_CXX_11_ENABLED
template <class MessageType>
class LCMLambdaSubscription : public Subscription {
    friend class LCM;

  private:
    using HandlerFunction = typename LCM::HandlerFunction<MessageType>;
    HandlerFunction handler;
    static void cb_func(const lcm_recv_buf_t *rbuf, const char *channel, void *user_data)
    {
        LCMLambdaSubscription<MessageType> *subs =
            static_cast<LCMLambdaSubscription<MessageType> *>(user_data);
        MessageType msg;
        int status = msg.decode(rbuf->data, 0, rbuf->data_size);
        if (status < 0) {
            fprintf(stderr, "error %d decoding %s!!!\n", status, MessageType::getTypeName());
            return;
        }
        const ReceiveBuffer rb = {rbuf->data, rbuf->data_size, rbuf->recv_utime};
        std::string chan_str(channel);
        (subs->handler)(&rb, chan_str, &msg);
    }
};
#endif

inline LCM::LCM(std::string lcm_url) : owns_lcm(true)
{
    this->lcm = lcm_create(lcm_url.c_str());
}

inline LCM::LCM(lcm_t *lcm_in) : owns_lcm(false)
{
    this->lcm = lcm_in;
}

inline bool LCM::good() const
{
    return this->lcm != NULL;
}

inline LCM::~LCM()
{
    for (int i = 0, n = subscriptions.size(); i < n; i++) {
        delete subscriptions[i];
    }
    if (this->lcm && this->owns_lcm) {
        lcm_destroy(this->lcm);
    }
}

inline int LCM::publish(const std::string &channel, const void *data, unsigned int datalen)
{
    if (!this->lcm) {
        fprintf(stderr, "LCM instance not initialized.  Ignoring call to publish()\n");
        return -1;
    }
    return lcm_publish(this->lcm, channel.c_str(), data, datalen);
}

template <class MessageType>
inline int LCM::publish(const std::string &channel, const MessageType *msg)
{
    unsigned int datalen = msg->getEncodedSize();
    uint8_t *buf = new uint8_t[datalen];
    msg->encode(buf, 0, datalen);
    int status = this->publish(channel, buf, datalen);
    delete[] buf;
    return status;
}

inline int LCM::unsubscribe(Subscription *subscription)
{
    if (!this->lcm) {
        fprintf(stderr, "LCM instance not initialized.  Ignoring call to unsubscribe()\n");
        return -1;
    }
    std::vector<Subscription *>::iterator iter;
    std::vector<Subscription *>::iterator eiter = subscriptions.end();
    for (iter = subscriptions.begin(); iter != eiter; ++iter) {
        if (*iter == subscription) {
            int status = lcm_unsubscribe(lcm, subscription->c_subs);
            subscriptions.erase(iter);
            delete subscription;
            return status;
        }
    }
    return -1;
}

inline int LCM::getFileno()
{
    if (!this->lcm) {
        fprintf(stderr, "LCM instance not initialized.  Ignoring call to fileno()\n");
        return -1;
    }
    return lcm_get_fileno(this->lcm);
}

inline int LCM::handle()
{
    if (!this->lcm) {
        fprintf(stderr, "LCM instance not initialized.  Ignoring call to handle()\n");
        return -1;
    }
    return lcm_handle(this->lcm);
}

inline int LCM::handleTimeout(int timeout_millis)
{
    if (!this->lcm) {
        fprintf(stderr, "LCM instance not initialized.  Ignoring call to handle()\n");
        return -1;
    }
    return lcm_handle_timeout(this->lcm, timeout_millis);
}

template <class MessageType, class MessageHandlerClass>
Subscription *LCM::subscribe(const std::string &channel,
                             void (MessageHandlerClass::*handlerMethod)(const ReceiveBuffer *rbuf,
                                                                        const std::string &channel,
                                                                        const MessageType *msg),
                             MessageHandlerClass *handler)
{
    if (!this->lcm) {
        fprintf(stderr, "LCM instance not initialized.  Ignoring call to subscribe()\n");
        return NULL;
    }
    LCMMHSubscription<MessageType, MessageHandlerClass> *subs =
        new LCMMHSubscription<MessageType, MessageHandlerClass>();
    subs->handler = handler;
    subs->handlerMethod = handlerMethod;
    subs->c_subs =
        lcm_subscribe(this->lcm, channel.c_str(),
                      LCMMHSubscription<MessageType, MessageHandlerClass>::cb_func, subs);
    subscriptions.push_back(subs);
    return subs;
}

template <class MessageHandlerClass>
Subscription *LCM::subscribe(const std::string &channel,
                             void (MessageHandlerClass::*handlerMethod)(const ReceiveBuffer *rbuf,
                                                                        const std::string &channel),
                             MessageHandlerClass *handler)
{
    if (!this->lcm) {
        fprintf(stderr, "LCM instance not initialized.  Ignoring call to subscribe()\n");
        return NULL;
    }
    LCMMHUntypedSubscription<MessageHandlerClass> *subs =
        new LCMMHUntypedSubscription<MessageHandlerClass>();
    subs->handler = handler;
    subs->handlerMethod = handlerMethod;
    subs->c_subs = lcm_subscribe(this->lcm, channel.c_str(),
                                 LCMMHUntypedSubscription<MessageHandlerClass>::cb_func, subs);
    subscriptions.push_back(subs);
    return subs;
}

template <class MessageType, class ContextClass>
Subscription *LCM::subscribeFunction(const std::string &channel,
                                     void (*handler)(const ReceiveBuffer *rbuf,
                                                     const std::string &channel,
                                                     const MessageType *msg, ContextClass context),
                                     ContextClass context)
{
    if (!this->lcm) {
        fprintf(stderr, "LCM instance not initialized.  Ignoring call to subscribeFunction()\n");
        return NULL;
    }
    typedef LCMTypedSubscription<MessageType, ContextClass> SubsClass;
    SubsClass *sub = new SubsClass();
    sub->handler = handler;
    sub->context = context;
    sub->c_subs = lcm_subscribe(lcm, channel.c_str(), SubsClass::cb_func, sub);
    subscriptions.push_back(sub);
    return sub;
}

template <class ContextClass>
Subscription *LCM::subscribeFunction(const std::string &channel,
                                     void (*handler)(const ReceiveBuffer *rbuf,
                                                     const std::string &channel,
                                                     ContextClass context),
                                     ContextClass context)
{
    if (!this->lcm) {
        fprintf(stderr, "LCM instance not initialized.  Ignoring call to subscribeFunction()\n");
        return NULL;
    }
    typedef LCMUntypedSubscription<ContextClass> SubsClass;
    SubsClass *sub = new SubsClass();
    sub->handler = handler;
    sub->context = context;
    sub->c_subs = lcm_subscribe(lcm, channel.c_str(), SubsClass::cb_func, sub);
    subscriptions.push_back(sub);
    return sub;
}

#if LCM_CXX_11_ENABLED
template <class MessageType>
Subscription *LCM::subscribe(const std::string &channel, LCM::HandlerFunction<MessageType> handler)
{
    if (!this->lcm) {
        fprintf(stderr, "LCM instance not initialized.  Ignoring call to subscribe()\n");
        return NULL;
    }
    LCMLambdaSubscription<MessageType> *subs = new LCMLambdaSubscription<MessageType>();
    subs->handler = handler;
    subs->c_subs = lcm_subscribe(this->lcm, channel.c_str(),
                                 LCMLambdaSubscription<MessageType>::cb_func, subs);
    subscriptions.push_back(subs);
    return subs;
}
#endif

lcm_t *LCM::getUnderlyingLCM()
{
    return this->lcm;
}

LogFile::LogFile(const std::string &path, const std::string &mode)
    : eventlog(lcm_eventlog_create(path.c_str(), mode.c_str())), last_event(NULL)
{
}

LogFile::~LogFile()
{
    if (eventlog)
        lcm_eventlog_destroy(eventlog);
    eventlog = NULL;
    if (last_event)
        lcm_eventlog_free_event(last_event);
    last_event = NULL;
}

bool LogFile::good() const
{
    return eventlog != NULL;
}

const LogEvent *LogFile::readNextEvent()
{
    lcm_eventlog_event_t *evt = lcm_eventlog_read_next_event(eventlog);
    if (last_event)
        lcm_eventlog_free_event(last_event);
    last_event = evt;
    if (!evt)
        return NULL;
    curEvent.eventnum = evt->eventnum;
    curEvent.timestamp = evt->timestamp;
    curEvent.channel.assign(evt->channel, evt->channellen);
    curEvent.datalen = evt->datalen;
    curEvent.data = evt->data;
    return &curEvent;
}

int LogFile::seekToTimestamp(int64_t timestamp)
{
    return lcm_eventlog_seek_to_timestamp(eventlog, timestamp);
}

int LogFile::writeEvent(LogEvent *event)
{
    lcm_eventlog_event_t evt;
    evt.eventnum = event->eventnum;
    evt.timestamp = event->timestamp;
    evt.channellen = event->channel.size();
    evt.datalen = event->datalen;
    evt.channel = const_cast<char *>(event->channel.c_str());
    evt.data = event->data;
    return lcm_eventlog_write_event(eventlog, &evt);
}

FILE *LogFile::getFilePtr()
{
    return eventlog->f;
}
