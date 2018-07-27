#include <assert.h>
#include <errno.h>
#include <getopt.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#include <glib.h>
#include <glib/gstdio.h>

#include <lcm/lcm.h>

// GRegex was new in GLib 2.14.0
#if GLIB_CHECK_VERSION(2, 14, 0)
#else
#error "LCM requires a glib version >= 2.14.0"
#endif

#ifdef WIN32
#define __STDC_FORMAT_MACROS  // Enable integer types
#include <lcm/windows/WinPorting.h>
#else
#include <unistd.h> /* fdatasync */
#endif

#include <inttypes.h>

#include "glib_util.h"

#ifdef SIGHUP
#define USE_SIGHUP
#endif

#define DEFAULT_MAX_WRITE_QUEUE_SIZE_MB 100

#define SECONDS_PER_HOUR 3600

GMainLoop *_mainloop;

static int _reset_logfile = 0;

static inline int64_t timestamp_seconds(int64_t v)
{
    return v / 1000000;
}

static inline int64_t timestamp_now(void)
{
    GTimeVal tv;
    g_get_current_time(&tv);
    return (int64_t) tv.tv_sec * 1000000 + tv.tv_usec;
}

typedef struct logger logger_t;
struct logger {
    lcm_eventlog_t *log;

    char input_fname[PATH_MAX];
    char fname[PATH_MAX];
    char fname_prefix[PATH_MAX];
    lcm_t *lcm;

    int64_t max_write_queue_size;
    int auto_increment;
    int next_increment_num;
    double auto_split_mb;
    int force_overwrite;
    int use_strftime;
    int fflush_interval_ms;
    int rotate;
    int quiet;
    int append;

    GThread *write_thread;
    GAsyncQueue *write_queue;
    GMutex *mutex;

    // variables for inverted matching (e.g., logging all but some channels)
    int invert_channels;
    GRegex *regex;

    // these members controlled by mutex
    int64_t write_queue_size;
    int write_thread_exit_flag;

    // these members controlled by write thread
    int64_t nevents;
    int64_t logsize;
    int64_t events_since_last_report;
    int64_t last_report_time;
    int64_t last_report_logsize;
    int64_t time0;
    int64_t last_fflush_time;

    int64_t dropped_packets_count;
    int64_t last_drop_report_utime;
    int64_t last_drop_report_count;
};

static void rotate_logfiles(logger_t *logger)
{
    if (!logger->quiet) {
        printf("Rotating log files\n");
    }
    // delete log files that have fallen off the end of the rotation
    gchar *tomove = g_strdup_printf("%s.%d", logger->fname_prefix, logger->rotate - 1);
    if (g_file_test(tomove, G_FILE_TEST_EXISTS)) {
        if (0 != g_unlink(tomove)) {
            fprintf(stderr, "ERROR! Unable to delete [%s]\n", tomove);
        }
    }
    g_free(tomove);

    // Rotate away any existing log files
    for (int file_num = logger->rotate - 1; file_num >= 0; file_num--) {
        gchar *newname = g_strdup_printf("%s.%d", logger->fname_prefix, file_num);
        tomove = g_strdup_printf("%s.%d", logger->fname_prefix, file_num - 1);
        if (g_file_test(tomove, G_FILE_TEST_EXISTS)) {
            if (0 != g_rename(tomove, newname)) {
                fprintf(stderr, "ERROR!  Unable to rotate [%s]\n", tomove);
            }
        }
        g_free(newname);
        g_free(tomove);
    }
}

