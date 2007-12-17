#include <stdio.h>
#include <stdint.h>
#include <string.h>
#include <assert.h>
#include <stdlib.h>
#include <inttypes.h>
#include <arpa/inet.h>

#define MAGIC ((int32_t) 0xEDA1DA01L)

static inline int fwrite32(FILE *f, int32_t v)
{
    v = htonl(v);
    if (fwrite(&v, 4, 1, f) == 1) return 0; else return -1;
}

static inline int fwrite64(FILE *f, int64_t v64)
{
    int32_t v = v64>>32;
    if (0 != fwrite32(f, v)) return -1;
    v = v64 & 0xffffffff;
    return fwrite32(f, v);
}

static inline int fread32(FILE *f, int32_t *v32)
{
    int32_t v;

    if (fread(&v, 4, 1, f) != 1)
        return -1;

    *v32 = ntohl(v);

    return 0;
}

static inline int fread64(FILE *f, int64_t *v64)
{
    int32_t v1, v2;

    if (fread32(f, &v1))
        return -1;

    if (fread32(f, &v2))
        return -1;

    *v64 =     (((int64_t) v1)<<32) | (((int64_t) v2)&0xffffffff);

    return 0;
}

typedef struct
{
    FILE *f;
    int64_t eventcount;
} eventlog_t;

typedef struct
{
    int64_t eventnum, timestamp;
    int32_t channellen, datalen;

    char     *channel;
    char     *data;
} eventlog_event_t;

// mode must be "r" or "w"
static eventlog_t      *eventlog_create(const char *path, const char *mode);

// read the next event; free the returned structure with log_free_event
static eventlog_event_t *eventlog_read_next_event(eventlog_t *l);
static void        eventlog_free_event(eventlog_event_t *le);

// seek (approximately) to particular timestamp
static int         eventlog_seek_to_timestamp(eventlog_t *l, int64_t time);

// eventnum will be filled in for you
static int         eventlog_write_event(eventlog_t *l, eventlog_event_t *le);

// when you're done with the log, clean up after yourself!
static void        eventlog_destroy(eventlog_t *l);
static eventlog_t *eventlog_create(const char *path, const char *mode)
{
    assert(!strcmp(mode, "r") || !strcmp(mode, "w"));

    eventlog_t *l = (eventlog_t*) calloc(1, sizeof(eventlog_t));

    l->f = fopen(path, mode);
    if (l->f == NULL) {
        free (l);
        return NULL;
    }

    l->eventcount = 0;

    return l;
}

static void eventlog_destroy(eventlog_t *l)
{
    fclose(l->f);
    free(l);
}

static eventlog_event_t *eventlog_read_next_event(eventlog_t *l)
{
    eventlog_event_t *le = 
        (eventlog_event_t*) calloc(1, sizeof(eventlog_event_t));
    
    int32_t magic = 0;
    int r;

    do {
        r = fgetc(l->f);
        if (r < 0) goto eof;
        magic = (magic << 8) | r;
    } while( magic != MAGIC );

    fread64(l->f, &le->eventnum);
    fread64(l->f, &le->timestamp);
    fread32(l->f, &le->channellen);
    fread32(l->f, &le->datalen);

    assert (le->channellen < 1000);

    if (l->eventcount != le->eventnum) {
        printf ("Mismatch: eventcount %"PRId64" eventnum %"PRId64"\n", 
                l->eventcount, le->eventnum);
        printf ("file offset %"PRId64"\n", ftello (l->f));
        l->eventcount = le->eventnum;
    }

    le->channel = calloc(1, le->channellen+1);
    if (fread(le->channel, 1, le->channellen, l->f) != (size_t) le->channellen)
        goto eof;

    le->data = calloc(1, le->datalen+1);
    if (fread(le->data, 1, le->datalen, l->f) != (size_t) le->datalen)
        goto eof;
    
    l->eventcount++;

    return le;

eof:
    return NULL;
}

static int eventlog_write_event(eventlog_t *l, eventlog_event_t *le)
{
    if (0 != fwrite32(l->f, MAGIC)) return -1;

    le->eventnum = l->eventcount;

    if (0 != fwrite64(l->f, le->eventnum)) return -1;
    if (0 != fwrite64(l->f, le->timestamp)) return -1;
    if (0 != fwrite32(l->f, le->channellen)) return -1;
    if (0 != fwrite32(l->f, le->datalen)) return -1;

    if (le->channellen != fwrite(le->channel, 1, le->channellen, l->f)) 
        return -1;
    if (le->datalen != fwrite(le->data, 1, le->datalen, l->f))
        return -1;

    l->eventcount++;

    return 0;
}

static void eventlog_free_event(eventlog_event_t *le)
{
    if (le->data) free(le->data);
    if (le->channel) free(le->channel);
    memset(le,0,sizeof(eventlog_event_t));
    free(le);
}



static int64_t get_event_time(eventlog_t *l)
{
    int32_t magic = 0;
    int r;

    do {
        r = fgetc(l->f);
        if (r < 0) goto eof;
        magic = (magic << 8) | r;
    } while( magic != MAGIC );

    int64_t event_num = 0;
    int64_t time = 0;
    if (0 != fread64(l->f, &event_num)) return -1;
    if (0 != fread64(l->f, &time)) return -1;
    fseeko (l->f, -20, SEEK_CUR);

    l->eventcount = event_num;

    return time;

eof:
    return -1;
}


static int eventlog_seek_to_timestamp(eventlog_t *l, int64_t time)
{
    fseeko (l->f, 0, SEEK_END);
    off_t file_len = ftello(l->f);

    int64_t cur_time;
    double frac1 = 0;               // left bracket
    double frac2 = 1;               // right bracket
    double prev_frac = -1;
    double frac;                    // current position

    while (1) {
        frac = 0.5*(frac1+frac2);
        off_t offset = (off_t)(frac*file_len);
        fseeko (l->f, offset, SEEK_SET);
        cur_time = get_event_time (l);
        if (cur_time < 0)
            return -1;

        frac = (double)ftello (l->f)/file_len;
        if ((frac > frac2) || (frac < frac1) || (frac1>=frac2))
            break;
    
        double df = frac-prev_frac;
        if (df < 0)
            df = -df;
        if (df < 1e-12)
            break;

        if (cur_time == time)
            break;

        if (cur_time < time)
            frac1 = frac;
        else
            frac2 = frac;

        prev_frac = frac;
    }

    return 0;
}
