#ifndef _LCM_EVENTLOG_H_
#define _LCM_EVENTLOG_H_

#include <stdint.h>
#include <stdio.h>

#ifdef LCM_PYTHON
#define LCM_EXPORT
#else
#include "lcm_export.h"
#endif

#ifdef __cplusplus
extern "C" {
#endif

/**
 * @defgroup LcmC_lcm_eventlog_t lcm_eventlog_t
 * @ingroup LcmC
 * @brief Read and write %LCM log files
 *
 * @code
 * #include <lcm/lcm.h>
 * @endcode
 *
 * Linking: <tt> `pkg-config --libs lcm` </tt>
 *
 * @{
 */

typedef struct _lcm_eventlog_t lcm_eventlog_t;
struct _lcm_eventlog_t {
    /**
     * The underlying file handle.  Made available for debugging.
     */
    FILE *f;

    /**
     * Internal counter, keeps track of how many events have been written.
     */
    int64_t eventcount;
};

/**
 * Represents a single event (message) in a log file.
 */
typedef struct _lcm_eventlog_event_t lcm_eventlog_event_t;
struct _lcm_eventlog_event_t {
    /**
     * A monotonically increasing number assigned to the message to identify it
     * in the log file.
     */
    int64_t eventnum;
    /**
     * Time that the message was received, in microseconds since the UNIX
     * epoch
     */
    int64_t timestamp;
    /**
     * Length of @c channel, in bytes
     */
    int32_t channellen;
    /**
     * Length of @c data, in bytes
     */
    int32_t datalen;

    /**
     * Channel that the message was received on
     */
    char *channel;
    /**
     * Raw byte buffer containing the message payload.
     */
    void *data;
};

/**
 * Open a log file for reading or writing.
 *
 * @param path Log file to open
 * @param mode "r" (read mode), "w" (write mode), or "a" (append mode)
 *
 * @return a newly allocated lcm_eventlog_t, or NULL on failure.
 */
LCM_EXPORT
lcm_eventlog_t *lcm_eventlog_create(const char *path, const char *mode);

/**
 * Read the next event in the log file.  Valid in read mode only.  Free the
 * returned structure with lcm_eventlog_free_event() after use.
 *
 * @param eventlog The log file object
 *
 * @return the next event in the log file.  Returns NULL when the end of the
 * file has been reached or when invalid data is read.
 */
LCM_EXPORT
lcm_eventlog_event_t *lcm_eventlog_read_next_event(lcm_eventlog_t *eventlog);

/**
 * Free a structure returned by lcm_eventlog_read_next_event().
 *
 * @param event A structure returned by lcm_eventlog_read_next_event()
 */
LCM_EXPORT
void lcm_eventlog_free_event(lcm_eventlog_event_t *event);

/**
 * Seek (approximately) to a particular timestamp.
 *
 * @param eventlog The log file object
 * @param ts Timestamp of the target event in the log file.
 *
 * @return 0 on success, -1 on failure
 */
LCM_EXPORT
int lcm_eventlog_seek_to_timestamp(lcm_eventlog_t *eventlog, int64_t ts);

/**
 * Write an event into a log file.  Valid in write mode only.
 *
 * @param eventlog The log file object
 * @param event The event to write to the file.  On return, the eventnum field
 * will be filled in for you.
 *
 * @return 0 on success, -1 on failure.
 */
LCM_EXPORT
int lcm_eventlog_write_event(lcm_eventlog_t *eventlog, lcm_eventlog_event_t *event);

/**
 * Close a log file and release allocated resources.
 *
 * @param eventlog The log file object
 */
LCM_EXPORT
void lcm_eventlog_destroy(lcm_eventlog_t *eventlog);

/**
 * @}
 */

#ifdef __cplusplus
}
#endif

#endif
