#include "pylc.h"
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#ifndef Py_RETURN_NONE
#define Py_RETURN_NONE  do { Py_INCREF( Py_None ); return Py_None; } while(0)
#endif

//#define dbg(...) fprintf (stderr, __VA_ARGS__)
#define dbg(...) 

PyDoc_STRVAR (pylc_doc,
"Lightweight Communications class\n\
\n\
LC (local_iface = \"\", mc_addr = \""LC_DEFAULT_MC_ADDR"\",\n\
     mc_port = -1, transmit_only=False, mc_ttl=0,\n\
     recv_buf_size=0)\n\
");

PyTypeObject pylc_type;

// all LC messages subscribed to by all LC objects pass through this
// handler first.
static int
pylc_msg_handler (const lc_recv_buf_t *rbuf, void *userdata)
{
    // if an exception has occurred, then abort.
    if (PyErr_Occurred ()) return -1;

    PyObject *tup = userdata;
    PyLCObject *s = (PyLCObject*) PyTuple_GET_ITEM (tup, 0);
    PyObject *handler = PyTuple_GET_ITEM (tup, 1);

    PyObject *arglist = Py_BuildValue ("ss#", rbuf->channel, 
            rbuf->data, rbuf->data_size);
    PyObject *result  = PyEval_CallObject (handler, arglist);
    Py_DECREF (arglist);

    if (! result) {
        s->exception_raised = 1;
        printf ("exception raised!\n");
    } else {
        Py_DECREF (result);
    }
    return 0;
}

// =============== LC class methods ==============

static PyObject *
internal_subscribe (PyLCObject *s, const char *channel, PyObject *handler)
{
    dbg ("pylc.c: internal_subscribe [%s] %p (%p)\n", channel, handler, s);
    if (!channel || ! strlen (channel)) {
        PyErr_SetString (PyExc_ValueError, "invalid channel");
        return NULL;
    }
    if (!PyCallable_Check (handler))  {
        PyErr_SetString (PyExc_ValueError, "handler is not callable");
        return NULL;
    }

    PyObject *tup = PyTuple_Pack (2, (PyObject*)s, handler);
    PyList_Append (s->all_handlers, tup);
    Py_DECREF (tup);

    lc_subscribe (s->lc, channel, pylc_msg_handler, tup);

    Py_RETURN_NONE;
}

static PyObject *
internal_unsubscribe (PyLCObject *s, const char *channel, PyObject *handler)
{
    dbg ("pylc.c: internal_unsubscribe [%s] %p (%p\n", channel, handler, s);
    if (!channel || ! strlen (channel)) {
        PyErr_SetString (PyExc_ValueError, "invalid channel");
        return NULL;
    }
    if (!PyCallable_Check (handler))  {
        PyErr_SetString (PyExc_ValueError, "handler is not callable");
        return NULL;
    }

    int nhandlers = PyList_Size (s->all_handlers);
    int i;
    for (i=0; i<nhandlers; i++) {
        PyObject *tup = PyList_GetItem (s->all_handlers, i);
        if (!tup) return NULL;
        PyObject *h = PyTuple_GetItem (tup, 1);
        if (h == handler) {
            lc_unsubscribe_by_func (s->lc, channel, pylc_msg_handler, handler);
            PySequence_DelItem (s->all_handlers, i);
            dbg ("found handler to unregister (%d) remain\n",
                    PySequence_Size (s->all_handlers));
            break;
        }
    }

    Py_RETURN_NONE;
}

static PyObject *
pylc_subscribe (PyLCObject *s, PyObject *args)
{
    char *channel = NULL;
    PyObject *handler = NULL;
    if (!PyArg_ParseTuple (args, "sO", &channel, &handler)) { return NULL; }
    return internal_subscribe (s, channel, handler);
}
PyDoc_STRVAR (pylc_subscribe_doc, 
"registers a callback function to handle all messages on a certain channel.\n\
Multiple handlers can be registered for a given channel\n\
");

static PyObject *
pylc_unsubscribe (PyLCObject *s, PyObject *args)
{
    char *channel = NULL;
    PyObject *handler = NULL;
    if (!PyArg_ParseTuple (args, "sO", &channel, &handler)) { return NULL; }
    return internal_unsubscribe (s, channel, handler);
}
PyDoc_STRVAR (pylc_unsubscribe_doc, 
"unregisters a message handler so that it will no longer be invoked when\n\
a message on the specified channel is received\n\
");

static PyObject *
pylc_publish (PyLCObject *s, PyObject *args)
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
    status = lc_publish (s->lc, channel, data, datalen);
    Py_END_ALLOW_THREADS

    if (0 != status) {
        PyErr_SetFromErrno (PyExc_IOError);
        return NULL;
    }

    Py_RETURN_NONE;
}
PyDoc_STRVAR (pylc_publish_doc,
"publish (channel, data)\n\
\n\
Publishes a message to a multicast group");

static PyObject *
pylc_fileno (PyLCObject *s)
{
    dbg ("%s %p\n", __FUNCTION__, s);
    return PyInt_FromLong (lc_get_fileno (s->lc));
}
PyDoc_STRVAR (pylc_fileno_doc,
"for use with select, poll, etc.");

static PyObject *
pylc_handle (PyLCObject *s)
{
    dbg ("%s %p\n", __FUNCTION__, s);
    int fd = lc_get_fileno (s->lc);
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
    s->exception_raised = 0;
    lc_handle (s->lc);
    if (s->exception_raised) return NULL;
    Py_RETURN_NONE;
}
PyDoc_STRVAR (pylc_handle_doc,
"\n\
waits for and dispatches the next incoming message\n\
\n\
Message handlers are invoked in this order:\n\
1.  type specific handlers are invoked in the order registered.  Then,\n\
2.  catchall handlers are invoked in the order registered\n\
");

