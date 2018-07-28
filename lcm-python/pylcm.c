#include <Python.h>  // include first because it contains pre-processor defs
#ifndef WIN32
#include <arpa/inet.h>
#include <netinet/in.h>
#include <sys/select.h>
#include <sys/socket.h>
#else
#include <winsock2.h>
#endif

#include "../lcm/dbg.h"
#include "pylcm.h"
#include "pylcm_subscription.h"

#ifndef Py_RETURN_NONE
#define Py_RETURN_NONE      \
    do {                    \
        Py_INCREF(Py_None); \
        return Py_None;     \
    } while (0)
#endif

//#define dbg(...) fprintf (stderr, __VA_ARGS__)
//#define dbg(...)

// to support python 2.5 and earlier
#ifndef Py_TYPE
#define Py_TYPE(ob) (((PyObject *) (ob))->ob_type)
#endif

// to support python 3 where all ints are long
#if PY_MAJOR_VERSION >= 3
#define PyInt_FromLong PyLong_FromLong
#define PyInt_AsLong PyLong_AsLong
#endif

PyDoc_STRVAR(pylcm_doc,
             "\
The LCM class provides a connection to an LCM network.\n\
\n\
usage::\n\
\n\
   m = LCM ([provider])\n\
\n\
provider is a string specifying the LCM network to join.  Since the Python \n\
LCM bindings are a wrapper around the C implementation, consult the C API\n\
documentation on how provider should be formatted.  provider may be None or \n\
the empty string, in which case a default network is chosen.\n\
\n\
To subscribe to a channel::\n\
\n\
   def msg_handler(channel, data):\n\
      # message handling code here.  For example:\n\
      print(\"received %d byte message on %s\" % (len(data), channel))\n\
\n\
   m.subscribe(channel, msg_handler)\n\
\n\
To transmit a raw binary string::\n\
\n\
   m.publish(\"CHANNEL_NAME\", data)\n\
\n\
In general, LCM is used with python modules compiled by lcm-gen, each of \n\
which provides the instance method encode() and the static method decode().\n\
Thus, if one had a compiled type named example_t, the following message\n\
handler would decode the message::\n\
\n\
   def msg_handler(channel, data):\n\
      msg = example_t.decode(data)\n\
\n\
and the following usage would publish a message::\n\
\n\
    msg = example_t()\n\
    # ... set member variables of msg\n\
    m.publish(\"CHANNEL_NAME\", msg.encode())\n\
\n\
@undocumented: __new__, __getattribute__\n\
");

// gives redefinition error in MSVC
// PyTypeObject pylcm_type;

// all LCM messages subscribed to by all LCM objects pass through this
// handler first.
static void pylcm_msg_handler(const lcm_recv_buf_t *rbuf, const char *channel, void *userdata)
{
    PyLCMSubscriptionObject *subs_obj = (PyLCMSubscriptionObject *) userdata;
    dbg(DBG_PYTHON, "%s %p %p\n", __FUNCTION__, subs_obj, subs_obj->lcm_obj);

    // Restore the thread state before calling back into Python.
    if (subs_obj->lcm_obj->saved_thread_state) {
        PyEval_RestoreThread(subs_obj->lcm_obj->saved_thread_state);
        subs_obj->lcm_obj->saved_thread_state = NULL;
    }

    // if an exception has occurred, then abort.
    if (PyErr_Occurred()) {
        return;
    }

#if PY_MAJOR_VERSION >= 3
    PyObject *arglist = Py_BuildValue("sy#", channel,  // build from bytes
                                      rbuf->data, rbuf->data_size);
#else
    PyObject *arglist = Py_BuildValue("ss#", channel,  // build from string
                                      rbuf->data, rbuf->data_size);
#endif

    PyObject *result = PyEval_CallObject(subs_obj->handler, arglist);
    Py_DECREF(arglist);

    if (!result) {
        subs_obj->lcm_obj->exception_raised = 1;
    } else {
        Py_DECREF(result);
    }
}

// =============== LCM class methods ==============

