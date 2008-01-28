#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include <sys/types.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <fcntl.h>
#include <netdb.h>

#include <sys/time.h>
#include <time.h>
#include <sys/select.h>

#include <lcm/lcm.h>

int test_count = 0;
int test_count2 = 0;
int catchall_count = 0;

int test_handler (const lcm_recv_buf_t *rbuf, void *u)
{
    printf ("handling [%s] msg (content: %s)\n", rbuf->channel, rbuf->data);
    test_count++;
    return 0;
}

int test_handler2 (const lcm_recv_buf_t *rbuf, void *u)
{
    printf ("handling [%s] msg (content: %s)\n", rbuf->channel, rbuf->data);
    test_count2++;
    return 0;
}

int catchall_handler (const lcm_recv_buf_t *rbuf, void *u)
{
    catchall_count++;
    return 0;
}

int main (int argc, char **argv)
{
    int status;

    char *words[] = { "foo", "bar", "baz", "cow", "bat", "hello", "world", 0 };
    srand (time (NULL));
    int nwords;
    for (nwords = 0; words[nwords] != NULL; nwords++);
    int windex = (int) (nwords * (rand () / (RAND_MAX+1.0)));
    
    printf ("windex: %d\n", windex);
    int datalen = strlen (words[windex]) + 1;
    char *payload = malloc (datalen);
    strcpy (payload, words[windex]);
    
    lcm_t *lcm = lcm_create ("udpm://");
    if (! lcm) {
        fprintf (stderr, "couldn't allocate lcm_t\n");
        return 1;
    }

    lcm_subscribe (lcm, "TEST", test_handler, 0);
    lcm_subscribe (lcm, "TEST", test_handler2, 0);
    lcm_unsubscribe_by_func ( lcm, test_handler, 0);
//    lcm_subscribe (lcm, ".*", catchall_handler, 0);

    int fd = lcm_get_fileno (lcm);

    int ntransmitted = 0;

    while (ntransmitted < 5) {
        struct timeval timeout = { 1, 0 };
        fd_set readfds;
        FD_ZERO (&readfds);
        FD_SET (fd,&readfds);

        status=select (fd + 1,&readfds,0,0,&timeout);
        if (0 == status) {
            printf ("sending\n");
            lcm_publish (lcm, "TEST", payload, datalen);
            lcm_publish (lcm, "12345", payload, datalen);
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

    free (payload);
    
    return 0;
}
