#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#include "pylcm.h"
#include "pylcm_subscription.h"

#ifndef Py_RETURN_NONE
#define Py_RETURN_NONE  do { Py_INCREF( Py_None ); return Py_None; } while(0)
#endif

//#define dbg(...) fprintf (stderr, __VA_ARGS__)
#define dbg(...) 

PyDoc_STRVAR (pylcm_doc,
"Lightweight Communications class\n\
\n\
LCM (url)\n\
");

PyTypeObject pylcm_type;

// all LCM messages subscribed to by all LCM objects pass through this
// handler first.
static int
pylcm_msg_handler (const lcm_recv_buf_t *rbuf, void *userdata)
{
    // if an exception has occurred, then abort.
    if (PyErr_Occurred ()) return 0;

    PyLCMSubscriptionObject *subs_obj = userdata;

    PyObject *arglist = Py_BuildValue ("ss#", rbuf->channel, 
            rbuf->data, rbuf->data_size);

    PyObject *result  = PyEval_CallObject (subs_obj->handler, arglist);
    Py_DECREF (arglist);

    if (! result) {
        subs_obj->lcm_obj->exception_raised = 1;
    } else {
        Py_DECREF (result);
    }
    return 0;
}

// =============== LCM class methods ==============

static PyObject *
pylcm_subscribe (PyLCMObject *lcm_obj, PyObject *args)
{
    char *channel = NULL;
    int chan_len = 0;
    PyObject *handler = NULL;
    if (!PyArg_ParseTuple (args, "s#O", &channel, &chan_len, &handler)) 
        return NULL;

    if (!channel || ! chan_len) {
        PyErr_SetString (PyExc_ValueError, "invalid channel");
        return NULL;
    }
    if (!PyCallable_Check (handler))  {
        PyErr_SetString (PyExc_ValueError, "handler is not callable");
        return NULL;
    }

    PyLCMSubscriptionObject * subs_obj = 
        (PyLCMSubscriptionObject*) PyType_GenericNew (&pylcm_subscription_type, 
                NULL, NULL);

    lcm_subscription_t *subscription = 
        lcm_subscribe (lcm_obj->lcm, channel, pylcm_msg_handler, subs_obj);
    if (!subscription) {
        Py_DECREF (subs_obj);
        Py_RETURN_NONE;
    }

    subs_obj->subscription = subscription;
    subs_obj->handler = handler;
    Py_INCREF (handler);
    subs_obj->lcm_obj = lcm_obj;

    PyList_Append (lcm_obj->all_handlers, (PyObject*)subs_obj);

    return (PyObject*)subs_obj;
}

PyDoc_STRVAR (pylcm_subscribe_doc, 
"subscribe(channel, callback) -> subscription_object\n\
\n\
registers a callback function to messages received on a certain channel.\n\
Multiple handlers can be registered for the same channel\n\
\n\
channel can also be a POSIX regular expression. It is implicitly treated as\n\
\"^channel$\" \n\
");

static PyObject *
pylcm_unsubscribe (PyLCMObject *lcm_obj, PyObject *args)
{
    dbg ("%s %p\n", __FUNCTION__, lcm_obj);
    PyObject *_subs_obj = NULL;
    if (!PyArg_ParseTuple (args, "O!", &pylcm_subscription_type, 
                &_subs_obj))
        return NULL;

    PyLCMSubscriptionObject *subs_obj = (PyLCMSubscriptionObject*) _subs_obj;
    if (!subs_obj->subscription || subs_obj->lcm_obj != lcm_obj) {
        PyErr_SetString (PyExc_ValueError, "Invalid Subscription object");
        return NULL;
    }
    int subs_index = 0;
    int nhandlers = PyList_Size (lcm_obj->all_handlers);
    for (subs_index=0; subs_index<nhandlers; subs_index++) {
        PyObject *so = PyList_GetItem (lcm_obj->all_handlers, subs_index);
        if (so == (PyObject*) subs_obj) {
            PySequence_DelItem (lcm_obj->all_handlers, subs_index);
            break;
        }
    }
    if (subs_index == nhandlers) {
        PyErr_SetString (PyExc_ValueError, "Invalid Subscription object");
        return NULL;
    }

    lcm_unsubscribe (lcm_obj->lcm, subs_obj->subscription);
    subs_obj->subscription = NULL;
    Py_DECREF (subs_obj->handler);
    subs_obj->handler = NULL;
    subs_obj->lcm_obj = NULL;

    printf ("OK!\n");

    Py_RETURN_NONE;
}
PyDoc_STRVAR (pylcm_unsubscribe_doc, 
"unsubscribe(subscription_object) -> None\n\
\n\
unregisters a message handler so that it will no longer be invoked when\n\
a message on the specified channel is received\n\
");

static PyObject *
pylcm_publish (PyLCMObject *lcm_obj, PyObject *args)
{
    char *data = NULL;
    int datalen = 0;
    char *channel = NULL;

    if (!PyArg_ParseTuple (args, "ss#", &channel, &data, &datalen)) {
        return NULL;
    }
    if (!channel || !strlen (channel)) {
        PyErr_SetString (PyExc_ValueError, "invalid channel");
        return NULL;
    }
    int status;

    Py_BEGIN_ALLOW_THREADS
    status = lcm_publish (lcm_obj->lcm, channel, data, datalen);
    Py_END_ALLOW_THREADS

    if (0 != status) {
        PyErr_SetFromErrno (PyExc_IOError);
        return NULL;
    }

    Py_RETURN_NONE;
}
PyDoc_STRVAR (pylcm_publish_doc,
"publish (channel, data)\n\
\n\
Publishes a message to a multicast group");

