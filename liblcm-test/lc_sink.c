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

int catchall_handler (const lc_recv_buf_t *rbuf, void *u)
{
    printf("catchall handler [%s] (content: %s)\n", rbuf->channel, rbuf->data);
    return 0;
}

int main(int argc, char **argv)
{
    int status;

    lc_params_t lc_args;
    lc_params_init_defaults (&lc_args);

    lc_t *lc = lc_create();
    if (! lc) {
        fprintf(stderr, "couldn't allocate lc_t\n");
        return 1;
    }
    status = lc_init (lc, &lc_args);
    if (0 != status) {
        fprintf(stderr, "error initializing lc context\n");
        return 1;
    }
    lc_subscribe (lc, ".*", catchall_handler, NULL);

    while(1) {
		lc_handle (lc);
   }

    lc_destroy (lc);
    
    return 0;
}
