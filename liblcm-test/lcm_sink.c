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

#include <lcm.h>

int catchall_handler (const lcm_recv_buf_t *rbuf, void *u)
{
    printf("catchall handler [%s] (content: %s)\n", rbuf->channel, rbuf->data);
    return 0;
}

int main(int argc, char **argv)
{
    int status;

    lcm_params_t lcm_args;
    lcm_params_init_defaults (&lcm_args);

    lcm_t *lcm = lcm_create();
    if (! lcm) {
        fprintf(stderr, "couldn't allocate lcm_t\n");
        return 1;
    }
    status = lcm_init (lcm, &lcm_args);
    if (0 != status) {
        fprintf(stderr, "error initializing lcm context\n");
        return 1;
    }
    lcm_subscribe (lcm, ".*", catchall_handler, NULL);

    while(1) {
		lcm_handle (lcm);
   }

    lcm_destroy (lcm);
    
    return 0;
}
