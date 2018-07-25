// file: read_log.c
//
// LCM example program.  Demonstrates how to read and decode messages directly
// from a log file in C.  It is also possible to use the log file provider --
// see the documentation on lcm_create() for details on that method.
//
// compile with:
//  $ gcc -o read_log read_log.c -llcm
//
// On a system with pkg-config, you can also use:
//  $ gcc -o read_log read_log.c `pkg-config --cflags --libs lcm`

#include <inttypes.h>
#include <lcm/lcm.h>
#include <stdio.h>
#include <string.h>

#include "exlcm_example_t.h"

int main(int argc, char **argv)
{
    lcm_eventlog_t *log;
    if (argc < 2) {
        fprintf(stderr, "usage: read_log <logfile>\n");
        return 1;
    }

    log = lcm_eventlog_create(argv[1], "r");
    if (!log) {
        fprintf(stderr, "couldn't open log file\n");
        return 1;
    }

    while (1) {
        exlcm_example_t msg;
        int i;
        lcm_eventlog_event_t *event = lcm_eventlog_read_next_event(log);

        if (!event)
            break;

        if (0 != strcmp(event->channel, "EXAMPLE"))
            continue;

        exlcm_example_t_decode(event->data, 0, event->datalen, &msg);

        printf("Message:\n");
        printf("  timestamp   = %" PRId64 "\n", msg.timestamp);
        printf("  position    = (%f, %f, %f)\n", msg.position[0], msg.position[1], msg.position[2]);
        printf("  orientation = (%f, %f, %f, %f)\n", msg.orientation[0], msg.orientation[1],
               msg.orientation[2], msg.orientation[3]);
        printf("  ranges:");
        for (i = 0; i < msg.num_ranges; i++)
            printf(" %d", msg.ranges[i]);
        printf("\n");
        printf("  name        = '%s'\n", msg.name);
        printf("  enabled     = %d\n", msg.enabled);

        exlcm_example_t_decode_cleanup(&msg);

        lcm_eventlog_free_event(event);
    }

    lcm_eventlog_destroy(log);
    return 0;
}
