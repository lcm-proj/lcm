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
} PyLCMObject;

#ifdef __cplusplus
}
#endif

#endif
