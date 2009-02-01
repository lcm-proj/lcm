#include <string.h>

#include <matrix.h>
#include <mex.h>

#include <lcm/lcm.h>

typedef struct _lcmmx_priv_t lcmmx_priv_t;

typedef struct {
    lcmmx_priv_t *priv;
    lcm_subscription_t *subs;
    char *callback_fn;
    char *user_data;

} lcmmx_subscription_priv_t;

static void
lcmmx_subscription_priv_destroy(lcmmx_subscription_priv_t *mxsubs)
{
    if(mxsubs->callback_fn)
        mxFree(mxsubs->callback_fn);
    if(mxsubs->user_data)
        mxFree(mxsubs->user_data);
    mxFree(mxsubs);
}

typedef struct _lcmmx_queued_received_msg_t lcmmx_queued_received_msg_t;
struct _lcmmx_queued_received_msg_t {
    mxArray *chan_arr;
    mxArray *data_arr;
    mxArray *user_data_arr;

    lcmmx_queued_received_msg_t * next;
};

struct _lcmmx_priv_t {
    lcm_t *lcm;

    int nsubscriptions;
    lcmmx_subscription_priv_t **subscriptions;

    int nmsgs;
    lcmmx_queued_received_msg_t *received_messages;
};

static void
lcmmx_priv_destroy(lcmmx_priv_t *priv)
{
    lcm_destroy(priv->lcm);

    int i;
    for(i=0; i<priv->nsubscriptions; i++) {
        lcmmx_subscription_priv_destroy(priv->subscriptions[i]);
    }
    mxFree(priv->subscriptions);
    mxFree(priv);
}

static lcmmx_priv_t * 
lcmmx_get_priv(const mxArray *p)
{
    lcmmx_priv_t *priv = NULL;
    char ptrstr[sizeof(lcmmx_priv_t*) * 8 + 4];

    /**
     * get the lcm_t pointer
     */
    if(!mxIsStruct(p)) {
        mexErrMsgTxt("First argument must be an LCM struct (input is not a struct)");
        return NULL;
    }

    mxArray *ptr_arr = mxGetField(p, 0, "priv_data");
    if(!ptr_arr) {
        mexErrMsgTxt("First argument must be an LCM struct (couldn't find pointer)");
        return NULL;
    }

    if(!mxIsChar(ptr_arr)) {
        mexErrMsgTxt("First argument must be an LCM struct (priv_data is not a string)");
        return NULL;
    }
    if(0 != mxGetString(ptr_arr, ptrstr, sizeof(ptrstr)-1)) {
        mexErrMsgTxt("Error getting LCM pointer");
        return NULL;
    }
    sscanf(ptrstr, "%p", &priv);

    return priv;
}

static mxArray * 
lcmmx_create(const mxArray *rhs)
{
    char lcmurl[4096];
    int status = 0;
    lcm_t *lcm = NULL;
    char ptrstr[sizeof(lcm_t*) * 8 + 4];
    mxArray *result = NULL;
    const char *fieldnames[] = {
        "priv_data",
    };
    int nfields = sizeof(fieldnames) / sizeof(char*);

    memset(lcmurl, 0, sizeof(lcmurl));

    if(rhs) {
        if(!mxIsChar(rhs))
            mexErrMsgTxt("Input must be a string");
        if(0 != mxGetString(rhs, lcmurl, sizeof(lcmurl)-1))
            mexErrMsgTxt("Error getting URL");
        lcm = lcm_create(lcmurl); 
    } else {
        lcm = lcm_create(NULL);
    }

    if(!lcm) {
        mexErrMsgTxt("Error creating LCM instance");
    }

    lcmmx_priv_t * priv = mxMalloc(sizeof(lcmmx_priv_t));
    mexMakeMemoryPersistent(priv);
    priv->lcm = lcm;
    priv->subscriptions = NULL;
    priv->nsubscriptions = 0;
    priv->nmsgs = 0;
    priv->received_messages = NULL;

    result = mxCreateStructMatrix(1, 1, nfields, fieldnames);
    memset(ptrstr, 0, sizeof(ptrstr));
    sprintf(ptrstr, "%p", priv);
    mxArray *lcmptr = mxCreateString(ptrstr);

    mxSetField(result, 0, "priv_data", lcmptr);

    return result;
}

static void
lcmmx_destroy(const mxArray *lcmmx_struct)
{
    lcmmx_priv_t *priv = lcmmx_get_priv(lcmmx_struct);
    if(!priv) {
        return;
    }
    lcmmx_priv_destroy(priv);
}

