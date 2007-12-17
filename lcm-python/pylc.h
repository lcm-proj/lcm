#ifndef __pylc_h__
#define __pylc_h__

#ifdef __cplusplus
extern "C" {
#endif
#include <Python.h>
#include <lc/lc.h>

typedef struct {
    PyObject_HEAD

    lc_t *lc;

    PyObject *all_handlers;

    int exception_raised;
} PyLCObject;

#ifdef __cplusplus
}
#endif

#endif
