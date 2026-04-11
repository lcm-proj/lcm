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

    // Set while LCM.handle() or LCM.handle_timeout() is active on this LCM.
    // Read and written only while holding the GIL -- used for the
    // "simultaneous calls" check. The actual suspended PyThreadState* is
    // stored in thread-specific storage (see pylcm.c) so that
    // pylcm_msg_handler can restore the GIL without dereferencing
    // subs_obj->lcm_obj (which races with pylcm_unsubscribe).
    int handle_in_progress;

    PyObject *all_handlers;
} PyLCMObject;

#ifdef __cplusplus
}
#endif

#endif