static mxArray * 
lcmmx_get_fileno(const mxArray *lcmmx_struct)
{
    lcmmx_priv_t *priv = lcmmx_get_priv(lcmmx_struct);

    mxArray *result = (mxArray*) mxCreateNumericMatrix(1, 1, 
            mxINT64_CLASS, mxREAL );
    if(! result) 
        mexErrMsgTxt("Error allocating result");

    int64_t * result_data = mxGetData(result);
    result_data[0] = lcm_get_fileno(priv->lcm);

    return result;
}

static void 
lcmmx_publish(const mxArray *lcmmx_struct, const mxArray *channel_arr, const mxArray *data_arr)
{
    lcmmx_priv_t *priv = lcmmx_get_priv(lcmmx_struct);
    if(!priv) {
        return;
    }

    if(!mxIsChar(channel_arr)) {
        mexErrMsgTxt("Second argument must be a string");
    }
    if(!mxIsChar(data_arr) && !mxIsUint8(data_arr) && !mxIsInt8(data_arr)) {
        mexErrMsgTxt("Data must be of type char, int8, or uint8");
    }

    /**
     * get the channel on which to publish
     */
    int chanlen = mxGetN(channel_arr) + 1;
    char *channel = malloc(chanlen);
    mxGetString(channel_arr, channel, chanlen);

    /**
     * get the raw data to publish
     */
    void *data = NULL;
    int datalen = 0;
    if(mxIsChar(data_arr)) {
        datalen = mxGetN(data_arr) + 1;
        data = malloc(datalen);
        mxGetString(data_arr, data, datalen);
        lcm_publish(priv->lcm, channel, data, datalen);
        free(data);
    } else {
        data = mxGetData(data_arr);
        datalen = mxGetN(data_arr);
        lcm_publish(priv->lcm, channel, data, datalen);
        free(data);
    }

    free(channel);
}

static void
lcmmx_msg_handler(const lcm_recv_buf_t *rbuf, const char *channel, 
        void *user_data);

static void 
lcmmx_subscribe(const mxArray *lcmmx_struct, const mxArray *channel_arr, 
        const mxArray *userdata_arr,
        mxArray **result)
{
    lcmmx_priv_t *priv = lcmmx_get_priv(lcmmx_struct);
    if(!priv) {
        return;
    }

    /* channel */
    if(!mxIsChar(channel_arr)) {
        mexErrMsgTxt("Second argument must be a string");
    }
    int chanlen = mxGetN(channel_arr) + 1;
    char *channel = malloc(chanlen);
    mxGetString(channel_arr, channel, chanlen);

    lcmmx_subscription_priv_t *mxsubs = 
        mxMalloc(sizeof(lcmmx_subscription_priv_t));
    mexMakeMemoryPersistent(mxsubs);
    mxsubs->priv = priv;
    mxsubs->subs = lcm_subscribe(priv->lcm, channel, 
            lcmmx_msg_handler, mxsubs);
    mxsubs->callback_fn = NULL;
    mxsubs->user_data = NULL;

    /* bookkeeping */
    priv->nsubscriptions += 1;
    priv->subscriptions = mxRealloc(priv->subscriptions, 
            priv->nsubscriptions * sizeof(lcmmx_subscription_priv_t*));
    mexMakeMemoryPersistent(priv->subscriptions);
    priv->subscriptions[priv->nsubscriptions-1] = mxsubs;

#if 0
    /* callback */
    if(!mxIsChar(cb_arr)) {
        mexErrMsgTxt("Second argument must be a string");
    }
    int cblen = mxGetN(cb_arr) + 1;
    mxsubs->callback_fn = mxMalloc(cblen);
    mexMakeMemoryPersistent(mxsubs->callback_fn);
    mxGetString(cb_arr, mxsubs->callback_fn, cblen);
#endif
    
    /* userdata */
    if(!mxIsChar(userdata_arr)) {
        mexErrMsgTxt("User data must be a string");
    }
    int udlen = mxGetN(userdata_arr) + 1;
    mxsubs->user_data = mxMalloc(udlen);
    mexMakeMemoryPersistent(mxsubs->user_data);
    mxGetString(userdata_arr, mxsubs->user_data, udlen);

    if(result) {
        const char *fieldnames[] = {
            "subs_data",
        };
        int nfields = sizeof(fieldnames) / sizeof(char*);
        char ptrstr[sizeof(lcm_subscription_t*) * 8 + 4];
        *result = mxCreateStructMatrix(1, 1, nfields, fieldnames);
        memset(ptrstr, 0, sizeof(ptrstr));
        sprintf(ptrstr, "%p", mxsubs);
        mxArray *ptr_arr = mxCreateString(ptrstr);

        mxSetField(*result, 0, "subs_data", ptr_arr);
    }
}

