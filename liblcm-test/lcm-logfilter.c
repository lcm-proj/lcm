// file: lcm-logfilter.c
// desc: utility to selectively extract channels from a logfile into a new one

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <regex.h>
#include <getopt.h>

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
           "  -c CHAN   POSIX extended regular expression.\n"
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

    char *optstring = "hc:v";
    char c;

    while ((c = getopt_long (argc, argv, optstring, NULL, 0)) >= 0)
    {
        switch (c) {
            case 'h':
                usage();
                break;
            case 'c':
                pattern = strdup(optarg);
                break;
            case 'v':
                verbose = 1;
                break;
            default:
                usage();
                break;
        };
    }

    if (optind != argc - 2) {
        usage();
    }
    if (!pattern) {
        usage();
    }

    regex_t preg;
    if (0 != regcomp(&preg, pattern, REG_NOSUB | REG_EXTENDED)) {
        fprintf(stderr, "bad regex\n");
        exit(1);
    }

    source_fname = argv[argc - 2];
    dest_fname = argv[argc - 1];

    lcm_eventlog_t *src_log = lcm_eventlog_create(source_fname, "r");
    if (!src_log) {
        perror("Unable to open source logfile");
        regfree(&preg);
        return 1;
    }
    lcm_eventlog_t *dst_log = lcm_eventlog_create(dest_fname, "w");
    if (!dst_log) {
        perror("Unable to open destination logfile");
        lcm_eventlog_destroy(src_log);
        regfree(&preg);
        return 1;
    }

    GHashTable *counts = g_hash_table_new_full(g_str_hash, g_str_equal,
            free, free);
    int nwritten = 0;

    for (lcm_eventlog_event_t *event = lcm_eventlog_read_next_event(src_log);
            event != NULL;
            event = lcm_eventlog_read_next_event(src_log)) {

        if (0 == regexec(&preg, event->channel, 0, NULL, 0)) {
            lcm_eventlog_write_event(dst_log, event);
            nwritten++;

            if (verbose)  {
                int *count = g_hash_table_lookup(counts, event->channel);
                if (!count) {
                    count = (int*) malloc(sizeof(int));
                    *count = 1;
                    g_hash_table_insert(counts, strdup(event->channel), count);
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
    
    regfree(&preg);
    lcm_eventlog_destroy(src_log);
    lcm_eventlog_destroy(dst_log);
    g_hash_table_destroy(counts);
    return 0;
}
