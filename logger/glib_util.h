#ifndef __lcm_logger_glib_util_h__
#define __lcm_logger_glib_util_h__

#include <lc.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef void (*signal_pipe_glib_handler_t) (int signal, void *user_data);

// initializes signal_pipe.  call this once per process.
int signal_pipe_init();

// cleans up resources used by the signal_pipe
int signal_pipe_cleanup();

// specifies that signal should be caught by signal_pipe and converted to a 
// glib event
void signal_pipe_add_signal (int signal);

// sets a handler function that is called when a signal is caught by
// signal_pipe.  The first argument to the user_func function is the number of
// the signal caught.  The second is the user_data parameter passed in here.
int signal_pipe_attach_glib (signal_pipe_glib_handler_t user_func, 
        gpointer user_data);

// convenience function to setup a signal handler that calls
// signal_pipe_init, and adds a signal handler that automatically call
// g_main_loop_quit (mainloop) on receiving SIGTERM, SIGINT, or SIGHUP.
// also invokes signal_pipe_cleanup() on receiving these signals.
int signal_pipe_glib_quit_on_kill (GMainLoop *mainloop);

int glib_mainloop_attach_lc (lc_t *lc);

int glib_mainloop_detach_lc (lc_t *lc);

#ifdef __cplusplus
}
#endif

#endif