static void 
lcmmx_unsubscribe(const mxArray *lcmmx_struct, const mxArray *subs_struct)
{
    lcmmx_priv_t *priv = lcmmx_get_priv(lcmmx_struct);
    if(!priv) {
        return;
    }

    lcmmx_subscription_priv_t *mxsubs = NULL;
    char ptrstr[sizeof(lcm_subscription_t*) * 8 + 4];

    if(!mxIsStruct(subs_struct)) {
        mexErrMsgTxt("Second argument must be an LCM Subscription struct (input is not a struct)");
        return;
    }

    mxArray *ptr_arr = mxGetField(subs_struct, 0, "subs_data");
    if(!ptr_arr) {
        mexErrMsgTxt("Second argument must be an LCM Subscription struct (couldn't find pointer)");
        return;
    }

    if(!mxIsChar(ptr_arr)) {
        mexErrMsgTxt("Second argument must be an LCM Subscription struct (subs_data is not a string)");
        return;
    }
    if(0 != mxGetString(ptr_arr, ptrstr, sizeof(ptrstr)-1)) {
        mexErrMsgTxt("Error getting LCM Subscription pointer");
        return;
    }
    sscanf(ptrstr, "%p", &mxsubs);

    if(priv != mxsubs->priv) {
        mexErrMsgTxt("This LCM object does not own the subscription");
        return;
    }

    /* bookkeeping */
    int i;
    for(i=0; i<priv->nsubscriptions; i++) {
        if(priv->subscriptions[i] == mxsubs) 
            break;
    }
    if(i == priv->nsubscriptions) {
        mexErrMsgTxt("Internal Error");
        return;
    }
    int toremove_ind = i;

    lcm_unsubscribe(priv->lcm, mxsubs->subs);
    lcmmx_subscription_priv_destroy(mxsubs);

    /* more bookeeping */
    if(priv->nsubscriptions - 1> toremove_ind) {
        priv->subscriptions[toremove_ind] = priv->subscriptions[priv->nsubscriptions-1];
    }
    priv->nsubscriptions--;

    return;
}

static void
lcmmx_msg_handler(const lcm_recv_buf_t *rbuf, const char *channel, 
        void *user_data)
{
    lcmmx_subscription_priv_t *mxsubs = (lcmmx_subscription_priv_t*) user_data;

#if 0
    if(mxsubs->callback_fn) {
        printf("handle: [%s] [%s]\n", channel, mxsubs->callback_fn);
        /* TODO */
    }
#endif

    /* make a copy of the message channel and data, and throw it into a linked list
     * for processing after lcm_handle() returns
     */

    lcmmx_queued_received_msg_t *msg = malloc(sizeof(lcmmx_queued_received_msg_t));

    /* channel */
    msg->chan_arr = mxCreateString(channel);

    /* raw data bytes */
    msg->data_arr = mxCreateNumericMatrix(rbuf->data_size, 1,   mxUINT8_CLASS, 0);
    memcpy(mxGetData(msg->data_arr), rbuf->data, rbuf->data_size);

    /* user data */
    msg->user_data_arr = mxCreateString(mxsubs->user_data);

    /* insert into linked list */
    lcmmx_priv_t *priv = mxsubs->priv;
    msg->next = priv->received_messages;
    priv->received_messages = msg;
    priv->nmsgs++;
}