static int open_logfile(logger_t *logger)
{
    // maybe run the filename through strftime
    if (logger->use_strftime) {
        char new_prefix[PATH_MAX];
        time_t now = time(NULL);
        strftime(new_prefix, sizeof(new_prefix), logger->input_fname, localtime(&now));

        // If auto-increment is enabled and the strftime-formatted filename
        // prefix has changed, then reset the auto-increment counter.
        if (logger->auto_increment && strcmp(new_prefix, logger->fname_prefix))
            logger->next_increment_num = 0;
        strcpy(logger->fname_prefix, new_prefix);
    } else {
        strcpy(logger->fname_prefix, logger->input_fname);
    }

    if (logger->auto_increment) {
        /* Loop through possible file names until we find one that doesn't
         * already exist.  This way, we never overwrite an existing file. */
        do {
            snprintf(logger->fname, sizeof(logger->fname), "%s.%02d", logger->fname_prefix,
                     logger->next_increment_num);
            logger->next_increment_num++;
        } while (g_file_test(logger->fname, G_FILE_TEST_EXISTS));
    } else if (logger->rotate > 0) {
        snprintf(logger->fname, sizeof(logger->fname), "%s.0", logger->fname_prefix);
    } else {
        strcpy(logger->fname, logger->fname_prefix);
        if (!(logger->force_overwrite || logger->append)) {
            if (g_file_test(logger->fname, G_FILE_TEST_EXISTS)) {
                fprintf(stderr, "Refusing to overwrite existing file \"%s\"\n", logger->fname);
                return 1;
            }
        }
    }

    // create directories if needed
    char *dirpart = g_path_get_dirname(logger->fname);
    if (!g_file_test(dirpart, G_FILE_TEST_IS_DIR)) {
        mkdir_with_parents(dirpart, 0755);
    }
    g_free(dirpart);

    if (!logger->quiet) {
        printf("Opening log file \"%s\"\n", logger->fname);
    }

    // open output file in append mode if we're rotating log files or appending
    // use write mode if not.
    const char *logmode = (logger->rotate > 0 || logger->append) ? "a" : "w";
    logger->log = lcm_eventlog_create(logger->fname, logmode);
    if (logger->log == NULL) {
        perror("Error: fopen failed");
        return 1;
    }
    return 0;
}

static void *write_thread(void *user_data)
{
    logger_t *logger = (logger_t *) user_data;

    GTimeVal start_time;
    g_get_current_time(&start_time);
    int num_splits = 0;

    while (1) {
        void *msg = g_async_queue_pop(logger->write_queue);

        // Is it time to start a new logfile?
        int split_log = 0;
        if (logger->auto_split_mb) {
            double logsize_mb = (double) logger->logsize / (1 << 20);
            split_log = (logsize_mb > logger->auto_split_mb);
        }
        if (_reset_logfile) {
            split_log = 1;
            _reset_logfile = 0;
        }

        if (split_log) {
            // Yes.  open up a new log file
            lcm_eventlog_destroy(logger->log);
            if (logger->rotate > 0)
                rotate_logfiles(logger);
            if (0 != open_logfile(logger))
                exit(1);
            num_splits++;
            logger->logsize = 0;
            logger->last_report_logsize = 0;
        }

        // Should the write thread exit?
        g_mutex_lock(logger->mutex);
        if (msg == &logger->write_thread_exit_flag) {
            g_mutex_unlock(logger->mutex);
            return NULL;
        }
        // nope.  write the event to disk
        lcm_eventlog_event_t *le = (lcm_eventlog_event_t *) msg;
        int64_t sz = sizeof(lcm_eventlog_event_t) + le->channellen + 1 + le->datalen;
        logger->write_queue_size -= sz;
        g_mutex_unlock(logger->mutex);

        if (0 != lcm_eventlog_write_event(logger->log, le)) {
            static int64_t last_spew_utime = 0;
            char *reason = strdup(strerror(errno));
            int64_t now = timestamp_now();
            if (now - last_spew_utime > 500000) {
                fprintf(stderr, "lcm_eventlog_write_event: %s\n", reason);
                last_spew_utime = now;
            }
            free(reason);
            free(le);
            if (errno == ENOSPC) {
                exit(1);
            } else {
                continue;
            }
        }
        if (logger->fflush_interval_ms >= 0 &&
            (le->timestamp - logger->last_fflush_time) > logger->fflush_interval_ms * 1000) {
            fflush(logger->log->f);
#ifndef WIN32
            // Perform a full fsync operation after flush
            fdatasync(fileno(logger->log->f));
#endif
            logger->last_fflush_time = le->timestamp;
        }

        // bookkeeping, cleanup
        int64_t offset_utime = le->timestamp - logger->time0;
        logger->nevents++;
        logger->events_since_last_report++;
        logger->logsize += 4 + 8 + 8 + 4 + le->channellen + 4 + le->datalen;

        free(le);

        if (!logger->quiet && (offset_utime - logger->last_report_time > 1000000)) {
            double dt = (offset_utime - logger->last_report_time) / 1000000.0;

            double tps = logger->events_since_last_report / dt;
            double kbps = (logger->logsize - logger->last_report_logsize) / dt / 1024.0;

            // clang-format off
            static const char* const info_format_str =
                "Summary: %s ti:%4" PRIi64 "sec Events: %-9" PRIi64 " ( %4"PRIi64" MB )      TPS: %8.2f       KB/s: %8.2f\n";
            // clang-format on

            printf(info_format_str, logger->fname, timestamp_seconds(offset_utime), logger->nevents,
                   logger->logsize / 1048576, tps, kbps);
            logger->last_report_time = offset_utime;
            logger->events_since_last_report = 0;
            logger->last_report_logsize = logger->logsize;
        }
    }
}

