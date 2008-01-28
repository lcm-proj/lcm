#include "pylcm.h"
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

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
    if (PyErr_Occurred ()) return -1;

    PyObject *tup = userdata;
    PyLCMObject *s = (PyLCMObject*) PyTuple_GET_ITEM (tup, 0);
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

// =============== LCM class methods ==============

static PyObject *
internal_subscribe (PyLCMObject *s, const char *channel, PyObject *handler)
{
    dbg ("pylcm.c: internal_subscribe [%s] %p (%p)\n", channel, handler, s);
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

    lcm_subscribe (s->lcm, channel, pylcm_msg_handler, tup);

    Py_RETURN_NONE;
}

static PyObject *
internal_unsubscribe (PyLCMObject *s, const char *channel, PyObject *handler)
{
    dbg ("pylcm.c: internal_unsubscribe [%s] %p (%p\n", channel, handler, s);
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
            lcm_unsubscribe_by_func (s->lcm, pylcm_msg_handler, handler);
            PySequence_DelItem (s->all_handlers, i);
            dbg ("found handler to unregister (%d) remain\n",
                    PySequence_Size (s->all_handlers));
            break;
        }
    }

    Py_RETURN_NONE;
}

static PyObject *
pylcm_subscribe (PyLCMObject *s, PyObject *args)
{
    char *channel = NULL;
    PyObject *handler = NULL;
    if (!PyArg_ParseTuple (args, "sO", &channel, &handler)) { return NULL; }
    return internal_subscribe (s, channel, handler);
}
PyDoc_STRVAR (pylcm_subscribe_doc, 
"registers a callback function to handle all messages on a certain channel.\n\
Multiple handlers can be registered for a given channel\n\
");

static PyObject *
pylcm_unsubscribe (PyLCMObject *s, PyObject *args)
{
    char *channel = NULL;
    PyObject *handler = NULL;
    if (!PyArg_ParseTuple (args, "sO", &channel, &handler)) { return NULL; }
    return internal_unsubscribe (s, channel, handler);
}
PyDoc_STRVAR (pylcm_unsubscribe_doc, 
"unregisters a message handler so that it will no longer be invoked when\n\
a message on the specified channel is received\n\
");

static PyObject *
pylcm_publish (PyLCMObject *s, PyObject *args)
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
    status = lcm_publish (s->lcm, channel, data, datalen);
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
pylcm_fileno (PyLCMObject *s)
{
    dbg ("%s %p\n", __FUNCTION__, s);
    return PyInt_FromLong (lcm_get_fileno (s->lcm));
}
PyDoc_STRVAR (pylcm_fileno_doc,
"for use with select, poll, etc.");

static PyObject *
pylcm_handle (PyLCMObject *s)
{
    dbg ("%s %p\n", __FUNCTION__, s);
    int fd = lcm_get_fileno (s->lcm);
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
    lcm_handle (s->lcm);
    if (s->exception_raised) return NULL;
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

PyMODINIT_FUNC
init_lcmobject (PyLCMObject *s, lcm_t *lcm)
{
    s->lcm = lcm;
}

static PyObject *
pylcm_repr (PyLCMObject *s)
{
    char buf[512];
    PyOS_snprintf (buf, sizeof (buf),
                "<LCM object ... TODO>");
    return PyString_FromString (buf);
}

static PyObject *
pylcm_new (PyTypeObject *type, PyObject *args, PyObject *kwds)
{
	PyObject *new;

	new = type->tp_alloc (type, 0);
	if (new != NULL) {
		 ((PyLCMObject*)new)->lcm = NULL;
         ((PyLCMObject*)new)->all_handlers = PyList_New (0);
	}
	return new;
}

static void
pylcm_dealloc (PyLCMObject *s)
{
    dbg ("pylcm_dealloc\n");
    if (s->lcm) {
        lcm_destroy (s->lcm);
        s->lcm = NULL;
    }
    Py_DECREF (s->all_handlers);
    s->ob_type->tp_free ((PyObject*)s);
}

static int
pylcm_initobj (PyObject *self, PyObject *args, PyObject *kwargs)
{
    dbg ("%s %p\n", __FUNCTION__, self);
    PyLCMObject *s = (PyLCMObject *)self;

    char *url = NULL;

    if (!PyArg_ParseTuple (args, "|s", &url))
        return -1;

    lcm_t *lcm = lcm_create (url);
    if (! lcm) {
        PyErr_SetString (PyExc_RuntimeError, "Couldn't create LCM");
        return -1;
    }

    init_lcmobject (s, lcm);

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
    (reprfunc)pylcm_repr,          /* tp_repr */
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