static mxArray * 
lcmmx_handle(const mxArray *lcmmx_struct, const mxArray *timeout_ms_arr)
{
    lcmmx_priv_t *priv = lcmmx_get_priv(lcmmx_struct);
    if(!priv) {
        return;
    }

    if(timeout_ms_arr) {
        double *p = mxGetPr(timeout_ms_arr);
        double timeout_ms = p[0];

        struct timeval to;
        to.tv_sec = (long) timeout_ms / 1000;
        to.tv_usec = (long) ((timeout_ms - to.tv_sec * 1000) * 1000);
        int fd = lcm_get_fileno(priv->lcm);

        int maxfd = 0;
        fd_set readfds;
        FD_ZERO(&readfds);

        FD_SET(fd,&readfds);
        maxfd = fd;
        int status=select(maxfd + 1,&readfds,0,0,&to);
        if(!FD_ISSET(fd,&readfds)) {
            return;
        }
    }

    if(priv->received_messages) {
        mexErrMsgTxt("lcm_mex has bad internal state");
    }
    priv->received_messages = NULL;
    priv->nmsgs = 0;
    lcm_handle(priv->lcm);

    /* create a cell array of message structures to return to the user. */
    const char *fieldnames[] = {
        "channel",
        "data",
        "userdata"
    };
    int nfields = sizeof(fieldnames) / sizeof(char*);
    mxArray *result = mxCreateStructMatrix(priv->nmsgs, 1, nfields, fieldnames);

    int i;
    for(i=0; i<priv->nmsgs; i++) {
        lcmmx_queued_received_msg_t *msg = priv->received_messages;

        mxSetField(result, i, "channel", msg->chan_arr);
        mxSetField(result, i, "data", msg->data_arr);
        mxSetField(result, i, "userdata", msg->user_data_arr);

        priv->received_messages = msg->next;
        free(msg);
    }
    priv->received_messages = NULL;
    priv->nmsgs = 0;
    return result;
}


void 
mexFunction(int nlhs, mxArray *plhs[],
        int nrhs, const mxArray *prhs[])
{
    if(nrhs < 1) {
        mexErrMsgTxt("Expected function specifier");
    }

    const mxArray * fn_arr = prhs[0];
    char fn_str[80];
    if(!mxIsChar(fn_arr)) {
        mexErrMsgTxt("Bad function specifier (not string)");
    }
    if(0 != mxGetString(fn_arr, fn_str, sizeof(fn_str)-1)) {
        mexErrMsgTxt("Error getting function specifier");
    }

    if(! strcmp(fn_str, "create")) {
        if(nlhs != 1) {
            mexErrMsgTxt("Must have one output argument");
        }
        if(nrhs == 1) {
            plhs[0] = lcmmx_create(NULL);
        } else if(nrhs == 2) {
            plhs[0] = lcmmx_create(prhs[1]);
        } else {
            mexErrMsgTxt("At most one input argument");
        }
    } else if(! strcmp(fn_str, "destroy")) {
        if(nlhs != 0) {
            mexErrMsgTxt("No output arguments produced");
        }
        if(nrhs != 2) {
            mexErrMsgTxt("Expecting one input argument");
        }
        lcmmx_destroy(prhs[1]);
    } else if(! strcmp(fn_str, "get_fileno")) {
        if(nlhs == 0) {
            return;
        } if(nlhs > 1) {
            mexErrMsgTxt("At most one output argument");
        }
        if(nrhs != 2) {
            mexErrMsgTxt("Expecting one input argument");
        }
        plhs[0] = lcmmx_get_fileno(prhs[1]);
    } else if(! strcmp(fn_str, "subscribe")) {
        if(nrhs != 4) {
            mexErrMsgTxt("Expecting four input arguments");
        }
        if(nlhs == 0) {
            lcmmx_subscribe(prhs[1], prhs[2], prhs[3], NULL);
        } else if(nlhs == 1) {
            lcmmx_subscribe(prhs[1], prhs[2], prhs[3], &plhs[0]);
        } else {
            mexErrMsgTxt("At most one output argument");
        }
    } else if(! strcmp(fn_str, "unsubscribe")) {
        if(nlhs != 0) {
            mexErrMsgTxt("No output arguments produced");
        }
        if(nrhs != 3) {
            mexErrMsgTxt("Expecting two input arguments");
        }
        lcmmx_unsubscribe(prhs[1], prhs[2]);
    } else if(! strcmp(fn_str, "publish")) {
        if(nlhs != 0) {
            mexErrMsgTxt("No output arguments");
        }
        if(nrhs != 4) {
            mexErrMsgTxt("Expecting three input arguments");
        }
        lcmmx_publish(prhs[1], prhs[2], prhs[3]);
    } else if(! strcmp(fn_str, "get_messages")) {
        if(nlhs != 1) {
            mexErrMsgTxt("Exactly one output argument");
        }
        if(nrhs == 2) {
            plhs[0] = lcmmx_handle(prhs[1], NULL);
        } else if(nrhs == 3) {
            /* timeout */
            plhs[0] = lcmmx_handle(prhs[1], prhs[2]);
        } else {
            mexErrMsgTxt("Expected one or two input arguments");
        }
    } else {
        mexErrMsgTxt("Bad function specifier (unrecognized)");
    }

}
