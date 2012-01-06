#include <stdio.h>
#include <stdlib.h>
#include <getopt.h>

#include <string.h>

#include <lcm/lcm.h>


typedef struct logplayer logplayer_t;
struct logplayer
{
    lcm_t * lcm_in;
    lcm_t * lcm_out;
    int verbose;
};

void
handler (const lcm_recv_buf_t *rbuf, const char *channel, void *u)
{
    logplayer_t * l = (logplayer_t *) u;

    if (l->verbose)
        printf ("%.3f Channel %-20s size %d\n", rbuf->recv_utime / 1000000.0,
                channel, rbuf->data_size);

    lcm_publish (l->lcm_out, channel, rbuf->data, rbuf->data_size);
}

static void
usage (char * cmd)
{
    fprintf (stderr, "\
Usage: %s [OPTION...] FILE\n\
  Reads packets from an LCM log file and publishes them to LCM.\n\
\n\
Options:\n\
  -v, --verbose       Print information about each packet.\n\
  -s, --speed=NUM     Playback speed multiplier.  Default is 1.0.\n\
  -e, --regexp=EXPR   GLib regular expression of channels to play.\n\
  -l, --lcm-url=URL   Play logged messages on the specified LCM URL.\n\
  -h, --help          Shows some help text and exits.\n\
  \n", cmd);
}

int
main(int argc, char ** argv)
{
    logplayer_t l;
    double speed = 1.0;
    int c;
    char * expression = NULL;
    struct option long_opts[] = { 
        { "help", no_argument, 0, 'h' },
        { "speed", required_argument, 0, 's' },
        { "lcm-url", required_argument, 0, 'l' },
        { "verbose", no_argument, 0, 'v' },
        { "regexp", required_argument, 0, 'e' },
        { 0, 0, 0, 0 }
    };

    char *lcmurl = NULL;
    memset (&l, 0, sizeof (logplayer_t));
    while ((c = getopt_long (argc, argv, "hp:s:ve:", long_opts, 0)) >= 0)
    {
        switch (c) {
            case 's':
                speed = strtod (optarg, NULL);
                break;
            case 'l':
                free(lcmurl);
                lcmurl = strdup(optarg);
                break;
            case 'v':
                l.verbose = 1;
                break;
            case 'e':
                expression = strdup (optarg);
                break;
            case 'h':
            default:
                usage (argv[0]);
                return 1;
        };
    }

    if (optind != argc - 1) {
        usage (argv[0]);
        return 1;
    }

    char * file = argv[optind];
    printf ("Using playback speed %f\n", speed);
    if (!expression)
        expression = strdup (".*");
#ifndef WIN32
    char url_in[strlen(file) + 64];
#else
    char url_in[2048];
#endif
    sprintf (url_in, "file://%s?speed=%f", argv[optind], speed);
    l.lcm_in = lcm_create (url_in);
    if (!l.lcm_in) {
        fprintf (stderr, "Error: Failed to open %s\n", file);
        free(expression);
        return 1;
    }

    l.lcm_out = lcm_create (lcmurl);
    free(lcmurl);
    if (!l.lcm_out) {
        fprintf (stderr, "Error: Failed to create LCM\n");
        free(expression);
        return 1;
    }

    lcm_subscribe (l.lcm_in, expression, handler, &l);

    while (!lcm_handle (l.lcm_in));

    lcm_destroy (l.lcm_in);
    lcm_destroy (l.lcm_out);
    free (expression);
}
