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

#include <lc.h>

int test_count = 0;
int test_count2 = 0;
int catchall_count = 0;

int test_handler (const lc_recv_buf_t *rbuf, void *u)
{
    printf ("handling [%s] msg (content: %s)\n", rbuf->channel, rbuf->data);
    test_count++;
    return 0;
}

int test_handler2 (const lc_recv_buf_t *rbuf, void *u)
{
    printf ("handling [%s] msg (content: %s)\n", rbuf->channel, rbuf->data);
    test_count2++;
    return 0;
}

int catchall_handler (const lc_recv_buf_t *rbuf, void *u)
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
    
    lc_params_t lc_args;
    lc_params_init_defaults (&lc_args);
//    lc_args.local_iface = inet_addr ("192.168.0.2");
//  lc_args.local_iface = INADDR_ANY;
//  lc_args.mc_addr = inet_addr ("225.0.0.3");
//  lc_args.mc_port = htons (2006);

    lc_t *lc = lc_create ();
    if (! lc) {
        fprintf (stderr, "couldn't allocate lc_t\n");
        return 1;
    }
    status = lc_init (lc, &lc_args);
    if (0 != status) {
        fprintf (stderr, "error initializing lc context\n");
        return 1;
    }

    lc_subscribe (lc, "TEST", test_handler, 0);
    lc_subscribe (lc, "TEST", test_handler2, 0);
    lc_unsubscribe_by_func ( lc, "TEST", test_handler, 0);
//    lc_subscribe (lc, ".*", catchall_handler, 0);

    int fd = lc_get_fileno (lc);

    int ntransmitted = 0;

    while (ntransmitted < 5) {
        struct timeval timeout = { 1, 0 };
        fd_set readfds;
        FD_ZERO (&readfds);
        FD_SET (fd,&readfds);

        status=select (fd + 1,&readfds,0,0,&timeout);
        if (0 == status) {
            printf ("sending\n");
            lc_publish (lc, "TEST", payload, datalen);
            lc_publish (lc, "12345", payload, datalen);
            ntransmitted++;
        } else if (FD_ISSET (fd,&readfds)) {
            lc_handle (lc);
            printf ("=====================================================\n");
        }
    }

    lc_destroy (lc);

    printf ("transmitted: %d\n", ntransmitted);
    printf ("test: %d test2: %d catchall: %d\n", 
            test_count, test_count2, catchall_count);

    free (payload);
    
    return 0;
}