static PyObject *pylcm_subscribe(PyLCMObject *lcm_obj, PyObject *args)
{
    char *channel = NULL;
    int chan_len = 0;
    PyObject *handler = NULL;
    if (!PyArg_ParseTuple(args, "s#O", &channel, &chan_len, &handler))
        return NULL;

    if (!channel || !chan_len) {
        PyErr_SetString(PyExc_ValueError, "invalid channel");
        return NULL;
    }
    if (!PyCallable_Check(handler)) {
        PyErr_SetString(PyExc_ValueError, "handler is not callable");
        return NULL;
    }

    PyLCMSubscriptionObject *subs_obj =
        (PyLCMSubscriptionObject *) PyType_GenericNew(&pylcm_subscription_type, NULL, NULL);

    lcm_subscription_t *subscription =
        lcm_subscribe(lcm_obj->lcm, channel, pylcm_msg_handler, subs_obj);
    if (!subscription) {
        Py_DECREF(subs_obj);
        Py_RETURN_NONE;
    }

    subs_obj->subscription = subscription;
    subs_obj->handler = handler;
    Py_INCREF(handler);
    subs_obj->lcm_obj = lcm_obj;

    PyList_Append(lcm_obj->all_handlers, (PyObject *) subs_obj);

    return (PyObject *) subs_obj;
}

PyDoc_STRVAR(pylcm_subscribe_doc,
             "\
subscribe(channel, callback) -> L{LCMSubscription<lcm.LCMSubscription>}\n\
Registers a callback function to handle messages received on the specified\n\
channel.\n\
\n\
Multiple handlers can be registered for the same channel\n\
\n\
@param channel: LCM channel to subscribe to.  Can also be a GLib/PCRE regular\n\
expression.  Implicitly treated as the regex \"^channel$\"\n\
@param callback:  Message handler, must accept two arguments.\n\
When a message is received, callback is invoked with two arguments\n\
corresponding to the actual channel on which the message was received, and \n\
a binary string containing the raw message bytes.\n\
");

static PyObject *pylcm_unsubscribe(PyLCMObject *lcm_obj, PyObject *args)
{
    dbg(DBG_PYTHON, "%s %p\n", __FUNCTION__, lcm_obj);
    PyObject *_subs_obj = NULL;
    if (!PyArg_ParseTuple(args, "O!", &pylcm_subscription_type, &_subs_obj))
        return NULL;

    PyLCMSubscriptionObject *subs_obj = (PyLCMSubscriptionObject *) _subs_obj;
    if (!subs_obj->subscription || subs_obj->lcm_obj != lcm_obj) {
        PyErr_SetString(PyExc_ValueError, "Invalid Subscription object");
        return NULL;
    }
    int subs_index = 0;
    int nhandlers = PyList_Size(lcm_obj->all_handlers);
    for (subs_index = 0; subs_index < nhandlers; subs_index++) {
        PyObject *so = PyList_GetItem(lcm_obj->all_handlers, subs_index);
        if (so == (PyObject *) subs_obj) {
            PySequence_DelItem(lcm_obj->all_handlers, subs_index);
            break;
        }
    }
    if (subs_index == nhandlers) {
        PyErr_SetString(PyExc_ValueError, "Invalid Subscription object");
        return NULL;
    }

    lcm_unsubscribe(lcm_obj->lcm, subs_obj->subscription);
    subs_obj->subscription = NULL;
    Py_DECREF(subs_obj->handler);
    subs_obj->handler = NULL;
    subs_obj->lcm_obj = NULL;

    Py_RETURN_NONE;
}
PyDoc_STRVAR(pylcm_unsubscribe_doc,
             "\
unsubscribe(subscription_object) -> None\n\
Unregisters a message handler so that it will no longer be invoked when\n\
a message on the specified channel is received\n\
\n\
@param subscription_object: An LCMSubscription object, as returned by a\n\
call to subscribe()\n\
");

static PyObject *pylcm_publish(PyLCMObject *lcm_obj, PyObject *args)
{
    char *data = NULL;
    int datalen = 0;
    char *channel = NULL;

    if (!PyArg_ParseTuple(args, "ss#", &channel, &data, &datalen)) {
        return NULL;
    }
    if (!channel || !strlen(channel)) {
        PyErr_SetString(PyExc_ValueError, "invalid channel");
        return NULL;
    }

    int status;
    Py_BEGIN_ALLOW_THREADS;
    status = lcm_publish(lcm_obj->lcm, channel, (uint8_t *) data, datalen);
    Py_END_ALLOW_THREADS;

    if (0 != status) {
        PyErr_SetFromErrno(PyExc_IOError);
        return NULL;
    }

    Py_RETURN_NONE;
}
PyDoc_STRVAR(pylcm_publish_doc,
             "\
publish(channel, data) -> None\n\
Publishes a message to an LCM network\n\
\n\
@param channel: specifies the channel to which the message should be published.\n\
@param data: binary string containing the message to publish\n\
");

