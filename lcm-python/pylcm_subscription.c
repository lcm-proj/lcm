#include "pylcm_subscription.h"

#ifndef Py_RETURN_NONE
#define Py_RETURN_NONE  do { Py_INCREF( Py_None ); return Py_None; } while(0)
#endif

PyDoc_STRVAR (_class_doc,
"The LCMSubscription class represents a single subscription of a message\n\
handler to an LCM channel.\n\
\n\
This class should never be instantiated by the programmer.\n\
\n\
@undocumented: __new__, __getattribute__, __init__\n\
");

// =============== LCMSubscription class methods ==============

static PyObject *
_set_queue_capacity (PyLCMSubscriptionObject *sobj, PyObject *arg)
{
    int num_messages = PyInt_AsLong(arg);
    if (num_messages == -1 && PyErr_Occurred())
        return NULL;

    int status;
    Py_BEGIN_ALLOW_THREADS
    status = lcm_subscription_set_queue_capacity (sobj->subscription, num_messages);
    Py_END_ALLOW_THREADS

    if (0 != status) {
        PyErr_SetFromErrno (PyExc_IOError);
        return NULL;
    }

    Py_RETURN_NONE;
}
PyDoc_STRVAR (pylcm_set_queue_capacity_doc,
"set_queue_capacity(num_messages) -> None\n\
Sets the maximum number of received but unhandled messages to queue for this\n\
subscription.  If messages start arriving faster than they are handled, then\n\
they will be discarded after more than this number start piling up.\n\
\n\
@param num_messages: Maximum number of messages to queue for this subscription.\n\
A number less than or equal to zero indicates no limit (very dangerous!).\n\
");

static PyMethodDef _methods[] = {
    { "set_queue_capacity", (PyCFunction)_set_queue_capacity, METH_O, pylcm_set_queue_capacity_doc },
    { NULL, NULL }
};

// ==================== class administrative methods ====================

static void
_dealloc (PyLCMSubscriptionObject *s)
{
    if (s->handler) {
        Py_DECREF (s->handler);
        s->handler = NULL;
    }
    // ignore s->subscription and s->lcm_obj
    s->ob_type->tp_free ((PyObject*)s);
}

static PyObject *
_new (PyTypeObject *type, PyObject *args, PyObject *kwds)
{
	PyObject *obj = type->tp_alloc (type, 0);
	return obj;
}

static int
_init (PyObject *self, PyObject *args, PyObject *kwargs)
{
    PyLCMSubscriptionObject *s = (PyLCMSubscriptionObject *)self;
    s->subscription = NULL;
    s->handler = NULL;
    s->lcm_obj = NULL;
    return 0;
}

/* Type object for socket objects. */
PyTypeObject pylcm_subscription_type = {
    PyObject_HEAD_INIT (0)   /* Must fill in type value later */
    0,                  /* ob_size */
    "LCMSubscription",            /* tp_name */
    sizeof (PyLCMSubscriptionObject),     /* tp_basicsize */
    0,                  /* tp_itemsize */
    (destructor)_dealloc,     /* tp_dealloc */
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
    _class_doc,                  /* tp_doc */
    0,                  /* tp_traverse */
    0,                  /* tp_clear */
    0,                  /* tp_richcompare */
    0,                  /* tp_weaklistoffset */
    0,                  /* tp_iter */
    0,                  /* tp_iternext */
    _methods,           /* tp_methods */
    0,                  /* tp_members */
    0,                  /* tp_getset */
    0,                  /* tp_base */
    0,                  /* tp_dict */
    0,                  /* tp_descr_get */
    0,                  /* tp_descr_set */
    0,                  /* tp_dictoffset */
    _init,                  /* tp_init */
    PyType_GenericAlloc,            /* tp_alloc */
    _new,             /* tp_new */
    PyObject_Del,               /* tp_free */
};
