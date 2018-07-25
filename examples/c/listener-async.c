// file: listener-async.c
//
// This program demonstrates how to use LCM in a select-based event loop where
// the program has other things to do while waiting for LCM messages (e.g.,
// periodically transmit messages of its own).
//
// To use LCM in another event loop (e.g., the GTK+ or QT event loops), see the
// API documentation on how to use file descriptors with the corresponding
// event loop.
//
// compile with:
//  $ gcc -o listener-async listener-async.c -llcm
//
// On a system with pkg-config, you can also use:
//  $ gcc -o listener-async listener-async.c `pkg-config --cflags --libs lcm`

#include <inttypes.h>
#include <lcm/lcm.h>
#include <stdio.h>
#include <sys/select.h>
#include "exlcm_example_t.h"

static void my_handler(const lcm_recv_buf_t *rbuf, const char *channel, const exlcm_example_t *msg,
                       void *user)
{
    int i;
    printf("Received message on channel \"%s\":\n", channel);
    printf("  timestamp   = %" PRId64 "\n", msg->timestamp);
    printf("  position    = (%f, %f, %f)\n", msg->position[0], msg->position[1], msg->position[2]);
    printf("  orientation = (%f, %f, %f, %f)\n", msg->orientation[0], msg->orientation[1],
           msg->orientation[2], msg->orientation[3]);
    printf("  ranges:");
    for (i = 0; i < msg->num_ranges; i++)
        printf(" %d", msg->ranges[i]);
    printf("\n");
    printf("  name        = '%s'\n", msg->name);
    printf("  enabled     = %d\n", msg->enabled);
}

int main(int argc, char **argv)
{
    lcm_t *lcm;

    lcm = lcm_create(NULL);
    if (!lcm)
        return 1;

    exlcm_example_t_subscription_t *sub =
        exlcm_example_t_subscribe(lcm, "EXAMPLE", &my_handler, NULL);

    while (1) {
        // setup the LCM file descriptor for waiting.
        int lcm_fd = lcm_get_fileno(lcm);
        fd_set fds;
        FD_ZERO(&fds);
        FD_SET(lcm_fd, &fds);

        // wait a limited amount of time for an incoming message
        struct timeval timeout = {
            1,  // seconds
            0   // microseconds
        };
        int status = select(lcm_fd + 1, &fds, 0, 0, &timeout);

        if (0 == status) {
            // no messages
            printf("waiting for message\n");
        } else if (FD_ISSET(lcm_fd, &fds)) {
            // LCM has events ready to be processed.
            lcm_handle(lcm);
        }
    }

    exlcm_example_t_unsubscribe(lcm, sub);
    lcm_destroy(lcm);
    return 0;
}
