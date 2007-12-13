#include <stdio.h>
#include <unistd.h>
#include <fcntl.h>
#include <string.h>
#include <stdlib.h>
#include <signal.h>

#include <glib.h>

#include "glib_util.h"

#define dbg(args...) fprintf(stderr, args)
#undef dbg
#define dbg(args...)

typedef struct _signal_pipe {
    int fds[2];
    GIOChannel *ioc;
    guint ios;

    signal_pipe_glib_handler_t userfunc;
    void *userdata;
} signal_pipe_t;

static signal_pipe_t g_sp;
static int g_sp_initialized = 0;

int 
signal_pipe_init()
{
    if (g_sp_initialized) {
        fprintf(stderr, "signal_pipe already initialized!!\n");
        return -1;
    }

    if (0 != pipe (g_sp.fds)) {
        perror("signal_pipe");
        return -1;
    }

    int flags = fcntl (g_sp.fds[1], F_GETFL);
    fcntl (g_sp.fds[1], F_SETFL, flags | O_NONBLOCK);

    g_sp_initialized = 1;

    dbg("signal_pipe: initialized\n");
    return 0;
}

int 
signal_pipe_cleanup()
{
    if (g_sp_initialized) {
        close (g_sp.fds[0]);
        close (g_sp.fds[1]);
        g_io_channel_unref (g_sp.ioc);
        g_sp_initialized = 0;
        return 0;
    }

    dbg("signal_pipe: destroyed\n");
    return -1;
}

static void
signal_handler (int signum)
{
    dbg("signal_pipe: caught signal %d\n", signum);
    write (g_sp.fds[1], &signum, sizeof(int));
}

static int
signal_handler_glib (GIOChannel *source, GIOCondition condition, void *ud)
{
    int signum;
    int status;
    status = read (g_sp.fds[0], &signum, sizeof(int));

    if (status != sizeof(int)) {
        fprintf(stderr, "wtf!? signal_handler_glib is confused (%s:%d)\n", 
                __FILE__, __LINE__);
        return TRUE;
    }

    if (g_sp.userfunc) {
        g_sp.userfunc (signum, g_sp.userdata);
    }

    return TRUE;
}

void 
signal_pipe_add_signal (int sig)
{
    // TODO use sigaction instead of signal()
#if 0
    struct sigaction siga;
    siga.sa_handler = signal_handler;
    siga.sa_sigaction = NULL;
    sigemptyset (&siga.sa_mask);
    siga.sa_flags = 0;
    siga.sa_restorer = 0;

    int status = sigaction (sig, &siga, NULL);
    if (0 != status) {
        perror("signal_pipe: sigaction failed");
    }
#else
    signal (sig, signal_handler);
#endif

    return;
}

int 
signal_pipe_attach_glib (signal_pipe_glib_handler_t func, gpointer user_data)
{
    if (! g_sp_initialized) return -1;

    if (g_sp.ioc) return -1;

    g_sp.ioc = g_io_channel_unix_new (g_sp.fds[0]);
    g_io_channel_set_flags (g_sp.ioc, 
            g_io_channel_get_flags (g_sp.ioc) | G_IO_FLAG_NONBLOCK, NULL);
    g_sp.ios = g_io_add_watch (g_sp.ioc, G_IO_IN | G_IO_PRI, 
            (GIOFunc) signal_handler_glib, NULL);

    g_sp.userfunc = func;
    g_sp.userdata = user_data;

    return 0;
}


static void
spgqok_handler (int signum, void *user)
{
    GMainLoop *mainloop = (GMainLoop*) user;
    g_main_loop_quit (mainloop);
    signal_pipe_cleanup();
}

int 
signal_pipe_glib_quit_on_kill (GMainLoop *mainloop)
{
    if (0 != signal_pipe_init()) return -1;

    signal_pipe_add_signal (SIGINT);
    signal_pipe_add_signal (SIGTERM);
    signal_pipe_add_signal (SIGKILL);
    signal_pipe_add_signal (SIGHUP);

    return signal_pipe_attach_glib (spgqok_handler, mainloop);
}

static int
lc_message_ready (GIOChannel *source, GIOCondition cond, lc_t *lc)
{
    lc_handle (lc);
    return TRUE;
}

typedef struct {
    GIOChannel *ioc;
    guint sid;
    lc_t *lc;
} glib_attached_lc_t;

static GHashTable *lc_glib_sources = NULL;
static GStaticMutex lc_glib_sources_mutex = G_STATIC_MUTEX_INIT;

int
glib_mainloop_attach_lc (lc_t *lc)
{
    g_static_mutex_lock (&lc_glib_sources_mutex);

    if (!lc_glib_sources) {
        lc_glib_sources = g_hash_table_new (g_direct_hash, g_direct_equal);
    }

    if (g_hash_table_lookup (lc_glib_sources, lc)) {
        dbg ("LC %p already attached to mainloop\n", lc);
        g_static_mutex_unlock (&lc_glib_sources_mutex);
        return -1;
    }

    glib_attached_lc_t *galc = 
        (glib_attached_lc_t*) calloc (1, sizeof (glib_attached_lc_t));

    galc->ioc = g_io_channel_unix_new (lc_get_fileno (lc));
    galc->sid = g_io_add_watch (galc->ioc, G_IO_IN, (GIOFunc) lc_message_ready, 
            lc);
    galc->lc = lc;

    dbg ("inserted LC %p into glib mainloop\n", lc);
    g_hash_table_insert (lc_glib_sources, lc, galc);

    g_static_mutex_unlock (&lc_glib_sources_mutex);
    return 0;
}

int
glib_mainloop_detach_lc (lc_t *lc)
{
    g_static_mutex_lock (&lc_glib_sources_mutex);
    if (!lc_glib_sources) {
        dbg ("no lc glib sources\n");
        g_static_mutex_unlock (&lc_glib_sources_mutex);
        return -1;
    }

    glib_attached_lc_t *galc = 
        (glib_attached_lc_t*) g_hash_table_lookup (lc_glib_sources, lc);

    if (!galc) {
        dbg ("couldn't find matching gaLC\n");
        g_static_mutex_unlock (&lc_glib_sources_mutex);
        return -1;
    }

    dbg ("detaching LC from glib\n");
    g_io_channel_unref (galc->ioc);
    g_source_remove (galc->sid);

    g_hash_table_remove (lc_glib_sources, lc);
    free (galc);

    if (g_hash_table_size (lc_glib_sources) == 0) {
        g_hash_table_destroy (lc_glib_sources);
        lc_glib_sources = NULL;
    }

    g_static_mutex_unlock (&lc_glib_sources_mutex);
    return 0;
}
