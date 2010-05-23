#include <stdio.h>

#include <lcm/lcm.h>

void catchall_handler(const lcm_recv_buf_t *rbuf, const char *channel, void *u)
{
    printf("received [%s] (content: %s)\n", channel, (char*)rbuf->data);
}

int main(int argc, char **argv)
{
    lcm_t *lcm = lcm_create(NULL);
    if (! lcm) {
        fprintf(stderr, "couldn't initialize LCM\n");
        return 1;
    }
    lcm_subscribe(lcm, ".*", catchall_handler, NULL);

    while(0 == lcm_handle(lcm)) {
    }

    lcm_destroy(lcm);
    
    return 0;
}
