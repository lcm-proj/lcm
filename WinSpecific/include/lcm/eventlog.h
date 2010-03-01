#ifndef _LCM_EVENTLOG_H_
#define _LCM_EVENTLOG_H_

#include <stdio.h>
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

#ifndef LCM_API_FUNCTION
#ifdef WIN32
#define LCM_API_FUNCTION __declspec(dllexport)
#else 
#define LCM_API_FUNCTION
#endif // WIN32
#endif // LCM_API_FUNCTION

/**
 * SECTION:eventlog
 * @short_description: Read and write LCM log files
 *
 */

typedef struct _lcm_eventlog_t lcm_eventlog_t;
struct _lcm_eventlog_t
{
    FILE *f;
    int64_t eventcount;
};

typedef struct _lcm_eventlog_event_t lcm_eventlog_event_t;
struct _lcm_eventlog_event_t {
    int64_t eventnum; 
    int64_t timestamp;
    int32_t channellen;
    int32_t datalen;

    char     *channel;
    void     *data;
};

/**
 * lcm_eventlog_create:
 * @path: Log file to open
 * @mode: must be "r" or "w"
 *
 * Open a log file for reading (mode = "r"), or writing (mode = "w")
 *
 * Returns: a newly allocated lcm_eventlog_t, or NULL on failure.
 */
LCM_API_FUNCTION
lcm_eventlog_t *lcm_eventlog_create(const char *path, const char *mode);

/** 
 * lcm_eventlog_read_next_event:
 * @eventlog: The log file
 *
 * Read the next event in the log file.  Valid in read mode only.
 *
 * Returns: the next event in the log file; free the returned structure with
 * lcm_eventlog_free_event()
 */
LCM_API_FUNCTION
lcm_eventlog_event_t *lcm_eventlog_read_next_event(lcm_eventlog_t *eventlog);

/**
 * lcm_eventlog_free_event:
 * @le: A structure returned by lcm_eventlog_read_next_event()
 *
 * free a structure returned by read_next_event.
 */
LCM_API_FUNCTION
void lcm_eventlog_free_event(lcm_eventlog_event_t *le);

/**
 * lcm_eventlog_seek_to_timestamp:
 * @eventlog: The log file
 * @ts: Timestamp of the target event in the log file.
 *
 * seek (approximately) to a particular timestamp
 */
LCM_API_FUNCTION
int lcm_eventlog_seek_to_timestamp(lcm_eventlog_t *eventlog, int64_t ts);

/**
 * lcm_eventlog_write_event:
 * @eventlog: The log file
 * @le: The event to write to the file
 *
 * Write an event into a log file.  Valid in write mode only.
 *
 * eventnum will be filled in for you
 */
LCM_API_FUNCTION
int lcm_eventlog_write_event(lcm_eventlog_t *eventlog, lcm_eventlog_event_t *le);

/**
 * lcm_eventlog_destroy:
 * @eventlog: The log file
 *
 * when you're done with the log, clean up after yourself!
 */
LCM_API_FUNCTION
void lcm_eventlog_destroy(lcm_eventlog_t *eventlog);

#ifdef __cplusplus
}
#endif

#endif
