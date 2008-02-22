#include <stdio.h>
#include <assert.h>
#include <unistd.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <time.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <inttypes.h>
#include <getopt.h>

#include <glib.h>

#include <lcm/lcm.h>

#include "glib_util.h"

static inline int64_t timestamp_seconds(int64_t v)
{
    return v/1000000;
}

static inline int64_t timestamp_now()
{
    struct timeval tv;
    gettimeofday (&tv, NULL);
    return (int64_t) tv.tv_sec * 1000000 + tv.tv_usec;
}

typedef struct logger logger_t;
struct logger
{
    lcm_eventlog_t *log;

    int64_t nevents;
    int64_t logsize;

    int64_t events_since_last_report;
    int64_t last_report_time;
    int64_t last_report_logsize;
    int64_t time0;
    char    fname[PATH_MAX];
    lcm_t    *lcm;
};

void message_handler (const lcm_recv_buf_t *rbuf, const char *channel, void *u)
{
    logger_t *l = (logger_t*) u;
    lcm_eventlog_event_t  le;

    int64_t offset_utime = rbuf->recv_utime - l->time0;
    int channellen = strlen(channel);

    le.timestamp = rbuf->recv_utime;
    le.channellen = channellen;
    le.datalen = rbuf->data_size;
    // log_write_event will handle le.eventnum.

    le.channel = (char*)  channel;
    le.data = rbuf->data;

    lcm_eventlog_write_event(l->log, &le);

    l->nevents++;
    l->events_since_last_report ++;

    l->logsize += 4 + 8 + 8 + 4 + channellen + 4 + rbuf->data_size;

    if (offset_utime - l->last_report_time > 1000000) {
        double dt = (offset_utime - l->last_report_time)/1000000.0;

        double tps =  l->events_since_last_report / dt;
        double kbps = (l->logsize - l->last_report_logsize) / dt / 1024.0;

        printf("Summary: %s ti:%4"PRIi64"sec Events: %-9"PRIi64" ( %4"PRIi64" MB )      TPS: %8.2f       KB/s: %8.2f\n", 
               l->fname,
               timestamp_seconds(offset_utime),
               l->nevents, l->logsize/1048576, 
               tps, kbps);
        l->last_report_time = offset_utime;
        l->events_since_last_report = 0;
        l->last_report_logsize = l->logsize;
    }
}

static void usage ()
{
    fprintf (stderr, "usage: lcm-logger [options] [FILE]\n"
            "\n"
            "    LCM message logging utility.  Subscribes to all channels on an LCM\n"
            "    network, and records all messages received on that network to\n"
            "    FILE.  If FILE is not specified, then a filename is automatically\n"
            "    chosen.\n"
            "\n"
            "Options:\n"
            "\n"
            "    -f, --force              Overwrite existing files\n"
            "    -i, --increment          Automatically append a suffix to FILE\n"
            "                             such that the resulting filename does not\n"
            "                             already exist.  This option precludes -f\n"
            "    -s, --strftime           Format FILE with strftime.\n" 
            "    -p PRV, --provider PRV   LCM network provider.\n"
            "    -h, --help               Shows this help text and exits\n"
            "\n");
}

int main(int argc, char *argv[])
{
    setlinebuf (stdout);

    char logpath[PATH_MAX];
    memset (logpath, 0, sizeof (logpath));

    // set some defaults
    char provider[4096];
    memset (provider, 0, sizeof (provider));

    int force = 0;
    int auto_increment = 0;
    int use_strftime = 0;

    char *optstring = "fisp:h";
    char c;
    struct option long_opts[] = { 
        { "force", no_argument, 0, 'f' },
        { "increment", required_argument, 0, 'i' },
        { "strftime", required_argument, 0, 's' },
        { "provider", required_argument, 0, 'p' },
        { 0, 0, 0, 0 }
    };

    while ((c = getopt_long (argc, argv, optstring, long_opts, 0)) >= 0)
    {
        switch (c) {
            case 'f':
                force = 1;
                break;
            case 'i':
                auto_increment = 1;
                break;
            case 's':
                use_strftime = 1;
                break;
            case 'p':
                strncpy (provider, optarg, sizeof (provider));
                break;
            case 'h':
            default:
                usage();
                return 1;
        };
    }

    if (optind == argc) {
        strcpy (logpath, "lcmlog-%Y-%m-%d");
        auto_increment = 1;
        use_strftime = 1;
    } else if (optind == argc - 1) {
        strncpy (logpath, argv[optind], sizeof (logpath));
    } else if (optind < argc-1) {
        usage ();
        return 1;
    }

    logger_t logger;
    memset (&logger, 0, sizeof (logger));
    logger.time0 = timestamp_now();

    // maybe run the filename through strftime
    if (use_strftime) {
        time_t now = time (NULL);
        strftime (logger.fname, sizeof (logger.fname), logpath, 
                localtime (&now));
    } else {
        strcpy (logger.fname, logpath);
    }

    if (auto_increment) {
        /* Loop through possible file names until we find one that doesn't
         * already exist.  This way, we never overwrite an existing file. */
        char tmpname[PATH_MAX];
        int filenum = -1;
        struct stat sbuf;
        do {
            filenum++;
            snprintf (tmpname, sizeof (tmpname), "%s.%02d", logger.fname, 
                    filenum);
        } while (0 == stat (tmpname, &sbuf));

        if (errno != ENOENT) {
            perror ("Error: checking for previous logs");
            return -1;
        }

        strcpy (logger.fname, tmpname);
    } else if (! force) {
        struct stat sbuf;
        if (0 == stat (logger.fname, &sbuf)) {
            fprintf (stderr, "Refusing to overwrite existing file \"%s\"\n",
                    logger.fname);
            return 1;
        }
    }

    // create directories if needed
    char *dirpart = g_path_get_dirname (logger.fname);
    if (! g_file_test (dirpart, G_FILE_TEST_IS_DIR)) {
        g_mkdir_with_parents (dirpart, 0755);
    }
    free (dirpart);

    fprintf (stderr, "Opening log file \"%s\"\n", logger.fname);

    // open output file
    logger.log = lcm_eventlog_create(logger.fname, "w");
    if (logger.log == NULL) {
        perror ("Error: fopen failed");
        return -1;
    }

    // begin logging
    logger.lcm = lcm_create (provider);
    if (!logger.lcm) {
        fprintf (stderr, "Couldn't initialize LCM!");
        return -1;
    }
    
    lcm_subscribe(logger.lcm, ".*", message_handler, &logger);

    GMainLoop *mainloop = g_main_loop_new (NULL, FALSE);
    signal_pipe_glib_quit_on_kill (mainloop);
    glib_mainloop_attach_lcm (logger.lcm);

    // main loop
    g_main_loop_run (mainloop);

    // cleanup
    glib_mainloop_detach_lcm (logger.lcm);
    lcm_destroy (logger.lcm);
    lcm_eventlog_destroy (logger.log);
}