static void message_handler(const lcm_recv_buf_t *rbuf, const char *channel, void *u)
{
    logger_t *logger = (logger_t *) u;

    if (logger->invert_channels) {
        if (g_regex_match(logger->regex, channel, (GRegexMatchFlags) 0, NULL))
            return;
    }

    int channellen = strlen(channel);

    // check if the backlog of unwritten messages is too big.  If so, then
    // ignore this event
    int64_t mem_sz = sizeof(lcm_eventlog_event_t) + channellen + 1 + rbuf->data_size;
    g_mutex_lock(logger->mutex);
    int64_t mem_required = mem_sz + logger->write_queue_size;

    if (mem_required > logger->max_write_queue_size) {
        // can't write to logfile fast enough.  drop packet.
        g_mutex_unlock(logger->mutex);

        // maybe print an informational message to stdout
        int64_t now = timestamp_now();
        logger->dropped_packets_count++;
        int rc = logger->dropped_packets_count - logger->last_drop_report_count;

        if (now - logger->last_drop_report_utime > 1000000 && rc > 0) {
            if (!logger->quiet)
                printf("Can't write to log fast enough.  Dropped %d packet%s\n", rc,
                       rc == 1 ? "" : "s");
            logger->last_drop_report_utime = now;
            logger->last_drop_report_count = logger->dropped_packets_count;
        }
        return;
    } else {
        logger->write_queue_size = mem_required;
        g_mutex_unlock(logger->mutex);
    }

    // queue up the message for writing to disk by the write thread
    lcm_eventlog_event_t *le = (lcm_eventlog_event_t *) malloc(mem_sz);
    memset(le, 0, mem_sz);

    le->timestamp = rbuf->recv_utime;
    le->channellen = channellen;
    le->datalen = rbuf->data_size;
    // log_write_event will handle le.eventnum.

    le->channel = ((char *) le) + sizeof(lcm_eventlog_event_t);
    strcpy(le->channel, channel);
    le->data = le->channel + channellen + 1;
    assert((char *) le->data + rbuf->data_size == (char *) le + mem_sz);
    memcpy(le->data, rbuf->data, rbuf->data_size);

    g_async_queue_push(logger->write_queue, le);
}

#ifdef USE_SIGHUP
static void sighup_handler(int signum)
{
    _reset_logfile = 1;
}
#endif

