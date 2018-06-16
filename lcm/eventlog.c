#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#ifdef WIN32
#define __STDC_FORMAT_MACROS  // Enable integer types
#endif
#include <stdint.h>

#include "eventlog.h"
#include "ioutils.h"

#ifdef WIN32
#include "./windows/WinPorting.h"
#endif

#define MAGIC ((int32_t) 0xEDA1DA01L)

lcm_eventlog_t *lcm_eventlog_create(const char *path, const char *mode)
{
    assert(!strcmp(mode, "r") || !strcmp(mode, "w") || !strcmp(mode, "a"));
    if (*mode == 'w')
        mode = "wb";
    else if (*mode == 'r')
        mode = "rb";
    else if (*mode == 'a')
        mode = "ab";
    else
        return NULL;

    lcm_eventlog_t *l = (lcm_eventlog_t *) calloc(1, sizeof(lcm_eventlog_t));

    l->f = fopen(path, mode);
    if (l->f == NULL) {
        free(l);
        return NULL;
    }

    l->eventcount = 0;

    return l;
}

void lcm_eventlog_destroy(lcm_eventlog_t *l)
{
    fflush(l->f);
    fclose(l->f);
    free(l);
}

lcm_eventlog_event_t *lcm_eventlog_read_next_event(lcm_eventlog_t *l)
{
    lcm_eventlog_event_t *le = (lcm_eventlog_event_t *) calloc(1, sizeof(lcm_eventlog_event_t));

    uint32_t magic = 0;
    int r;

    do {
        r = fgetc(l->f);
        if (r < 0) {
            free(le);
            return NULL;
        }
        magic = (magic << 8) | (uint32_t) r;
    } while (magic != MAGIC);

    if (0 != fread64(l->f, &le->eventnum) || 0 != fread64(l->f, &le->timestamp) ||
        0 != fread32(l->f, &le->channellen) || 0 != fread32(l->f, &le->datalen)) {
        free(le);
        return NULL;
    }

    // Sanity check the channel length and data length
    if (le->channellen <= 0 || le->channellen >= 1000) {
        fprintf(stderr, "Log event has invalid channel length: %d\n", le->channellen);
        free(le);
        return NULL;
    }
    if (le->datalen < 0) {
        fprintf(stderr, "Log event has invalid data length: %d\n", le->datalen);
        free(le);
        return NULL;
    }

    le->channel = (char *) calloc(1, le->channellen + 1);
    if (fread(le->channel, 1, le->channellen, l->f) != (size_t) le->channellen) {
        free(le->channel);
        free(le->data);
        free(le);
        return NULL;
    }

    le->data = calloc(1, le->datalen + 1);
    if (fread(le->data, 1, le->datalen, l->f) != (size_t) le->datalen) {
        free(le->channel);
        free(le->data);
        free(le);
        return NULL;
    }

    // Check that there's a valid event or the EOF after this event.
    int32_t next_magic;
    if (0 == fread32(l->f, &next_magic)) {
        if (next_magic != MAGIC) {
            fprintf(stderr, "Invalid header after log data\n");
            free(le->channel);
            free(le->data);
            free(le);
            return NULL;
        }
        fseeko(l->f, -4, SEEK_CUR);
    }
    return le;
}

int lcm_eventlog_write_event(lcm_eventlog_t *l, lcm_eventlog_event_t *le)
{
    if (0 != fwrite32(l->f, MAGIC))
        return -1;

    le->eventnum = l->eventcount;

    if (0 != fwrite64(l->f, le->eventnum))
        return -1;
    if (0 != fwrite64(l->f, le->timestamp))
        return -1;
    if (0 != fwrite32(l->f, le->channellen))
        return -1;
    if (0 != fwrite32(l->f, le->datalen))
        return -1;

    if (le->channellen != fwrite(le->channel, 1, le->channellen, l->f))
        return -1;
    if (le->datalen != fwrite(le->data, 1, le->datalen, l->f))
        return -1;

    l->eventcount++;

    return 0;
}

void lcm_eventlog_free_event(lcm_eventlog_event_t *le)
{
    if (le->data)
        free(le->data);
    if (le->channel)
        free(le->channel);
    memset(le, 0, sizeof(lcm_eventlog_event_t));
    free(le);
}

static int64_t get_event_time(lcm_eventlog_t *l)
{
    uint32_t magic = 0;
    int r;

    do {
        r = fgetc(l->f);
        if (r < 0)
            goto eof;
        magic = (magic << 8) | (uint32_t) r;
    } while (magic != MAGIC);

    int64_t event_num;
    int64_t timestamp;
    if (0 != fread64(l->f, &event_num))
        return -1;
    if (0 != fread64(l->f, &timestamp))
        return -1;
    fseeko(l->f, -20, SEEK_CUR);

    l->eventcount = event_num;

    return timestamp;

eof:
    return -1;
}

int lcm_eventlog_seek_to_timestamp(lcm_eventlog_t *l, int64_t timestamp)
{
    fseeko(l->f, 0, SEEK_END);
    off_t file_len = ftello(l->f);

    int64_t cur_time;
    double frac1 = 0;  // left bracket
    double frac2 = 1;  // right bracket
    double prev_frac = -1;
    double frac;  // current position

    while (1) {
        frac = 0.5 * (frac1 + frac2);
        off_t offset = (off_t)(frac * file_len);
        fseeko(l->f, offset, SEEK_SET);
        cur_time = get_event_time(l);
        if (cur_time < 0)
            return -1;

        if ((frac > frac2) || (frac < frac1) || (frac1 >= frac2))
            break;

        double df = frac - prev_frac;
        if (df < 0)
            df = -df;
        if (df < 1e-12)
            break;

        if (cur_time == timestamp)
            break;

        if (cur_time < timestamp)
            frac1 = frac;
        else
            frac2 = frac;

        prev_frac = frac;
    }

    return 0;
}
