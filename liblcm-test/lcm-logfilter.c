// file: lcm-logfilter.c
// desc: utility to selectively extract channels from a logfile into a new one

#include <stdio.h>
#include <stdlib.h>
#include <getopt.h>

#ifndef WIN32
#include <regex.h>
#else
#define USE_GREGEX
#endif

#include <glib.h>

#include <lcm/lcm.h>

static void 
usage()
{
    printf("usage: lcm-logfilter -c <CHAN> [OPTIONS] <source_logfile> <dest_logfile>\n"
           "\n"
           "Selectively extract channels from a source logfile to a destination\n"
           "logfile.\n"
           "\n"
           "Optiosn:\n"
           "  -h        prints this help text and exits\n"
           "  -c CHAN   POSIX regular expression.  Channels matching this expression\n"
           "            will be copied to the destination logfile.\n"
           "  -i        invert the regular expression CHAN, so that only channels not\n"
           "  -c CHAN   GLib regular expression.\n"
           "  -s START  start time.  Messages logged less than START seconds\n"
           "            after the first message in the logfile will not be\n"
           "            extracted.\n"
           "  -e END    end time.  Messages logged more than END seconds\n"
           "            after the first message in the logfile will not be\n"
           "            extracted.\n"
           "  -v        verbose mode. Prints a summary of channels extracted\n"
           );
    exit(1);
}

static void
_verbose_entry_summary(gpointer key, gpointer value, gpointer user_data)
{
    printf("%20s: %d\n", (char*)key, *((int*)value));
}

int main(int argc, char **argv)
{
    int verbose = 0;
    char *pattern = NULL;
    char *source_fname = NULL;
    char *dest_fname = NULL;
    int64_t start_utime = 0;
    int64_t end_utime = -1;
    int have_end_utime = 0;
    int invert_regex = 0;

    char *optstring = "hc:vs:e:i";
    char c;

    while ((c = getopt(argc, argv, optstring)) >= 0)
    {
        switch (c) {
            case 'h':
                usage();
                break;
            case 's':
                {
                    char *eptr = NULL;
                    double start_time = strtod(optarg, &eptr);
                    if(*eptr != 0)
                        usage();
                    start_utime = (int64_t) (start_time * 1000000);
                }
                break;
            case 'e':
                {
                    char *eptr = NULL;
                    double end_time = strtod(optarg, &eptr);
                    if(*eptr != 0)
                        usage();
                    end_utime = (int64_t) (end_time * 1000000);
                    have_end_utime = 1;
                }
                break;
            case 'i':
                invert_regex = 1;
                break;
            case 'c':
                pattern = g_strdup(optarg);
                break;
            case 'v':
                verbose = 1;
                break;
            default:
                usage();
                break;
        };
    }

    if(start_utime < 0 || (have_end_utime && end_utime < start_utime))
        usage();

    if (optind != argc - 2)
        usage();

    if (!pattern)
        usage();

#ifdef USE_GREGEX
    GRegex * regex;
    GError *rerr = NULL;
    regex = g_regex_new(pattern, (GRegexCompileFlags) 0, (GRegexMatchFlags) 0, &rerr);
    if(rerr) {
        fprintf(stderr, "bad regex\n");
        exit(1);
    }
#else
    regex_t preg;

    if (0 != regcomp(&preg, pattern, REG_NOSUB | REG_EXTENDED)) {
        fprintf(stderr, "bad regex\n");
        exit(1);
    }
#endif

    source_fname = argv[argc - 2];
    dest_fname = argv[argc - 1];

    lcm_eventlog_t *src_log = lcm_eventlog_create(source_fname, "r");
    if (!src_log) {
        perror("Unable to open source logfile");
#ifdef USE_GREGEX
		g_regex_unref(regex);
#else
        regfree(&preg);
#endif
        return 1;
    }
    lcm_eventlog_t *dst_log = lcm_eventlog_create(dest_fname, "w");
    if (!dst_log) {
        perror("Unable to open destination logfile");
        lcm_eventlog_destroy(src_log);
#ifdef USE_GREGEX
		g_regex_unref(regex);
#else
        regfree(&preg);
#endif
        return 1;
    }

    GHashTable *counts = g_hash_table_new_full(g_str_hash, g_str_equal,
            g_free, free);
    int nwritten = 0;
    int have_first_event_timestamp = 0;
    int64_t first_event_timestamp = 0;

    for (lcm_eventlog_event_t *event = lcm_eventlog_read_next_event(src_log);
            event != NULL;
            event = lcm_eventlog_read_next_event(src_log)) {
        if(!have_first_event_timestamp) {
            first_event_timestamp = event->timestamp;
            have_first_event_timestamp = 1;
        }

        int64_t elapsed = event->timestamp - first_event_timestamp;
        if(elapsed < start_utime) {
            lcm_eventlog_free_event(event);
            continue;
        }
        if(have_end_utime && elapsed > end_utime) {
            lcm_eventlog_free_event(event);
            break;
        }

#ifdef USE_GREGEX
		int regmatch =  g_regex_match(regex, event->channel, (GRegexMatchFlags) 0, NULL);
#else
        int regmatch = regexec(&preg, event->channel, 0, NULL, 0);
#endif
        int copy_to_dest = (regmatch == 0 && !invert_regex) ||
                           (regmatch != 0 && invert_regex);
        if (copy_to_dest) {
            lcm_eventlog_write_event(dst_log, event);
            nwritten++;

            if (verbose)  {
                int *count = (int *) g_hash_table_lookup(counts, event->channel);
                if (!count) {
                    count = (int*) malloc(sizeof(int));
                    *count = 1;
                    g_hash_table_insert(counts, g_strdup(event->channel), count);
                    printf("matched channel %s\n", event->channel);
                } else {
                    *count += 1;
                }
            }
        }
        lcm_eventlog_free_event(event);
    }

    if (verbose) {
        g_hash_table_foreach(counts, _verbose_entry_summary, NULL);
        printf("=====\n");
        printf("Events written: %d\n", nwritten);
    }
    
#ifdef USE_GREGEX
	g_regex_unref(regex);
#else
	regfree(&preg);
#endif
    lcm_eventlog_destroy(src_log);
    lcm_eventlog_destroy(dst_log);
    g_hash_table_destroy(counts);
    return 0;
}
