#include <assert.h>
#include <errno.h>
#include <getopt.h>
#include <glib.h>
#include <glib/gstdio.h>
#include <lcm/lcm.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

// Several thread and synchronization API functions (e.g. g_mutex_init,
// g_cond_init, g_thread_new, etc) require 2.32
#if GLIB_CHECK_VERSION(2, 32, 0)
#else
#error "LCM requires a glib version >= 2.32.0"
#endif

#ifdef WIN32
#define __STDC_FORMAT_MACROS  // Enable integer types
#else
#include <unistd.h> /* fdatasync */
#endif

#ifdef _MSC_VER
#include <lcm/windows/WinPorting.h>
#endif

#ifndef WIN32
#include <sys/statvfs.h>
#endif

#include <inttypes.h>

#include "glib_util.h"

#ifdef SIGHUP
#define USE_SIGHUP
#endif

#define DEFAULT_MAX_WRITE_QUEUE_SIZE_MB 100

#define SECONDS_PER_HOUR 3600

GMainLoop *_mainloop;

// Force rotating logs when a SIGHUP is received.
// It is common practice for programs running as service/daemon to restart itself
// in some way in response to SIGHUP. lcm-logger responds by rotating logs.
static int _reset_logfile = 0;  // bool

static inline int64_t timestamp_seconds(int64_t v)
{
    return v / 1000000;
}

static int64_t get_free_disk_bytes(const char *path_on_disk)
{
#ifdef WIN32
    // Not implemented; logger->disk_quota should be 0.
    assert(false);
    return -1;
#else
    /*
     * > f_bsize:  Filesystem block size
     * > f_frsize: Fragment size
     * https://man7.org/linux/man-pages/man3/statvfs.3.html
     *
     * Both work on Ubuntu, but f_bsize is used differently on macOS.
     * Must use f_frsize.
     */
    struct statvfs fs;
    statvfs(path_on_disk, &fs);
    int64_t free_bytes = (int64_t) fs.f_frsize * (int64_t) fs.f_bavail;
    return free_bytes;

#endif
}

//

/**
 * Parse a string like "5.5MB" as a number of bytes.
 * K and KiB are treated as 1024 kibibytes.
 * KB is treated kilobytes.
 * Input is case insensitive. KB and kb are both treated as kilobytes.
 * Input will never be treated as kilobits.
 *
 * Suported unit prefixes: B, K, M, G, T, P, E
 *
 * str: A null terminated string in the form <value>[ ]<unit> .
 *  Both the value and unit are required.
 *  Spaces are permited between the value and the unit.
 *  Value must be positive.
 *
 * Returns -1 on error.
 * Otherwise the number of byte represented by the input.
 */
static int64_t parse_mem_size(const char *str)
{
    const int64_t INVALID_SIZE = -1;

    // int64_t len = strlen(str);
    char *end = NULL;
    double val = strtod(str, &end);
    if (str == end) {
        // Could not parse a double
        return INVALID_SIZE;
    }
    if (val < 0) {
        return INVALID_SIZE;
    }

    // Munch spaces if they exist
    while (*end && (*end) == ' ') {
        end++;
    }

    // ---
    // Parse unit
    size_t unit_len = strlen(end);
    if (unit_len == 0) {
        // Could default to byte scale, but for our use case, that is probably unintentional.
        return INVALID_SIZE;
    }

    double scale = 1;
    double thous = 1000;  // 1024 vs 1000?
    if (1 == unit_len) {
        // 10 K
        thous = 1024;
    } else {
        // 10 KB vs 10 Ki[B]
        char mod = end[1];
        switch (mod) {
        case 'B':
        case 'b':
            thous = 1000;
            break;
        case 'I':
        case 'i':
            thous = 1024;
            break;
        default:
            return INVALID_SIZE;
        }
    }
    if (3 == unit_len) {
        // KiB: Optional B following Ki
        char b = end[3 - 1];
        if (b != 'B' && b != 'b') {
            return INVALID_SIZE;
        }
    } else if (unit_len > 3) {
        return INVALID_SIZE;
    }

    char unit = end[0];
    switch (unit) {
    case 'B':
    case 'b':
        scale = 1;
        break;
    case 'K':
    case 'k':
        scale = thous;
        break;
    case 'M':
    case 'm':
        scale = thous * thous;
        break;
    case 'G':
    case 'g':
        scale = thous * thous * thous;
        break;
    case 'T':
    case 't':
        scale = thous * thous * thous * thous;
        break;
    case 'P':
    case 'p':
        scale = thous * thous * thous * thous * thous;
        break;
    case 'E':
    case 'e':
        scale = thous * thous * thous * thous * thous * thous;
        break;

    default:
        return INVALID_SIZE;
    }

    return val * scale;
}

