#include "pylcm_subscription.h"

static void
_dealloc (PyLCMSubscriptionObject *s)
{
    if (s->handler) {
        Py_DECREF (s->handler);
    }
    printf ("dealloc!\n");
    // ignore s->subscription and s->lcm_obj
    s->ob_type->tp_free ((PyObject*)s);
}

static PyObject *
_new (PyTypeObject *type, PyObject *args, PyObject *kwds)
{
	PyObject *obj = type->tp_alloc (type, 0);
    printf ("new!!!\n");
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
    "LCM.LCMSubscription",            /* tp_name */
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
    0,                  /* tp_doc */
    0,                  /* tp_traverse */
    0,                  /* tp_clear */
    0,                  /* tp_richcompare */
    0,                  /* tp_weaklistoffset */
    0,                  /* tp_iter */
    0,                  /* tp_iternext */
    0,                  /* tp_methods */
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
