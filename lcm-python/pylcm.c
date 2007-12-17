#include <Python.h>

//#define dbg(...) fprintf (stderr, __VA_ARGS__)
#define dbg(...)

extern PyTypeObject pylcmeventlog_type;
extern PyTypeObject pylc_type;

/* module initialization */

static PyMethodDef lcmmod_methods[] = {
    { NULL, NULL } /* sentinel */
};
PyDoc_STRVAR(lcmmod_doc, "LCM python extension modules");

PyMODINIT_FUNC
init_lcm(void)
{
    PyObject *m;

    pylcmeventlog_type.ob_type = &PyType_Type;
    pylc_type.ob_type = &PyType_Type;

    m = Py_InitModule3("_lcm", lcmmod_methods, lcmmod_doc);

    Py_INCREF((PyObject *)&pylcmeventlog_type);
    if (PyModule_AddObject(m, "Eventlog", 
                (PyObject *)&pylcmeventlog_type) != 0) {
        return;
    }

    Py_INCREF ((PyObject *)&pylc_type);
    if (PyModule_AddObject (m, "LC", (PyObject *)&pylc_type) != 0) {
        return;
    }
}