static PyMethodDef pylc_methods[] = {
    { "handle", (PyCFunction)pylc_handle, METH_NOARGS, pylc_handle_doc },
    { "subscribe", (PyCFunction)pylc_subscribe, METH_VARARGS, 
        pylc_subscribe_doc },
    { "unsubscribe", (PyCFunction)pylc_unsubscribe, METH_VARARGS,
        pylc_unsubscribe_doc },
    { "publish", (PyCFunction)pylc_publish, METH_VARARGS,
        pylc_publish_doc },
    { "fileno", (PyCFunction)pylc_fileno, METH_NOARGS, pylc_fileno_doc },
    { NULL, NULL }
};

// ==================== class administrative methods ====================

PyMODINIT_FUNC
init_lcobject (PyLCObject *s, lc_t *lc)
{
    s->lc = lc;
}

static PyObject *
pylc_repr (PyLCObject *s)
{
    char buf[512];
    PyOS_snprintf (buf, sizeof (buf),
                "<LC object ... TODO>");
    return PyString_FromString (buf);
}

static PyObject *
pylc_new (PyTypeObject *type, PyObject *args, PyObject *kwds)
{
	PyObject *new;

	new = type->tp_alloc (type, 0);
	if (new != NULL) {
		 ((PyLCObject*)new)->lc = NULL;
         ((PyLCObject*)new)->all_handlers = PyList_New (0);
	}
	return new;
}

static void
pylc_dealloc (PyLCObject *s)
{
    dbg ("pylc_dealloc\n");
    if (s->lc) {
        lc_destroy (s->lc);
        s->lc = NULL;
    }
    Py_DECREF (s->all_handlers);
    s->ob_type->tp_free ((PyObject*)s);
}

static int
pylc_initobj (PyObject *self, PyObject *args, PyObject *kwds)
{
    dbg ("%s %p\n", __FUNCTION__, self);
    PyLCObject *s = (PyLCObject *)self;
    static char *keywords[] = { "local_iface", "mc_addr", 
        "mc_port", "transmit_only", "mc_ttl", "recv_buf_size", 0};

    lc_params_t params;
    lc_params_init_defaults (&params);
    char default_local_iface_str[80];
    char default_mc_addr_str[80];
    struct in_addr local_iface_sin = { params.local_iface };
    strcpy (default_local_iface_str, inet_ntoa (local_iface_sin));
    struct in_addr mc_addr_sin = { params.mc_addr };
    strcpy (default_mc_addr_str, inet_ntoa (mc_addr_sin));
    char *local_iface_str = default_local_iface_str;
    char *mc_addr_str = default_mc_addr_str;

    if (!PyArg_ParseTupleAndKeywords (args, kwds, "|ssHiBi", keywords,
                &local_iface_str, &mc_addr_str, 
                &params.mc_port, 
                &params.transmit_only,
                &params.mc_ttl,
                &params.recv_buf_size))
        return -1;

    if (local_iface_str != default_local_iface_str) {
        struct in_addr t;
        if (0 == inet_aton (local_iface_str, &t)) {
            PyErr_SetString (PyExc_ValueError, "invalid local_iface");
            return -1;
        }
        params.local_iface = t.s_addr;
    }
    if (mc_addr_str != default_mc_addr_str) {
        struct in_addr t;
        if (0 == inet_aton (mc_addr_str, &t)) {
            PyErr_SetString (PyExc_ValueError, "invalid mc_addr");
            return -1;
        }
        params.mc_addr = t.s_addr;
    }

    lc_t *lc = lc_create ();
    if (! lc) {
        PyErr_SetString (PyExc_RuntimeError, "Couldn't create LC");
        return -1;
    }

    if (0 != lc_init (lc, &params)) {
        lc_destroy (lc);
        PyErr_SetString (PyExc_RuntimeError, "Error initializing LC object");
        return -1;
    }

    init_lcobject (s, lc);

    return 0;
}

/* Type object for socket objects. */
PyTypeObject pylc_type = {
    PyObject_HEAD_INIT (0)   /* Must fill in type value later */
    0,                  /* ob_size */
    "LC.LC",            /* tp_name */
    sizeof (PyLCObject),     /* tp_basicsize */
    0,                  /* tp_itemsize */
    (destructor)pylc_dealloc,     /* tp_dealloc */
    0,                  /* tp_print */
    0,                  /* tp_getattr */
    0,                  /* tp_setattr */
    0,                  /* tp_compare */
    (reprfunc)pylc_repr,          /* tp_repr */
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
    pylc_doc,               /* tp_doc */
    0,                  /* tp_traverse */
    0,                  /* tp_clear */
    0,                  /* tp_richcompare */
    0,                  /* tp_weaklistoffset */
    0,                  /* tp_iter */
    0,                  /* tp_iternext */
    pylc_methods,               /* tp_methods */
    0,                  /* tp_members */
    0,                  /* tp_getset */
    0,                  /* tp_base */
    0,                  /* tp_dict */
    0,                  /* tp_descr_get */
    0,                  /* tp_descr_set */
    0,                  /* tp_dictoffset */
    pylc_initobj,             /* tp_init */
    PyType_GenericAlloc,            /* tp_alloc */
    pylc_new,             /* tp_new */
    PyObject_Del,               /* tp_free */
};
