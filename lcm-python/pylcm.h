#ifndef __pylcm_h__
#define __pylcm_h__

#ifdef __cplusplus
extern "C" {
#endif
#include <Python.h>
#include <lcm/lcm.h>

typedef struct {
    PyObject_HEAD

    lcm_t *lcm;

    int exception_raised;

    PyObject *all_handlers;

    // The stored thread state, so that we can do the equivalent of
    // Py_BEGIN_ALLOW_THREADS and Py_END_ALLOW_THREADS in different functions.
    PyThreadState *saved_thread_state;
} PyLCMObject;

#ifdef __cplusplus
}
#endif

#endif
