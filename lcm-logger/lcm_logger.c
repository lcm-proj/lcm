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

#include <lcm.h>

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
    char    *fname;
    int     filenum;
    lcm_t    *lcm;
};

int message_handler (const lcm_recv_buf_t *rbuf, void *u)
{
    logger_t *l = (logger_t*) u;
    lcm_eventlog_event_t  le;

    int64_t offset_utime = rbuf->recv_utime - l->time0;
    int channellen = strlen(rbuf->channel);

    // log_write_event will handle le.eventnum.
//    le.eventnum = l->nevents;
    le.timestamp = rbuf->recv_utime;
    le.channellen = channellen;
    le.datalen = rbuf->data_size;

    le.channel = (char*)  rbuf->channel;
    le.data = (char*) rbuf->data;

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

    return 0;
}

static void usage (const char *progname)
{
    fprintf (stderr, "usage: %s [options] [output file]\n"
            "\n"
            "Options:\n"
            "\n"
            "    -n, --name SFX    string appended to filename of new logs\n"
            "    -p, --path PTH    Directory where new log files are created.\n"
            "                      Formatted with strftime\n"
            "    -h, --help        Shows this help text and exits\n"
            , progname);
}

int main(int argc, char *argv[])
{
    setlinebuf (stdout);

    char logpath[PATH_MAX];
    memset (logpath, 0, sizeof (logpath));
    char logsfx[PATH_MAX];
    memset (logsfx, 0, sizeof (logsfx));

    char *optstring = "hm:p:s:v";
    char c;
    struct option long_opts[] = { 
        { "help", no_argument, 0, 'h' },
        { "path", required_argument, 0, 'p' },
        { "n", required_argument, 0, 'n' },
        { 0, 0, 0, 0 }
    };

    while ((c = getopt_long (argc, argv, optstring, long_opts, 0)) >= 0)
    {
        switch (c) {
            case 'h':
                usage(argv[0]);
                return 1;
            case 'p':
                strncpy (logpath, optarg, sizeof (logpath) - 1);
                break;
            case 'n':
                strncpy (logsfx, optarg, sizeof (logsfx) - 1);
                break;
            default:
                usage(argv[0]);
                return 1;
        };
    }

    if (optind < argc-2) {
        usage (argv[0]);
        return 1;
    }

    logger_t logger;
    logger.nevents = 0;
    logger.logsize = 0;
    logger.last_report_time = 0;
    logger.events_since_last_report = 0;
    logger.last_report_logsize = 0;
    logger.fname=0;
    logger.time0= timestamp_now();

    char file[256];

    if (optind == argc) {
        /* Construct the default file name using the date */
        char path[4096];
        time_t now = time (NULL);
        strftime (path, sizeof (path), logpath, localtime (&now));
        if (!g_file_test (path, G_FILE_TEST_EXISTS)) {
            g_mkdir_with_parents (path, 0755);
        }
        time_t t = time (NULL);
        struct tm ti;
        localtime_r (&t, &ti);
        char basename[256];

        if (strlen (logsfx))
            snprintf (basename, sizeof (basename), "%d-%02d-%02d-log-%s",
                      ti.tm_year+1900, ti.tm_mon+1, ti.tm_mday, logsfx);
        else
            snprintf (basename, sizeof (basename), "%d-%02d-%02d-log",
                      ti.tm_year+1900, ti.tm_mon+1, ti.tm_mday);

        if (strlen (path))
            snprintf (file, sizeof (file), "%s/%s", path, basename);
        else
            strcpy (file, basename);
    }
    else {
        /* User specified a file name */
        strncpy (file, argv[optind], sizeof (file));
    }

    /* Loop through possible file names until we find one that doesn't already
     * exist.  This way, we never overwrite an existing file. */
    char filename[PATH_MAX];
    int res;
    logger.filenum = -1;
    do {
        logger.filenum++;
        struct stat statbuf;
        snprintf (filename, sizeof (filename), "%s.%02d", file, logger.filenum);
        res = stat (filename, &statbuf);
    } while (res == 0);

    if (errno != ENOENT) {
        perror ("Error: checking for previous logs");
        return -1;
    }

    fprintf (stderr, "Opening log file : \"%s\"...\n", filename);

    //////// open output file
    logger.fname = strdup(filename);
    logger.log = lcm_eventlog_create(filename, "w");
    if (logger.log == NULL) {
        perror ("Error: fopen failed");
        return -1;
    }

    //////// begin logging
    
    logger.lcm = lcm_create();
    assert(logger.lcm);
    
    res = lcm_init(logger.lcm, NULL);
    assert (!res);

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
    if (logger.fname)
        free(logger.fname);
}
