#include <Python.h>

#include <lcm/eventlog.h>
#ifdef WIN32
#include <lcm/windows/WinPorting.h>
#endif

typedef struct {
    PyObject_HEAD

    lcm_eventlog_t *eventlog;
    char mode;
} PyLogObject;

PyDoc_STRVAR(pylog_doc,
"Event Log parser\n\
");

//gives redefinition error in MSVC
//PyTypeObject pylcmeventlog_type; 

static PyObject *
pylog_close (PyLogObject *self)
{
    if (self->eventlog) {
        lcm_eventlog_destroy (self->eventlog);
        self->eventlog = NULL;
    }
    Py_INCREF (Py_None);
    return Py_None;
}

static PyObject *
pylog_read_next_event (PyLogObject *self)
{
    if (!self->eventlog) {
        PyErr_SetString (PyExc_ValueError, "event log already closed");
        return NULL;
    }

    if (self->mode != 'r') {
        PyErr_SetString (PyExc_RuntimeError, 
                "reading not allowed in write mode");
        return NULL;
    }

    lcm_eventlog_event_t *next_event = 
        lcm_eventlog_read_next_event (self->eventlog);
    if (!next_event) {
        Py_INCREF (Py_None);
        return Py_None;
    }

    PyObject *result = Py_BuildValue ("LLs#s#", 
            next_event->eventnum,
            next_event->timestamp,
            next_event->channel, next_event->channellen,
            next_event->data, next_event->datalen);

    lcm_eventlog_free_event (next_event);

    return result;
}

static PyObject *
pylog_seek (PyLogObject *self, PyObject *arg)
{
    int64_t offset = PyLong_AsLongLong (arg);
    if (PyErr_Occurred ()) return 0;

    if (!self->eventlog) {
        PyErr_SetString (PyExc_ValueError, "event log already closed");
        return NULL;
    }

    if (self->mode != 'r') {
        PyErr_SetString (PyExc_RuntimeError, 
                "seeking not allowed in write mode");
        return NULL;
    }

    fseek (self->eventlog->f, offset, SEEK_SET);

    Py_INCREF (Py_None);
    return Py_None;
}

static PyObject *
pylog_seek_to_timestamp (PyLogObject *self, PyObject *arg)
{
    int64_t timestamp = PyLong_AsLongLong (arg);
    if (PyErr_Occurred ()) return 0;

    if (!self->eventlog) {
        PyErr_SetString (PyExc_ValueError, "event log already closed");
        return NULL;
    }

    if (self->mode != 'r') {
        PyErr_SetString (PyExc_RuntimeError, 
                "seeking not allowed in write mode");
        return NULL;
    }

    if (0 == lcm_eventlog_seek_to_timestamp(self->eventlog, timestamp)) {
        Py_INCREF (Py_None);
        return Py_None;
    } else {
        PyErr_SetFromErrno (PyExc_IOError);
        return Py_None;
    }
}

static PyObject *
pylog_write_next_event (PyLogObject *self, PyObject *args)
{
    int64_t utime = 0;
    char *channel = NULL;
    int channellen = 0;

    // TODO use a buffer object instead of a string
    uint8_t *data = NULL;
    int datalen = 0;

    if (!PyArg_ParseTuple(args, "Ls#s#", 
                &utime, &channel, &channellen, &data, &datalen)) {
        return NULL;
    }

    if (!self->eventlog) {
        PyErr_SetString (PyExc_ValueError, "event log already closed");
        return NULL;
    }

    if (self->mode != 'w') {
        PyErr_SetString (PyExc_RuntimeError, 
                "writing not allowed in read mode");
        return NULL;
    }

    lcm_eventlog_event_t le; //msvc needs init of all fields seperately
    le.eventnum = 0;
    le.timestamp = utime;
    le.channellen = channellen;
    le.datalen = datalen;
    le.channel = channel;
    le.data = data;
    

    if (0 != lcm_eventlog_write_event (self->eventlog, &le)) {
        PyErr_SetFromErrno (PyExc_IOError);
        return NULL;
    }

    Py_INCREF (Py_None);
    return Py_None;
}

