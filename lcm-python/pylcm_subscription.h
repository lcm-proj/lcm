#ifndef __pylcm_subscription_h__
#define __pylcm_subscription_h__

#include <Python.h>
#include <lcm/lcm.h>

#include "pylcm.h"

#ifdef __cplusplus
extern "C" {
#endif

typedef struct {
    PyObject_HEAD

    lcm_subscription_t *subscription;

    PyObject *handler;
    PyLCMObject *lcm_obj;
} PyLCMSubscriptionObject;

extern PyTypeObject pylcm_subscription_type;

#ifdef __cplusplus
}
#endif

#endif