static void usage()
{
    fprintf(stderr,
            "usage: lcm-logger [options] [FILE]\n"
            "\n"
            "    LCM message logging utility.  Subscribes to all channels on an LCM\n"
            "    network, and records all messages received on that network to\n"
            "    FILE.  If FILE is not specified, then a filename is automatically\n"
            "    chosen.\n"
            "\n"
            "Options:\n"
            "\n"
            "  -c, --channel=CHAN         Channel string to pass to lcm_subscribe.\n"
            "                             (default: \".*\")\n"
            "      --flush-interval=MS    Flush the log file to disk every MS milliseconds.\n"
            "                             (default: 100)\n"
            "  -f, --force                Overwrite existing files\n"
            "  -h, --help                 Shows this help text and exits\n"
            "  -i, --increment            Automatically append a suffix to FILE\n"
            "                             such that the resulting filename does not\n"
            "                             already exist.  This option precludes -f and\n"
            "                             --rotate\n"
            "  -l, --lcm-url=URL          Log messages on the specified LCM URL\n"
            "  -m, --max-unwritten-mb=SZ  Maximum size of received but unwritten\n"
            "                             messages to store in memory before dropping\n"
            "                             messages.  (default: 100 MB)\n"
            "      --rotate=NUM           When creating a new log file, rename existing files\n"
            "                             out of the way and always write to FILE.0.  If\n"
            "                             FILE.0 already exists, it is renamed to FILE.1.  If\n"
            "                             FILE.1 exists, it is renamed to FILE.2, etc.  If\n"
            "                             FILE.NUM exists, then it is deleted.  This option\n"
            "                             precludes -i.\n"
            "      --split-mb=N           Automatically start writing to a new log\n"
            "                             file once the log file exceeds N MB in size\n"
            "                             (can be fractional).  This option requires -i\n"
            "                             or --rotate.\n"
            "  -q, --quiet                Suppress normal output and only report errors.\n"
            "  -a, --append               Append events to the given log file.\n"
            "  -s, --strftime             Format FILE with strftime.\n"
            "  -v, --invert-channels      Invert channels.  Log everything that CHAN\n"
            "                             does not match.\n"
            "\n"
            "Rotating / splitting log files\n"
            "==============================\n"
            "    For long-term logging, lcm-logger can rotate through a fixed number of\n"
            "    log files, moving to a new log file as existing files reach a maximum size.\n"
            "    To do this, use --rotate and --split-mb.  For example:\n"
            "\n"
            "        # Rotate through logfile.0, logfile.1, ... logfile.4\n"
            "        lcm-logger --rotate=5 --split-mb=2 logfile\n"
            "\n"
            "    Moving to a new file happens either when the current log file size exceeds\n"
            "    the limit specified by --split-mb, or when lcm-logger receives a SIGHUP.\n"
            "\n");
}