static PyObject *
pylog_size (PyLogObject *self)
{
    struct stat sbuf;
    if (0 != fstat (fileno (self->eventlog->f), &sbuf)) {
        PyErr_SetFromErrno (PyExc_IOError);
        return NULL;
    }
    return PyLong_FromLongLong (sbuf.st_size);
}

static PyObject *
pylog_ftell (PyLogObject *self)
{
    return PyLong_FromLongLong (ftello(self->eventlog->f));
}

static PyMethodDef pylog_methods[] = {
    { "close", (PyCFunction)pylog_close, METH_NOARGS, "" },
    { "seek", (PyCFunction)pylog_seek, METH_O, "" },
    { "seek_to_timestamp", (PyCFunction)pylog_seek_to_timestamp, METH_O, "" },
    { "read_next_event", (PyCFunction)pylog_read_next_event, METH_NOARGS, "" },
    { "write_event", (PyCFunction)pylog_write_next_event, METH_VARARGS, "" },
    { "size", (PyCFunction)pylog_size, METH_NOARGS, "" },
    { "ftell", (PyCFunction)pylog_ftell, METH_NOARGS, "" },
    { NULL, NULL }
};

// ==================== class administrative methods ====================

static PyObject *
pylog_repr(PyLogObject *s)
{
    char buf[512];
    PyOS_snprintf(buf, sizeof(buf),
                "<Log object ... TODO>");
    return PyString_FromString(buf);
}

static PyObject *
pylog_new(PyTypeObject *type, PyObject *args, PyObject *kwds)
{
	//msvc does not allow usage of new as variable name
	PyObject *newobj;

	newobj = type->tp_alloc(type, 0);
	if (newobj != NULL) {
		((PyLogObject *)newobj)->eventlog = NULL;
        ((PyLogObject *)newobj)->mode = 0;
    }
	return newobj;
}

static void
pylog_dealloc(PyLogObject *self)
{
    if (self->eventlog) {
        lcm_eventlog_destroy (self->eventlog);
    }
    self->ob_type->tp_free((PyObject*)self);
}

static int
pylog_initobj(PyObject *s, PyObject *args, PyObject *kwds)
{
    PyLogObject *self = (PyLogObject *)s;
    static char *keywords[] = { "filename", "mode", 0 };
    char *filename = NULL;
    char *mode = "r";

    if (!PyArg_ParseTupleAndKeywords(args, kwds, "s|s", keywords, &filename,
                &mode))
        return -1;

    if (!strcmp (mode, "r")) {
        self->mode = 'r';
    } else if (!strcmp (mode, "w")) {
        self->mode = 'w';
    } else {
        PyErr_SetString (PyExc_ValueError, "invalid mode");
        return -1;
    }

    if (self->eventlog) { lcm_eventlog_destroy (self->eventlog); }

    self->eventlog = lcm_eventlog_create (filename, mode);
    if (!self->eventlog) {
        PyErr_SetFromErrno (PyExc_IOError);
        return -1;
    }

    return 0;
}

/* Type object */
PyTypeObject pylcmeventlog_type = {
    PyObject_HEAD_INIT(0)   /* Must fill in type value later */
    0,                  /* ob_size */
    "EventLog",            /* tp_name */
    sizeof(PyLogObject),     /* tp_basicsize */
    0,                  /* tp_itemsize */
    (destructor)pylog_dealloc,     /* tp_dealloc */
    0,                  /* tp_print */
    0,                  /* tp_getattr */
    0,                  /* tp_setattr */
    0,                  /* tp_compare */
    (reprfunc)pylog_repr,          /* tp_repr */
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
    pylog_doc,               /* tp_doc */
    0,                  /* tp_traverse */
    0,                  /* tp_clear */
    0,                  /* tp_richcompare */
    0,                  /* tp_weaklistoffset */
    0,                  /* tp_iter */
    0,                  /* tp_iternext */
    pylog_methods,               /* tp_methods */
    0,                  /* tp_members */
    0,                  /* tp_getset */
    0,                  /* tp_base */
    0,                  /* tp_dict */
    0,                  /* tp_descr_get */
    0,                  /* tp_descr_set */
    0,                  /* tp_dictoffset */
    pylog_initobj,             /* tp_init */
    PyType_GenericAlloc,            /* tp_alloc */
    pylog_new,             /* tp_new */
    PyObject_Del,               /* tp_free */
};

