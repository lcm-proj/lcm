#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <glib.h>
#include <lcm/lcm.h>

lcm_t *lcm = NULL;
lcm_subscription_t *subscription = NULL;
int num_received = 0;
int msg_id = 0;
int queue_capacity = 10;

static void on_buftest(const lcm_recv_buf_t *rbuf, const char *channel, void *user_data)
{
    num_received++;
    msg_id = atoi(rbuf->data);
    printf("received msg id: %4d (recv count: %4d)\n", msg_id, num_received);
    if (num_received % 10 == 0) {
        g_usleep(1000 * 1000);
        printf("sleeping for 1000 ms\n");
    }
}

int main(int argc, char **argv)
{
    if (argc > 1) {
        if (!strcmp(argv[1], "-h") || !strcmp(argv[1], "--help")) {
            printf("usage: buftest-receiver [queue-capacity]\n");
            return 1;
        } else {
            queue_capacity = atoi(argv[1]);
        }
    }

    printf("queue capacity: %d\n", queue_capacity);

    lcm = lcm_create(NULL);

    subscription = lcm_subscribe(lcm, "BUFTEST", on_buftest, NULL);
    lcm_subscription_set_queue_capacity(subscription, queue_capacity);

    while (lcm_handle(lcm) == 0) {
        // handle messages
        if (msg_id >= 9000)
            break;
    }

    return 0;
}