int main(int argc, char *argv[])
{
#ifndef WIN32
    setlinebuf(stdout);
#endif

    char logpath[PATH_MAX];
    memset(logpath, 0, sizeof(logpath));

    logger_t logger;
    memset(&logger, 0, sizeof(logger));

    // set some defaults
    logger.force_overwrite = 0;
    logger.auto_increment = 0;
    logger.use_strftime = 0;
    char *chan_regex = strdup(".*");
    double max_write_queue_size_mb = DEFAULT_MAX_WRITE_QUEUE_SIZE_MB;
    logger.invert_channels = 0;
    logger.fflush_interval_ms = 100;
    logger.rotate = -1;
    logger.quiet = 0;
    logger.append = 0;

    char *lcmurl = NULL;
    char *optstring = "fic:shm:vu:qa";
    int c;
    struct option long_opts[] = {
        {"split-mb", required_argument, 0, 'b'},
        {"channel", required_argument, 0, 'c'},
        {"force", no_argument, 0, 'f'},
        {"increment", required_argument, 0, 'i'},
        {"lcm-url", required_argument, 0, 'l'},
        {"max-unwritten-mb", required_argument, 0, 'm'},
        {"rotate", required_argument, 0, 'r'},
        {"strftime", required_argument, 0, 's'},
        {"quiet", no_argument, 0, 'q'},
        {"append", no_argument, 0, 'a'},
        {"invert-channels", no_argument, 0, 'v'},
        {"flush-interval", required_argument, 0, 'u'},
        {0, 0, 0, 0},
    };

    while ((c = getopt_long(argc, argv, optstring, long_opts, 0)) >= 0) {
        switch (c) {
        case 'b':
            logger.auto_split_mb = strtod(optarg, NULL);
            if (logger.auto_split_mb <= 0) {
                usage();
                return 1;
            }
            break;
        case 'f':
            logger.force_overwrite = 1;
            break;
        case 'c':
            free(chan_regex);
            chan_regex = strdup(optarg);
            break;
        case 'i':
            logger.auto_increment = 1;
            break;
        case 's':
            logger.use_strftime = 1;
            break;
        case 'l':
            free(lcmurl);
            lcmurl = strdup(optarg);
            break;
        case 'q':
            logger.quiet = 1;
            break;
        case 'v':
            logger.invert_channels = 1;
            break;
        case 'm':
            max_write_queue_size_mb = strtod(optarg, NULL);
            if (max_write_queue_size_mb <= 0) {
                usage();
                return 1;
            }
            break;
        case 'r': {
            char *eptr = NULL;
            logger.rotate = strtol(optarg, &eptr, 10);
            if (*eptr) {
                usage();
                return 1;
            }
        } break;
        case 'u':
            logger.fflush_interval_ms = atol(optarg);
            if (logger.fflush_interval_ms <= 0) {
                usage();
                return 1;
            }
            break;
        case 'a':
            logger.append = 1;
            break;
        case 'h':
        default:
            usage();
            return 1;
        };
    }

    if (optind == argc) {
        strcpy(logger.input_fname, "lcmlog-%Y-%m-%d");
        logger.auto_increment = 1;
        logger.use_strftime = 1;
    } else if (optind == argc - 1) {
        strncpy(logger.input_fname, argv[optind], sizeof(logger.input_fname));
    } else if (optind < argc - 1) {
        usage();
        return 1;
    }

    if (logger.auto_split_mb > 0 && !(logger.auto_increment || (logger.rotate > 0))) {
        fprintf(stderr, "ERROR.  --split-mb requires either --increment or --rotate\n");
        return 1;
    }
    if (logger.rotate > 0 && logger.auto_increment) {
        fprintf(stderr, "ERROR.  --increment and --rotate can't both be used\n");
        return 1;
    }
    if (logger.force_overwrite && logger.append) {
        fprintf(stderr, "ERROR.  --force_overwrite and --append can't both be used\n");
    }

    logger.time0 = timestamp_now();
    logger.max_write_queue_size = (int64_t)(max_write_queue_size_mb * (1 << 20));

    if (0 != open_logfile(&logger))
        return 1;

    // create write thread
    logger.write_thread_exit_flag = 0;
    logger.mutex = g_mutex_new();
    logger.write_queue_size = 0;
    logger.write_queue = g_async_queue_new();
    logger.write_thread = g_thread_create(write_thread, &logger, TRUE, NULL);

    // begin logging
    logger.lcm = lcm_create(lcmurl);
    free(lcmurl);
    if (!logger.lcm) {
        fprintf(stderr, "Couldn't initialize LCM!");
        return 1;
    }

    if (logger.invert_channels) {
        // if inverting the channels, subscribe to everything and invert on the
        // callback
        lcm_subscribe(logger.lcm, ".*", message_handler, &logger);
        char *regexbuf = g_strdup_printf("^%s$", chan_regex);
        GError *rerr = NULL;
        logger.regex = g_regex_new(regexbuf, (GRegexCompileFlags) 0, (GRegexMatchFlags) 0, &rerr);
        if (rerr) {
            fprintf(stderr, "%s\n", rerr->message);
            g_free(regexbuf);
            return 1;
        }
        g_free(regexbuf);
    } else {
        // otherwise, let LCM handle the regex
        lcm_subscribe(logger.lcm, chan_regex, message_handler, &logger);
    }

    free(chan_regex);

    _mainloop = g_main_loop_new(NULL, FALSE);
    signal_pipe_glib_quit_on_kill();
    glib_mainloop_attach_lcm(logger.lcm);

#ifdef USE_SIGHUP
    signal(SIGHUP, sighup_handler);
#endif

    // main loop
    g_main_loop_run(_mainloop);

    fprintf(stderr, "Logger exiting\n");

    // stop the write thread
    g_mutex_lock(logger.mutex);
    logger.write_thread_exit_flag = 1;
    g_mutex_unlock(logger.mutex);
    g_async_queue_push(logger.write_queue, &logger.write_thread_exit_flag);
    g_thread_join(logger.write_thread);
    g_mutex_free(logger.mutex);

    // cleanup.  This isn't strictly necessary, do it to be pedantic and so that
    // leak checkers don't complain
    glib_mainloop_detach_lcm(logger.lcm);
    lcm_destroy(logger.lcm);
    lcm_eventlog_destroy(logger.log);

    for (void *msg = g_async_queue_try_pop(logger.write_queue); msg;
         msg = g_async_queue_try_pop(logger.write_queue)) {
        if (msg == &logger.write_thread_exit_flag)
            continue;
        free(msg);
    }
    g_async_queue_unref(logger.write_queue);

    if (logger.invert_channels) {
        g_regex_unref(logger.regex);
    }

    return 0;
}