typedef struct logger logger_t;
struct logger {
    lcm_eventlog_t *log;

    char input_fname[PATH_MAX];
    char fname[PATH_MAX];
    char fname_prefix[PATH_MAX];
    char *write_directory;

    lcm_t *lcm;

    int64_t max_write_queue_size;
    int auto_increment;  // bool
    int next_increment_num;
    double auto_split_mb;
    int force_overwrite;  // bool
    int use_strftime;     // bool
    int fflush_interval_ms;
    int rotate;
    int quiet;   // bool
    int append;  // bool
    int64_t disk_quota;

    GThread *write_thread;
    GAsyncQueue *write_queue;
    GMutex mutex;

    // bool for inverted matching (e.g., logging all but some channels)
    int invert_channels;
    GRegex *regex;

    // these members controlled by mutex
    struct {
        int64_t write_queue_size;
        int write_thread_exit_flag;  // bool
    } sync;
    // these members controlled by write thread
    int64_t nevents;
    int64_t logsize;
    int64_t events_since_last_report;
    int64_t last_report_time;
    int64_t last_report_logsize;
    // Time (micro s) the logger started
    int64_t time0;
    int64_t last_fflush_time;

    int64_t dropped_packets_count;
    int64_t last_drop_report_utime;
    int64_t last_drop_report_count;