static PyObject *
pylcm_fileno (PyLCMObject *lcm_obj)
{
    dbg ("%s %p\n", __FUNCTION__, lcm_obj);
    return PyInt_FromLong (lcm_get_fileno (lcm_obj->lcm));
}
PyDoc_STRVAR (pylcm_fileno_doc,
"for use with select, poll, etc.");

static PyObject *
pylcm_handle (PyLCMObject *lcm_obj)
{
    dbg ("%s %p\n", __FUNCTION__, lcm_obj);
    int fd = lcm_get_fileno (lcm_obj->lcm);
    fd_set fds;
    FD_ZERO (&fds);
    FD_SET (fd, &fds);
    int status;

    Py_BEGIN_ALLOW_THREADS
    status = select (fd+1, &fds, NULL, NULL, NULL);
    Py_END_ALLOW_THREADS

    if (status < 0) {
        PyErr_SetFromErrno (PyExc_IOError);
        return NULL;
    }

    // XXX how to properly use Py_{BEGIN,END}_ALLOW_THREADS here????
    lcm_obj->exception_raised = 0;
    lcm_handle (lcm_obj->lcm);
    if (lcm_obj->exception_raised) return NULL;
    Py_RETURN_NONE;
}
PyDoc_STRVAR (pylcm_handle_doc,
"\n\
waits for and dispatches the next incoming message\n\
\n\
Message handlers are invoked in this order:\n\
1.  type specific handlers are invoked in the order registered.  Then,\n\
2.  catchall handlers are invoked in the order registered\n\
");

static PyMethodDef pylcm_methods[] = {
    { "handle", (PyCFunction)pylcm_handle, METH_NOARGS, pylcm_handle_doc },
    { "subscribe", (PyCFunction)pylcm_subscribe, METH_VARARGS, 
        pylcm_subscribe_doc },
    { "unsubscribe", (PyCFunction)pylcm_unsubscribe, METH_VARARGS,
        pylcm_unsubscribe_doc },
    { "publish", (PyCFunction)pylcm_publish, METH_VARARGS,
        pylcm_publish_doc },
    { "fileno", (PyCFunction)pylcm_fileno, METH_NOARGS, pylcm_fileno_doc },
    { NULL, NULL }
};

// ==================== class administrative methods ====================

static PyObject *
pylcm_new (PyTypeObject *type, PyObject *args, PyObject *kwds)
{
	PyObject *obj = type->tp_alloc (type, 0);
    if (!obj) return NULL;
    PyLCMObject *lcm_obj = (PyLCMObject*) obj;
    lcm_obj->all_handlers = PyList_New (0);
    if (!lcm_obj->all_handlers) {
        Py_DECREF (obj);
        return NULL;
    }
	return obj;
}

static void
pylcm_dealloc (PyLCMObject *lcm_obj)
{
    dbg ("pylcm_dealloc\n");
    if (lcm_obj->lcm) {
        lcm_destroy (lcm_obj->lcm);
        lcm_obj->lcm = NULL;
    }
    Py_DECREF (lcm_obj->all_handlers);
    lcm_obj->ob_type->tp_free ((PyObject*)lcm_obj);
}

static int
pylcm_initobj (PyObject *self, PyObject *args, PyObject *kwargs)
{
    dbg ("%s %p\n", __FUNCTION__, self);
    PyLCMObject *s = (PyLCMObject *)self;

    char *url = NULL;

    if (!PyArg_ParseTuple (args, "|s", &url))
        return -1;

    s->lcm = lcm_create (url);
    if (! s->lcm) {
        PyErr_SetString (PyExc_RuntimeError, "Couldn't create LCM");
        return -1;
    }

    return 0;
}

/* Type object for socket objects. */
PyTypeObject pylcm_type = {
    PyObject_HEAD_INIT (0)   /* Must fill in type value later */
    0,                  /* ob_size */
    "LCM.LCM",            /* tp_name */
    sizeof (PyLCMObject),     /* tp_basicsize */
    0,                  /* tp_itemsize */
    (destructor)pylcm_dealloc,     /* tp_dealloc */
    0,                  /* tp_print */
    0,                  /* tp_getattr */
    0,                  /* tp_setattr */
    0,                  /* tp_compare */
    0,                  /* tp_repr */
    0,                  /* tp_as_number */
    0,                  /* tp_as_sequence */
    0,                  /* tp_as_mapping */
    0,                  /* tp_hash */
    0,                  /* tp_call */
    0,                  /* tp_str */
    PyObject_GenericGetAttr,        /* tp_getattro */
    0,                  /* tp_setattro */
    0,                  /* tp_as_buffer */
    Py_TPFLAGS_DEFAULT | Py_TPFLAGS_BASETYPE, /* tp_flags */
    pylcm_doc,               /* tp_doc */
    0,                  /* tp_traverse */
    0,                  /* tp_clear */
    0,                  /* tp_richcompare */
    0,                  /* tp_weaklistoffset */
    0,                  /* tp_iter */
    0,                  /* tp_iternext */
    pylcm_methods,               /* tp_methods */
    0,                  /* tp_members */
    0,                  /* tp_getset */
    0,                  /* tp_base */
    0,                  /* tp_dict */
    0,                  /* tp_descr_get */
    0,                  /* tp_descr_set */
    0,                  /* tp_dictoffset */
    pylcm_initobj,             /* tp_init */
    PyType_GenericAlloc,            /* tp_alloc */
    pylcm_new,             /* tp_new */
    PyObject_Del,               /* tp_free */
};