static PyObject *pylcm_fileno(PyLCMObject *lcm_obj)
{
    dbg(DBG_PYTHON, "%s %p\n", __FUNCTION__, lcm_obj);
    return PyInt_FromLong(lcm_get_fileno(lcm_obj->lcm));
}
PyDoc_STRVAR(pylcm_fileno_doc,
             "\
fileno() -> int\n\
\n\
Returns a file descriptor suitable for use with select, poll, etc.\n\
");

static PyObject *pylcm_handle(PyLCMObject *lcm_obj)
{
    dbg(DBG_PYTHON, "pylcm_handle(%p)\n", lcm_obj);

    if (lcm_obj->saved_thread_state) {
        PyErr_SetString(
            PyExc_RuntimeError,
            "only one thread is allowed to call LCM.handle() or LCM.handle_timeout() at a time");
        return NULL;
    }
    lcm_obj->saved_thread_state = PyEval_SaveThread();
    lcm_obj->exception_raised = 0;

    dbg(DBG_PYTHON, "calling lcm_handle(%p)\n", lcm_obj->lcm);
    int status = lcm_handle(lcm_obj->lcm);

    // Restore the thread state before returning back to Python.  The thread
    // state may have already been restored by the callback function
    // pylcm_msg_handler()
    if (lcm_obj->saved_thread_state) {
        PyEval_RestoreThread(lcm_obj->saved_thread_state);
        lcm_obj->saved_thread_state = NULL;
    }

    if (lcm_obj->exception_raised) {
        return NULL;
    }
    if (status < 0) {
        PyErr_SetString(PyExc_IOError, "lcm_handle() returned -1");
        return NULL;
    }
    Py_RETURN_NONE;
}
PyDoc_STRVAR(pylcm_handle_doc,
             "\
handle() -> None\n\
waits for and dispatches the next incoming message\n\
");

static PyObject *pylcm_handle_timeout(PyLCMObject *lcm_obj, PyObject *arg)
{
    int timeout_millis = PyInt_AsLong(arg);
    if (timeout_millis == -1 && PyErr_Occurred())
        return NULL;
    if (timeout_millis < 0) {
        PyErr_SetString(PyExc_ValueError, "invalid timeout");
        return NULL;
    }

    dbg(DBG_PYTHON, "pylcm_handle_timeout(%p, %d)\n", lcm_obj, timeout_millis);

    if (lcm_obj->saved_thread_state) {
        PyErr_SetString(PyExc_RuntimeError,
                        "Simultaneous calls to handle() / handle_timeout() detected");
        return NULL;
    }
    lcm_obj->saved_thread_state = PyEval_SaveThread();
    lcm_obj->exception_raised = 0;

    dbg(DBG_PYTHON, "calling lcm_handle_timeout(%p, %d)\n", lcm_obj->lcm, timeout_millis);
    int status = lcm_handle_timeout(lcm_obj->lcm, timeout_millis);

    // Restore the thread state before returning back to Python.  The thread
    // state may have already been restored by the callback function
    // pylcm_msg_handler()
    if (lcm_obj->saved_thread_state) {
        PyEval_RestoreThread(lcm_obj->saved_thread_state);
        lcm_obj->saved_thread_state = NULL;
    }

    if (lcm_obj->exception_raised) {
        return NULL;
    }
    if (status < 0) {
        PyErr_SetString(PyExc_IOError, "lcm_handle_timeout() returned -1");
        return NULL;
    }
    return PyInt_FromLong(status);
}
PyDoc_STRVAR(pylcm_handle_timeout_doc,
             "\
handle_timeout(timeout_millis) -> int\n\
New in LCM 1.1.0\n\
\n\
waits for and dispatches the next incoming message, with a timeout.\n\
\n\
Raises ValueError if @p timeout_millis is invalid, or IOError if another\n\
error occurs.\n\
\n\
@param timeout_millis: the amount of time to wait, in milliseconds.\n\
@return 0 if the function timed out, >1 if a message was handled.\n\
");

