#include <stdio.h>
#include <stdlib.h>
#ifndef WIN32
#include <signal.h>
#endif
#include <lcm/lcm.h>

static int g_messages_received = 0;

#ifndef WIN32
void on_signal(int signum)
{
    printf("received %d messages\n", g_messages_received);
    exit(0);
}
#endif

void catchall_handler(const lcm_recv_buf_t *rbuf, const char *channel, void *u)
{
    printf("received [%s] (content: %s)\n", channel, (char *) rbuf->data);
    g_messages_received++;
}

int main(int argc, char **argv)
{
#ifndef WIN32
    signal(SIGINT, on_signal);
#endif

    lcm_t *lcm = lcm_create(NULL);
    if (!lcm) {
        fprintf(stderr, "couldn't initialize LCM\n");
        return 1;
    }
    lcm_subscribe(lcm, ".*", catchall_handler, NULL);

    while (0 == lcm_handle(lcm)) {
        // Do nothing
    }

    lcm_destroy(lcm);

    return 0;
}