    int64_t last_quota_time;
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
            int ret = snprintf(logger->fname, sizeof(logger->fname), "%s.%02d",
                               logger->fname_prefix, logger->next_increment_num);
            if (ret < 0) {
                fprintf(stderr, "Error: failed to create filename string");
                return 1;
            }
            logger->next_increment_num++;
        } while (g_file_test(logger->fname, G_FILE_TEST_EXISTS));
    } else if (logger->rotate > 0) {
        int ret = snprintf(logger->fname, sizeof(logger->fname), "%s.0", logger->fname_prefix);
        if (ret < 0) {
            fprintf(stderr, "Error: failed to create filename string");
            return 1;
        }
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

    while (1) {
        void *msg = g_async_queue_pop(logger->write_queue);

        // ---
        // Is it time to start a new logfile?
        gboolean split_log = 0;
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

            if (logger->rotate > 0) {
                rotate_logfiles(logger);
            }

            if (0 != open_logfile(logger)) {
                printf("Failed to open next log. Aborting.\n");
                exit(1);
            }
            logger->logsize = 0;
            logger->last_report_logsize = 0;
        }

        // ---
        // Should the write thread exit?

        {
            // Looking at the address of write_thread_exit_flag instead of its value.
            // [2024-10-08 judfs: Why though?]
            void *sentinel_msg = &(logger->sync.write_thread_exit_flag);

            if (msg == sentinel_msg) {
                return NULL;
            }
        }

        // ---
        // Track the pop
        lcm_eventlog_event_t *log_event = (lcm_eventlog_event_t *) msg;
        {  // LOCK
            g_mutex_lock(&logger->mutex);

            int64_t sz =
                sizeof(lcm_eventlog_event_t) + log_event->channellen + 1 + log_event->datalen;
            logger->sync.write_queue_size -= sz;
            g_mutex_unlock(&logger->mutex);
        }

        // ---
        // Write the event to disk
        if (0 != lcm_eventlog_write_event(logger->log, log_event)) {
            // Write error
            static int64_t last_spew_utime = 0;
            char *reason = strdup(strerror(errno));
            int64_t now = g_get_real_time();
            if (now - last_spew_utime > 500000) {
                fprintf(stderr, "lcm_eventlog_write_event: %s\n", reason);
                last_spew_utime = now;
            }
            free(reason);
            free(log_event);
            if (errno == ENOSPC) {
                exit(1);
            } else {
                continue;
            }
        }

        assert(logger->fflush_interval_ms >= 0);
        gboolean needs_flushed =
            (log_event->timestamp - logger->last_fflush_time) > (logger->fflush_interval_ms * 1000);
        if (needs_flushed) {
            fflush(logger->log->f);
#ifndef WIN32
            // Perform a full fsync operation after flush
            fdatasync(fileno(logger->log->f));
#endif
            logger->last_fflush_time = log_event->timestamp;
        }

        // ---
        // Bookkeeping, cleanup
        int64_t offset_utime = log_event->timestamp - logger->time0;
        logger->nevents++;
        logger->events_since_last_report++;
        logger->logsize += 4 + 8 + 8 + 4 + log_event->channellen + 4 + log_event->datalen;

        free(log_event);

        // ---
        // UI update
        const int64_t term_update_interval = 1000000;  // 1sec * 1e6
        if (!logger->quiet && (offset_utime - logger->last_report_time > term_update_interval)) {
            double dt = (offset_utime - logger->last_report_time) / 1e6;  // (Sec)

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

        /* Disk Quota
         * There is no strong motivation for performing this check at this point in the code.
         * For now, avoiding the complication of checking on an additional thread.
         */
        const int64_t quota_check_interval = 1e6;  // Microsec
        if (logger->disk_quota && (offset_utime - logger->last_quota_time > quota_check_interval)) {
            logger->last_quota_time = offset_utime;

            int64_t free_bytes = get_free_disk_bytes(logger->write_directory);
            if (free_bytes < logger->disk_quota) {
                printf("Disk quota exceeded. Exiting logger.\n");

                /* How should this exit? For now going with a graceful exit.
                 * But maybe this case warrants more urgency.
                 * Make a PR if you feel this change.
                 */

                // Akin to ctrl-c. Stops the glib loop and returns control to our `main`.
                g_main_loop_quit(_mainloop);
            }
        }

    }  // END while (1)
}

static void message_handler(const lcm_recv_buf_t *rbuf, const char *channel, void *u)
{
    logger_t *logger = (logger_t *) u;

    if (logger->invert_channels) {
        if (g_regex_match(logger->regex, channel, (GRegexMatchFlags) 0, NULL))
            return;
    }

    int channellen = strlen(channel);

    // Check if the backlog of unwritten messages is too big.  If so, then
    // ignore this event
    int64_t event_sz = sizeof(lcm_eventlog_event_t) + channellen + 1 + rbuf->data_size;
    {  // LOCK
        g_mutex_lock(&logger->mutex);
        int64_t pending_queue_mem = event_sz + logger->sync.write_queue_size;

        if (pending_queue_mem > logger->max_write_queue_size) {
            g_mutex_unlock(&logger->mutex);

            // Can't write to logfile fast enough. Drop packet.
            logger->dropped_packets_count++;

            // maybe print an informational message to stdout
            int64_t now = g_get_real_time();
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
            logger->sync.write_queue_size = pending_queue_mem;

            g_mutex_unlock(&logger->mutex);
        }
    }

    // Queue up the message for writing to disk by the write thread

    // Make a flat allocation for both an event_t and its data buffers.
    lcm_eventlog_event_t *log_event = (lcm_eventlog_event_t *) malloc(event_sz);
    memset(log_event, 0, event_sz);

    // Store the channel just past the end of the struct
    log_event->channel = ((char *) log_event) + sizeof(lcm_eventlog_event_t);
    // Store the data past the channel.
    log_event->data = log_event->channel + channellen + 1;
    assert((char *) log_event->data + rbuf->data_size == (char *) log_event + event_sz);

    //
    log_event->timestamp = rbuf->recv_utime;
    log_event->channellen = channellen;
    log_event->datalen = rbuf->data_size;
    // log_write_event will handle le.eventnum.

    strcpy(log_event->channel, channel);

    memcpy(log_event->data, rbuf->data, rbuf->data_size);

    g_async_queue_push(logger->write_queue, log_event);
}

