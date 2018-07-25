#include <gtest/gtest.h>
#include <stdlib.h>
#include <string.h>

#include <lcm/lcm.h>
#include "common.h"

TEST(LCM_C, EventLogBasic)
{
    // Open and close a log file.
    char *fname = make_tmpnam();

    lcm_eventlog_t *wlog = lcm_eventlog_create(fname, "w");
    EXPECT_NE((void *) NULL, wlog);
    lcm_eventlog_destroy(wlog);

    free_tmpnam(fname);
}

TEST(LCM_C, EventLogWriteRead)
{
    // Write some events to a log, then read them back in and check what was
    // read is the same as what was written.
    char *fname = make_tmpnam();

    lcm_eventlog_t *wlog = lcm_eventlog_create(fname, "w");
    EXPECT_NE((void *) NULL, wlog);

    const char *channel = "CHANNEL_TEST";
    const int channellen = strlen(channel);
    const int datalen = 256;
    char data[datalen];

    lcm_eventlog_event_t event;
    event.channellen = channellen;
    event.channel = const_cast<char *>(channel);
    event.datalen = datalen;
    event.data = data;

    const int byte_increment = 13;
    const int num_events = 100;
    char next_byte = 5;
    for (int event_num = 0; event_num < num_events; ++event_num) {
        event.timestamp = event_num;
        for (int byte_num = 0; byte_num < datalen; ++byte_num) {
            data[byte_num] = next_byte;
            next_byte += byte_increment;
        }
        EXPECT_EQ(0, lcm_eventlog_write_event(wlog, &event));
    }

    lcm_eventlog_destroy(wlog);

    // Read the data back in
    lcm_eventlog_t *rlog = lcm_eventlog_create(fname, "r");
    EXPECT_NE((void *) NULL, rlog);

    lcm_eventlog_event_t *revent = NULL;
    char expected_next_byte = 5;
    for (int event_num = 0; event_num < num_events; ++event_num) {
        revent = lcm_eventlog_read_next_event(rlog);
        EXPECT_NE((void *) NULL, revent);

        EXPECT_EQ(channellen, revent->channellen);
        EXPECT_EQ(datalen, revent->datalen);
        EXPECT_EQ(event_num, revent->timestamp);

        EXPECT_EQ(0, strcmp(channel, revent->channel));

        bool bytes_match = true;
        if (datalen == revent->datalen) {
            for (int byte_num = 0; byte_num < datalen; ++byte_num) {
                bytes_match &= (((const char *) revent->data)[byte_num] == expected_next_byte);
                expected_next_byte += byte_increment;
            }
        }
        EXPECT_TRUE(bytes_match);
        lcm_eventlog_free_event(revent);
    }

    lcm_eventlog_destroy(rlog);
    free_tmpnam(fname);
}

TEST(LCM_C, EventLogCorrupt)
{
    // Tests detection of corrupt data.
    char *fname = make_tmpnam();

    lcm_eventlog_t *wlog = lcm_eventlog_create(fname, "w");
    ASSERT_NE((void *) NULL, wlog);

    // Write two valid events, then garbage, then another valid event.
    const char *channel = "CHANNEL_TEST";
    const int channellen = strlen(channel);
    const int datalen = 256;
    char data[datalen];
    memset(data, 127, datalen);

    lcm_eventlog_event_t event;
    event.timestamp = 0;
    event.channellen = channellen;
    event.channel = const_cast<char *>(channel);
    event.datalen = datalen;
    event.data = data;

    // The two valid events
    EXPECT_EQ(0, lcm_eventlog_write_event(wlog, &event));
    EXPECT_EQ(0, lcm_eventlog_write_event(wlog, &event));

    // Garbage
    EXPECT_EQ(datalen, fwrite(data, 1, datalen, wlog->f));

    // A valid event
    EXPECT_EQ(0, lcm_eventlog_write_event(wlog, &event));

    lcm_eventlog_destroy(wlog);

    // Now try reading the corrupt log back in.
    lcm_eventlog_t *rlog = lcm_eventlog_create(fname, "r");
    // First event should be valid.
    lcm_eventlog_event_t *revent = lcm_eventlog_read_next_event(rlog);
    ASSERT_NE((void *) NULL, revent);
    lcm_eventlog_free_event(revent);

    // Second event is not because it's not followed by EOF or an event header.
    revent = lcm_eventlog_read_next_event(rlog);
    EXPECT_EQ((void *) NULL, revent);

    lcm_eventlog_destroy(rlog);
    free_tmpnam(fname);
}
