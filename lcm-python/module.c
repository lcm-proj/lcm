#include <Python.h>
#include "pylcm_subscription.h"

//#define dbg(...) fprintf (stderr, __VA_ARGS__)
#define dbg(...)

extern PyTypeObject pylcmeventlog_type;
extern PyTypeObject pylcm_type;
extern PyTypeObject pylcm_subscription_type;

/* module initialization */

static PyMethodDef lcmmod_methods[] = {
    { NULL, NULL } /* sentinel */
};
PyDoc_STRVAR (lcmmod_doc, "LCM python extension modules");

PyMODINIT_FUNC
init_lcm (void)
{
    PyObject *m;

    pylcmeventlog_type.ob_type = &PyType_Type;
    pylcm_type.ob_type = &PyType_Type;
    pylcm_subscription_type.ob_type = &PyType_Type;

    m = Py_InitModule3 ("_lcm", lcmmod_methods, lcmmod_doc);

    Py_INCREF (&pylcmeventlog_type);
    if (PyModule_AddObject (m, "EventLog", 
                (PyObject *)&pylcmeventlog_type) != 0) {
        return;
    }

    Py_INCREF (&pylcm_type);
    if (PyModule_AddObject (m, "LCM", (PyObject *)&pylcm_type) != 0) {
        return;
    }

    Py_INCREF (&pylcm_subscription_type);
    if (PyModule_AddObject (m, "LCMSubscription", 
                (PyObject *)&pylcm_subscription_type) != 0) {
        return;
    }
}
