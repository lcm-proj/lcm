// file: listener-glib.c
//
// This program demonstrates how to use LCM in a GLib event loop.
//
// compile with:
//  $ gcc -o listener-glib listener-glib.c `pkg-config --cflags --libs lcm glib-2.0`
//
//  (note that in the above line, ` is a backtick, not a normal quote mark ')

#include <stdio.h>
#include <sys/select.h>
#include <lcm/lcm.h>
#include <glib.h>
#include "exlcm_example_t.h"

typedef struct _state_t {
    GMainLoop* mainloop;
    lcm_t* lcm;
} state_t;

static void
on_example_msg(const lcm_recv_buf_t *rbuf, const char * channel, 
        const exlcm_example_t * msg, void * user)
{
    int i;
    printf("Received message on channel \"%s\":\n", channel);
    printf("  timestamp   = %lld\n", (long long)msg->timestamp);
    printf("  position    = (%f, %f, %f)\n",
            msg->position[0], msg->position[1], msg->position[2]);
    printf("  orientation = (%f, %f, %f, %f)\n",
            msg->orientation[0], msg->orientation[1], msg->orientation[2],
            msg->orientation[3]);
    printf("  ranges:");
    for(i = 0; i < msg->num_ranges; i++)
        printf(" %d", msg->ranges[i]);
    printf("\n");
    printf("  name        = '%s'\n", msg->name);
    printf("  enabled     = %d\n", msg->enabled);
}

static gboolean
on_lcm(GIOChannel *source, GIOCondition cond, void *user_data)
{
    state_t* state = (state_t*) user_data;
    if(0 != lcm_handle(state->lcm))
        g_main_loop_quit(state->mainloop);
    return TRUE;
}

static gboolean 
on_timeout(void* user_data)
{
    printf("Timer fired\n");
    return TRUE;
}

int
main(int argc, char ** argv)
{
    state_t* state = calloc(1, sizeof(state_t));
    state->lcm = lcm_create(NULL);
    if(!state->lcm)
        return 1;

    // subscribe to messages
    exlcm_example_t_subscribe(state->lcm, "EXAMPLE", &on_example_msg, state);

    // attach LCM to the GLib event loop
    state->mainloop = g_main_loop_new(NULL, TRUE);
    GIOChannel* ioc = g_io_channel_unix_new(lcm_get_fileno(state->lcm));
    guint sid = g_io_add_watch(ioc, G_IO_IN, (GIOFunc)on_lcm, state);

    // add a periodic timer to call a function every 1000 ms.
    g_timeout_add(1000, on_timeout, state);

    // start the GLib event loop running
    g_main_loop_run(state->mainloop);

    lcm_destroy(state->lcm);
    free(state);
    return 0;
}