#ifdef USE_SIGHUP
static void sighup_handler(int signum)
{
    _reset_logfile = 1;
}
#endif

static void usage()
{
    // Manually wrapped to 80cols Leaves 52 for flag help.
    // The following command may help you when editing a long help block:
    // fold -w 52 -s /tmp/qqq.txt > /tmp/sink && cp /tmp/sink /tmp/qqq.txt
    // "--------------------------------------------------------------------------------\n"
    fprintf(stderr,
            "usage: lcm-logger [options] [FILE]\n"
            "\n"
            "LCM message logging utility. Subscribes to all channels on an LCM\n"
            "network, and records all messages received on that network to\n"
            "FILE. If FILE is not specified, then a filename is automatically\n"
            "chosen.\n"
            "\n"
            "Options:\n"
            "\n"
            "  -c, --channel=CHAN         Channel string to pass to lcm_subscribe.\n"
            "                             (default: \".*\")\n"
            "      --flush-interval=MS    Flush the log file to disk every MS milliseconds.\n"
            "                             (default: 100)\n"
            "  -f, --force                Overwrite existing files.\n"
            "  -h, --help                 Shows this help text and exits.\n"
            "  -i, --increment            Automatically append a suffix to FILE\n"
            "                             such that the resulting filename does not\n"
            "                             already exist.  This option precludes -f and\n"
            "                             --rotate.\n"
            "\n"
            "  -l, --lcm-url=URL          Log messages on the specified LCM URL\n"
            "  -m, --max-unwritten-mb=SZ  Maximum size of received but unwritten\n"
            "                             messages to store in memory before dropping\n"
            "                             messages.  (default: 100 MB)\n"
            "\n"
            "      --rotate=NUM           When creating a new log file, rename existing files\n"
            "                             out of the way and always write to FILE.0.\n"
            "                             If FILE.0 already exists, it is renamed to FILE.1.\n"
            "                             If FILE.1 exists, it is renamed to FILE.2, etc.\n"
            "                             If FILE.NUM exists, then it is deleted.\n"
            "                             This option precludes -i.\n"
            "\n"
            "      --split-mb=N           Automatically start writing to a new log\n"
            "                             file once the log file exceeds N MB in size\n"
            "                             (can be fractional).  This option requires -i\n"
            "                             or --rotate.\n"
            "\n"
            "  -q, --quiet                Suppress normal output and only report errors.\n"
            "  -a, --append               Append events to the given log file.\n"
            "  -s, --strftime             Format FILE with strftime.\n"
            "  -v, --invert-channels      Invert channels.  Log everything that CHAN\n"
            "                             does not match.\n"
            "\n"
            "      --disk-quota=SIZE      Minimum amount of free space to reserve on the disk\n"
            "                             being written to. lcm-logger will exit when it sees\n"
            "                             the current free disk space has fallen below the\n"
            "                             quota minimum.\n"
            "                             For example, given `du -H` reports 15 GB free, and\n"
            "                             that nothing else is changing the target disk,\n"
            "                             --disk-quota=10GB means that lcm-logger will not\n"
            "                             write more than 5 GB before it exits.\n"
            "                             Units accepted: B\n"
            "                             1024: K, M, G, T, P, E, Ki, Mi, Gi, Ti, Pi, Ei, KiB, "
            "MiB, GiB, TiB, PiB, EiB\n"
            "                             1000: KB, MB, GB, TB, PB, EB\n"
            "                             e.g. \"50G\" or \"0.05 TiB\". \n"
            "                             Units are treated case insensitively. gb is treated\n"
            "                             as GB (gigabytes) and not as gigabits.\n"
            "                             Note for `df`: -h is 1024 and -H is 1000 units.\n"
            "                             NOT currently supported on Windows!\n"
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
            "    A user may send SIGHUP with the kill command to trigger rotating logs.\n"
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
    logger.disk_quota = 0;

    char *lcmurl = NULL;

    // Arg Parsing:
    // https://www.gnu.org/software/libc/manual/html_node/Getopt.html
    // https://linux.die.net/man/3/getopt
    char *optstring = "ac:fhil:m:qsu:v";

    // Keep sorted based on the 4th (`val`) arg which needs to be a unique character (int) per
    // option.
    // If a short option is desired, it must be placed above in the optstring.
    struct option long_opts[] = {
        {"append", no_argument, 0, 'a'},
        {"split-mb", required_argument, 0, 'b'},
        {"channel", required_argument, 0, 'c'},
        {"force", no_argument, 0, 'f'},
        {"help", no_argument, 0, 'h'},
        {"increment", required_argument, 0, 'i'},
        {"lcm-url", required_argument, 0, 'l'},
        {"max-unwritten-mb", required_argument, 0, 'm'},
        {"quiet", no_argument, 0, 'q'},
        {"rotate", required_argument, 0, 'r'},
        {"strftime", no_argument, 0, 's'},
        {"flush-interval", required_argument, 0, 'u'},
        {"invert-channels", no_argument, 0, 'v'},
        {"disk-quota", required_argument, 0, 128},
        {0, 0, 0, 0},
    };

    int c;
    while ((c = getopt_long(argc, argv, optstring, long_opts, 0)) >= 0) {
        switch (c) {
        case 'b': /* --split-mb */
            logger.auto_split_mb = strtod(optarg, NULL);
            if (logger.auto_split_mb <= 0) {
                usage();
                return 1;
            }
            break;
        case 'f': /* force */
            logger.force_overwrite = 1;
            break;
        case 'c': /* --channel */
            free(chan_regex);
            chan_regex = strdup(optarg);
            break;
        case 'i': /* --increment */
            logger.auto_increment = 1;
            break;
        case 's': /* strftime */
            logger.use_strftime = 1;
            break;
        case 'l': /* --lcm-url */
            free(lcmurl);
            lcmurl = strdup(optarg);
            break;
        case 'q': /* quiet */
            logger.quiet = 1;
            break;
        case 'v': /* --invert-channels */
            logger.invert_channels = 1;
            break;
        case 'm': /* --max-unwritten-mb */
            max_write_queue_size_mb = strtod(optarg, NULL);
            if (max_write_queue_size_mb <= 0) {
                usage();
                return 1;
            }
            break;
        case 'r': { /* --rotate */
            char *eptr = NULL;
            logger.rotate = strtol(optarg, &eptr, 10);
            if (*eptr) {
                usage();
                return 1;
            }
        } break;
        case 'u': /* --flush-interval */
            logger.fflush_interval_ms = atol(optarg);
            if (logger.fflush_interval_ms <= 0) {
                usage();
                return 1;
            }
            break;
        case 'a': /* --append */
            logger.append = 1;
            break;

        case 128: {
#ifdef WIN32
            printf("--disk-quota not yet supported on windows.\n");
            return 1;
#endif
            logger.disk_quota = parse_mem_size(optarg);
            if (logger.disk_quota == -1 || logger.disk_quota == 0) {
                printf("--disk-quota: Invalid argument\n\n");
                usage();
                return 1;
            }
        } break;

        //
        case 'h':
        default: /* implicit --help */
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
    logger.write_directory = g_path_get_dirname(logger.input_fname);
    if (logger.disk_quota) {
        GFormatSizeFlags fflags = 0 | G_FORMAT_SIZE_IEC_UNITS;

        // https://docs.gtk.org/glib/flags.FormatSizeFlags.html
        // > G_FORMAT_SIZE_IEC_UNITS
        // > Use IEC (base 1024) units with “KiB”-style suffixes. IEC units should only be used for
        // > reporting things with a strong “power of 2” basis, like RAM sizes or RAID stripe sizes.
        // > Network and storage sizes should be reported in the normal SI units.
        //
        // HOWEVER, `df -h` (--human-readable) does 1024. -H (--si) uses 1000, but that is less
        // common?

        int64_t free_bytes = get_free_disk_bytes(logger.write_directory);
        char *free_str = g_format_size_full(free_bytes, fflags);
        char *quota_str = g_format_size_full(logger.disk_quota, fflags);
        printf("There is currently %s free on disk.\n", free_str);
        if (free_bytes < logger.disk_quota) {
            printf("\nAttention:\n");
            printf("\tThat is less than the quota of %s.\n", quota_str);
            printf(
                "\tRefusing to log anything. Please clean up disk space or decrease the quota.\n");
            return 1;
        }
        printf("lcm-logger will exit should that value reach %s.\n", quota_str);

        g_free(free_str);
        g_free(quota_str);
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

    logger.time0 = g_get_real_time();
    logger.max_write_queue_size = (int64_t) (max_write_queue_size_mb * (1 << 20));

    if (0 != open_logfile(&logger))
        return 1;

    /* THREADING:
     *
     * 1 (main):
     *      `g_main_loop_run(_mainloop)` effectively does
     *      `while (not_interrupted) lcm_handle(logger.lcm);`.
     *      That in turn calls message_handler for every message.
     *      Messages are copied into a logger_event_t and pushed into
     *      the logger.write_queue.
     *      When a stop signal (Ctrl+C) is received, glib returns control to `main`.
     *      A sentinel value is then pushed to the write_queue to indicate that
     *      the write thread should stop.
     *
     * 2 (logger.write_thread):
     *      Dumps the write_queue to disk.
     *      Stops when the sentinel value is found in the queue.
     *
     * The address of logger.sync.write_thread_exit_flag is used as the sentinel value.
     * The value itself is not being used...
     *
     */

    // create write thread
    logger.sync.write_thread_exit_flag = 0;
    g_mutex_init(&logger.mutex);
    logger.sync.write_queue_size = 0;
    logger.write_queue = g_async_queue_new();
    logger.write_thread = g_thread_new(NULL, write_thread, &logger);

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

    // ------------------------------------------------------------------------
    // main loop - Returns after a stop signal (Ctrl-C)

    g_main_loop_run(_mainloop);

    // ------------------------------------------------------------------------

    fprintf(stderr, "\nLogger exiting\n");

    {  // LOCK
        // stop the write thread
        g_mutex_lock(&logger.mutex);
        logger.sync.write_thread_exit_flag = 1;
        g_mutex_unlock(&logger.mutex);
    }

    void *stop_sentinel = &(logger.sync.write_thread_exit_flag);
    g_async_queue_push(logger.write_queue, stop_sentinel);
    g_thread_join(logger.write_thread);

    g_mutex_clear(&logger.mutex);

    // cleanup.  This isn't strictly necessary, do it to be pedantic and so that
    // leak checkers don't complain
    glib_mainloop_detach_lcm(logger.lcm);
    lcm_destroy(logger.lcm);
    lcm_eventlog_destroy(logger.log);

    g_free(logger.write_directory);

    for (void *msg = g_async_queue_try_pop(logger.write_queue); msg;
         msg = g_async_queue_try_pop(logger.write_queue)) {
        if (msg == &logger.sync.write_thread_exit_flag)
            continue;
        free(msg);
    }
    g_async_queue_unref(logger.write_queue);

    if (logger.invert_channels) {
        g_regex_unref(logger.regex);
    }

    return 0;
}
