#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#ifdef WIN32
#include <winsock2.h>
#else
#include <sys/select.h>
#endif

#include <fcntl.h>

#include <lcm/lcm.h>

int test_count = 0;
int test_count2 = 0;
int catchall_count = 0;

void test_handler (const lcm_recv_buf_t *rbuf, const char *channel, void *u)
{
    printf ("handling [%s] msg (content: %s)\n", channel, (char*)rbuf->data);
    test_count++;
}

void test_handler2 (const lcm_recv_buf_t *rbuf, const char *channel, void *u)
{
    printf ("handling [%s] msg (content: %s)\n", channel, (char*)rbuf->data);
    test_count2++;
}

void catchall_handler (const lcm_recv_buf_t *rbuf, const char *channel, void *u)
{
    catchall_count++;
}

int main (int argc, char **argv)
{
    char *data = "payload";
    int datalen = strlen(data) + 1;
    
    lcm_t *lcm = lcm_create (NULL);
    if (! lcm) {
        fprintf (stderr, "couldn't allocate lcm_t\n");
        return 1;
    }

    lcm_subscription_t * s = lcm_subscribe (lcm, "TEST", test_handler, 0);
    lcm_subscribe (lcm, "TEST", test_handler2, 0);
    lcm_unsubscribe ( lcm, s);
//    lcm_subscribe (lcm, ".*", catchall_handler, 0);

    int fd = lcm_get_fileno (lcm);

    int ntransmitted = 0;

    while (ntransmitted < 5) {
        struct timeval timeout = { 1, 0 };
        fd_set readfds;
        FD_ZERO (&readfds);
        FD_SET (fd,&readfds);

        int status=select (fd + 1,&readfds,0,0,&timeout);
        if (0 == status) {
            printf ("sending\n");
            lcm_publish (lcm, "TEST", data, datalen);
            lcm_publish (lcm, "12345", data, datalen);
            ntransmitted++;
        } else if (FD_ISSET (fd,&readfds)) {
            lcm_handle (lcm);
            printf ("=====================================================\n");
        }
    }

    lcm_destroy (lcm);

    printf ("transmitted: %d\n", ntransmitted);
    printf ("test: %d test2: %d catchall: %d\n", 
            test_count, test_count2, catchall_count);

    return 0;
}
