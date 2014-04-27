#include <stdio.h>
#include <sys/types.h>
#include <string.h>
#include <assert.h>
#include <stdlib.h>
#ifdef WIN32
#define __STDC_FORMAT_MACROS			// Enable integer types
#endif
#include <stdint.h>

#include "ioutils.h"
#include "eventlog.h"

#ifdef WIN32
#include "./windows/WinPorting.h"
#endif

#define MAGIC ((int32_t) 0xEDA1DA01L)

lcm_eventlog_t *lcm_eventlog_create(const char *path, const char *mode)
{
    assert(!strcmp(mode, "r") || !strcmp(mode, "w") || !strcmp(mode, "rw") || !strcmp(mode, "a"));
    if (!strcmp(mode, "rw"))
        mode = "wb+";
    else if(!strcmp(mode, "w"))
        mode = "wb";
    else if(!strcmp(mode, "r"))
        mode = "rb";
    else if(!strcmp(mode, "a"))
        mode = "ab";
    else
        return NULL;

    lcm_eventlog_t *l = (lcm_eventlog_t*) calloc(1, sizeof(lcm_eventlog_t));

    l->f = fopen(path, mode);
    if (l->f == NULL) {
        free (l);
        return NULL;
    }

    l->write_event_count = 0;

	// Start with f in read mode. If we write first, the read position will get saved.
    l->f_at_read_pos = 1;

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
    // Move the file pointer to the read position if need be
    if (!l->f_at_read_pos) {
        if(fsetpos(l->f, &l->read_pos)) 
            return NULL;
        l->f_at_read_pos = 1;
    }

    lcm_eventlog_event_t *le = 
        (lcm_eventlog_event_t*) calloc(1, sizeof(lcm_eventlog_event_t));
    
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

    le->channel = (char *) calloc(1, le->channellen+1);
    if (fread(le->channel, 1, le->channellen, l->f) != (size_t) le->channellen)
        goto eof;

    le->data = calloc(1, le->datalen+1);
    if (fread(le->data, 1, le->datalen, l->f) != (size_t) le->datalen)
        goto eof;
    
    return le;

eof:
    free(le->channel);
    free(le->data);
    free(le);
    return NULL;
}

int lcm_eventlog_write_event(lcm_eventlog_t *l, lcm_eventlog_event_t *le)
{
    // Move the file pointer to the write position if need be
    if (l->f_at_read_pos) {
        if (0 != fgetpos(l->f, &l->read_pos)) 
            return -1;
        l->f_at_read_pos = 0;
        if (0 != fseek(l->f, 0, SEEK_END))
            return -1;
    }

    if (0 != fwrite32(l->f, MAGIC)) return -1;

    le->eventnum = l->write_event_count;

    if (0 != fwrite64(l->f, le->eventnum)) return -1;
    if (0 != fwrite64(l->f, le->timestamp)) return -1;
    if (0 != fwrite32(l->f, le->channellen)) return -1;
    if (0 != fwrite32(l->f, le->datalen)) return -1;

    if (le->channellen != fwrite(le->channel, 1, le->channellen, l->f)) 
        return -1;
    if (le->datalen != fwrite(le->data, 1, le->datalen, l->f))
        return -1;

    l->write_event_count++;

    return 0;
}

void lcm_eventlog_free_event(lcm_eventlog_event_t *le)
{
    if (le->data) free(le->data);
    if (le->channel) free(le->channel);
    memset(le,0,sizeof(lcm_eventlog_event_t));
    free(le);
}



static int64_t get_event_time(lcm_eventlog_t *l)
{
    int32_t magic = 0;
    int r;

    do {
        r = fgetc(l->f);
        if (r < 0) goto eof;
        magic = (magic << 8) | r;
    } while( magic != MAGIC );

    int64_t event_num;
    int64_t timestamp;
    if (0 != fread64(l->f, &event_num)) return -1;
    if (0 != fread64(l->f, &timestamp)) return -1;
    fseeko (l->f, -20, SEEK_CUR);

    return timestamp;

eof:
    return -1;
}


int lcm_eventlog_seek_to_timestamp(lcm_eventlog_t *l, int64_t timestamp)
{
    if (timestamp <= 0){
        // The binary search below has issues searching to the begging of the
        // log due to how it searches for the timestamp in log events.
        // TODO: Fix the logic below somehow, or add functions such as
        // lcm_eventlog_seek_to_fraction() or lcm_eventlog_seek_to_eventnum()?
        fseeko (l->f, 0, SEEK_SET);
        return 0;
    }

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
