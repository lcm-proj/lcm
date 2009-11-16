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

typedef struct _lcm_eventlog_t lcm_eventlog_t;
struct _lcm_eventlog_t
{
    FILE *f;
    int64_t eventcount;
};

typedef struct _lcm_eventlog_event_t lcm_eventlog_event_t;
struct _lcm_eventlog_event_t {
    int64_t eventnum, timestamp;
    int32_t channellen, datalen;

    char     *channel;
    void     *data;
};

// mode must be "r" or "w"
LCM_API_FUNCTION
lcm_eventlog_t *lcm_eventlog_create(const char *path, const char *mode);

// read the next event; free the returned structure with log_free_event
LCM_API_FUNCTION
lcm_eventlog_event_t *lcm_eventlog_read_next_event(lcm_eventlog_t *l);

// free a structure returned by read_next_event.
LCM_API_FUNCTION
void lcm_eventlog_free_event(lcm_eventlog_event_t *le);

// seek (approximately) to particular timestamp
LCM_API_FUNCTION
int lcm_eventlog_seek_to_timestamp(lcm_eventlog_t *l, int64_t ts);

// eventnum will be filled in for you
LCM_API_FUNCTION
int lcm_eventlog_write_event(lcm_eventlog_t *l, lcm_eventlog_event_t *le);

// when you're done with the log, clean up after yourself!
LCM_API_FUNCTION
void lcm_eventlog_destroy(lcm_eventlog_t *l);

#ifdef __cplusplus
}
#endif

#endif
