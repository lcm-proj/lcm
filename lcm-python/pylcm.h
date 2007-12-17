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

    PyObject *all_handlers;

    int exception_raised;
} PyLCMObject;

#ifdef __cplusplus
}
#endif

#endif