static PyMethodDef pylcm_methods[] = {
    {"handle", (PyCFunction) pylcm_handle, METH_NOARGS, pylcm_handle_doc},
    {"handle_timeout", (PyCFunction) pylcm_handle_timeout, METH_O, pylcm_handle_timeout_doc},
    {"subscribe", (PyCFunction) pylcm_subscribe, METH_VARARGS, pylcm_subscribe_doc},
    {"unsubscribe", (PyCFunction) pylcm_unsubscribe, METH_VARARGS, pylcm_unsubscribe_doc},
    {"publish", (PyCFunction) pylcm_publish, METH_VARARGS, pylcm_publish_doc},
    {"fileno", (PyCFunction) pylcm_fileno, METH_NOARGS, pylcm_fileno_doc},
    {NULL, NULL}, /* sentinel */
};

// ==================== class administrative methods ====================

static PyObject *pylcm_new(PyTypeObject *type, PyObject *args, PyObject *kwds)
{
    PyObject *obj = type->tp_alloc(type, 0);
    if (!obj)
        return NULL;
    PyLCMObject *lcm_obj = (PyLCMObject *) obj;
    lcm_obj->all_handlers = PyList_New(0);
    if (!lcm_obj->all_handlers) {
        Py_DECREF(obj);
        return NULL;
    }
    return obj;
}

static void pylcm_dealloc(PyLCMObject *lcm_obj)
{
    dbg(DBG_PYTHON, "pylcm_dealloc\n");
    if (lcm_obj->lcm) {
        lcm_destroy(lcm_obj->lcm);
        lcm_obj->lcm = NULL;
    }
    Py_DECREF(lcm_obj->all_handlers);
    Py_TYPE(lcm_obj)->tp_free((PyObject *) lcm_obj);
}

static int pylcm_initobj(PyObject *self, PyObject *args, PyObject *kwargs)
{
    dbg(DBG_PYTHON, "%s %p\n", __FUNCTION__, self);
    PyLCMObject *lcm_obj = (PyLCMObject *) self;

    char *url = NULL;

    if (!PyArg_ParseTuple(args, "|s", &url))
        return -1;

    lcm_obj->lcm = lcm_create(url);
    if (!lcm_obj->lcm) {
        PyErr_SetString(PyExc_RuntimeError, "Couldn't create LCM");
        return -1;
    }
    lcm_obj->saved_thread_state = NULL;

    return 0;
}

/* Type object for socket objects. */
PyTypeObject pylcm_type = {
#if PY_MAJOR_VERSION >= 3
    PyVarObject_HEAD_INIT(0, 0) /* size is now part of macro */
#else
    PyObject_HEAD_INIT(0) /* Must fill in type value later */
    0,                    /* ob_size */
#endif
    "LCM",                                    /* tp_name */
    sizeof(PyLCMObject),                      /* tp_basicsize */
    0,                                        /* tp_itemsize */
    (destructor) pylcm_dealloc,               /* tp_dealloc */
    0,                                        /* tp_print */
    0,                                        /* tp_getattr */
    0,                                        /* tp_setattr */
    0,                                        /* tp_compare */
    0,                                        /* tp_repr */
    0,                                        /* tp_as_number */
    0,                                        /* tp_as_sequence */
    0,                                        /* tp_as_mapping */
    0,                                        /* tp_hash */
    0,                                        /* tp_call */
    0,                                        /* tp_str */
    PyObject_GenericGetAttr,                  /* tp_getattro */
    0,                                        /* tp_setattro */
    0,                                        /* tp_as_buffer */
    Py_TPFLAGS_DEFAULT | Py_TPFLAGS_BASETYPE, /* tp_flags */
    pylcm_doc,                                /* tp_doc */
    0,                                        /* tp_traverse */
    0,                                        /* tp_clear */
    0,                                        /* tp_richcompare */
    0,                                        /* tp_weaklistoffset */
    0,                                        /* tp_iter */
    0,                                        /* tp_iternext */
    pylcm_methods,                            /* tp_methods */
    0,                                        /* tp_members */
    0,                                        /* tp_getset */
    0,                                        /* tp_base */
    0,                                        /* tp_dict */
    0,                                        /* tp_descr_get */
    0,                                        /* tp_descr_set */
    0,                                        /* tp_dictoffset */
    pylcm_initobj,                            /* tp_init */
    PyType_GenericAlloc,                      /* tp_alloc */
    pylcm_new,                                /* tp_new */
    PyObject_Del,                             /* tp_free */
};
