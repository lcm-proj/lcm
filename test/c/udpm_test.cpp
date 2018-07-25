#ifndef WIN32
#include <time.h>
#endif

#include <gtest/gtest.h>

#include <lcm/lcm.h>

TEST(LCM_C, InvalidCreation)
{
    lcm_t *lcm = lcm_create("udpm://asdf");
    EXPECT_EQ(NULL, lcm);

    lcm = lcm_create("udpm://0.0.0.0");
    EXPECT_EQ(NULL, lcm);

    lcm = lcm_create("udpm://239.255.1.1:65536");
    EXPECT_EQ(NULL, lcm);
}

#ifndef WIN32
static void empty_handler(const lcm_recv_buf_t * /* unused */, const char * /* unused */,
                          void * /* unused */)
{
}

TEST(LCM_C, QueueSize)
{
    lcm_t *lcm = lcm_create(NULL);
    ASSERT_NE((void *) NULL, lcm);

    lcm_subscription_t *subs = lcm_subscribe(lcm, "channel", empty_handler, NULL);
    EXPECT_EQ(0, lcm_subscription_get_queue_size(subs));

    // Set a small queue size
    lcm_subscription_set_queue_capacity(subs, 5);

    // publish a bunch of messages
    for (int i = 0; i < 10; i++) {
        lcm_publish(lcm, "channel", "", 0);
    }

    // sleep for 100 ms. Assume that this is enough time for the messages to get
    // queued up.
    struct timespec sleeptime;
    sleeptime.tv_sec = 0;
    sleeptime.tv_nsec = 100000000;
    nanosleep(&sleeptime, NULL);

    // Handle messages one by one
    for (int i = 5; i > 0; i--) {
        EXPECT_EQ(i, lcm_subscription_get_queue_size(subs));
        ASSERT_GT(lcm_handle_timeout(lcm, 500), 0);
        EXPECT_EQ(i - 1, lcm_subscription_get_queue_size(subs));
    }

    lcm_destroy(lcm);
}
#endif
