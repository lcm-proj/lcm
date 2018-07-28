#include <Python.h>
#include "pylcm_subscription.h"

//#define dbg(...) fprintf (stderr, __VA_ARGS__)
#define dbg(...)

// to support python 2.5 and earlier
#ifndef Py_TYPE
#define Py_TYPE(ob) (((PyObject *) (ob))->ob_type)
#endif

extern PyTypeObject pylcmeventlog_type;
extern PyTypeObject pylcm_type;
extern PyTypeObject pylcm_subscription_type;

/* module initialization */

static PyMethodDef lcmmod_methods[] = {
    {NULL, NULL}, /* sentinel */
};
PyDoc_STRVAR(lcmmod_doc, "LCM python extension modules");

// macro to make module init portable between python 2 and 3
#if PY_MAJOR_VERSION >= 3
#define MOD_DEF(ob, name, doc, methods)                \
    static struct PyModuleDef moduledef = {            \
        PyModuleDef_HEAD_INIT, name, doc, -1, methods, \
    };                                                 \
    ob = PyModule_Create(&moduledef);
#else
#define MOD_DEF(ob, name, doc, methods) ob = Py_InitModule3(name, methods, doc);
#endif

#if __GNUC__ >= 4 || defined(__clang__)
__attribute__((visibility("default")))
#endif
PyMODINIT_FUNC
#if PY_MAJOR_VERSION >= 3
PyInit__lcm(void)
#else
init_lcm(void)
#endif
{
    PyObject *m;

    Py_TYPE(&pylcmeventlog_type) = &PyType_Type;
    Py_TYPE(&pylcm_type) = &PyType_Type;
    Py_TYPE(&pylcm_subscription_type) = &PyType_Type;

    MOD_DEF(m, "_lcm", lcmmod_doc, lcmmod_methods);

    Py_INCREF(&pylcmeventlog_type);
    if (PyModule_AddObject(m, "EventLog", (PyObject *) &pylcmeventlog_type) != 0) {
#if PY_MAJOR_VERSION >= 3
        return NULL;  // in python 3 return NULL on error
#else
        return;
#endif
    }

    Py_INCREF(&pylcm_type);
    if (PyModule_AddObject(m, "LCM", (PyObject *) &pylcm_type) != 0) {
#if PY_MAJOR_VERSION >= 3
        return NULL;  // in python 3 return NULL on error
#else
        return;
#endif
    }

    Py_INCREF(&pylcm_subscription_type);
    if (PyModule_AddObject(m, "LCMSubscription", (PyObject *) &pylcm_subscription_type) != 0) {
#if PY_MAJOR_VERSION >= 3
        return NULL;  // in python 3 return NULL on error
#else
        return;
#endif
    }
#if PY_MAJOR_VERSION >= 3
    return m;  // in python 3, we must return the PyObject
#endif
}
