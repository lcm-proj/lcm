#include <gtest/gtest.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#include <lcm/lcm.h>

#include "common.h"

#define info(...)             \
    do {                      \
        printf("c_client: "); \
        printf(__VA_ARGS__);  \
        printf("\n");         \
    } while (0)

static lcm_t *g_lcm = NULL;

// ====================================== node_t test
#define MAKE_CLIENT_TEST(type, num_iters)                                                        \
                                                                                                 \
    static int g_##type##_response_count = 0;                                                    \
                                                                                                 \
    static void type##_handler(const lcm_recv_buf_t *rbuf, const char *channel, const type *msg, \
                               void *user)                                                       \
    {                                                                                            \
        if (!check_##type(msg, g_##type##_response_count + 1)) {                                 \
            return;                                                                              \
        }                                                                                        \
        g_##type##_response_count++;                                                             \
    }                                                                                            \
                                                                                                 \
    static int do_##type##_test(void)                                                            \
    {                                                                                            \
        type msg;                                                                                \
        type##_subscription_t *subs =                                                            \
            type##_subscribe(g_lcm, "test_" #type "_reply", type##_handler, NULL);               \
        g_##type##_response_count = 0;                                                           \
        int result = 1;                                                                          \
        int iter;                                                                                \
        for (iter = 0; iter < num_iters && result; iter++) {                                     \
            fill_##type(iter, &msg);                                                             \
            type##_publish(g_lcm, "test_" #type, &msg);                                          \
            if (!lcm_handle_timeout(g_lcm, 500)) {                                               \
                info(#type " test: Timeout waiting for reply");                                  \
                result = 0;                                                                      \
            } else if (g_##type##_response_count != iter + 1) {                                  \
                info(#type " test: failed on iteration %d", iter);                               \
                result = 0;                                                                      \
            }                                                                                    \
            clear_##type(&msg);                                                                  \
        }                                                                                        \
        type##_unsubscribe(g_lcm, subs);                                                         \
        return result;                                                                           \
    }

MAKE_CLIENT_TEST(lcmtest2_cross_package_t, 100);
MAKE_CLIENT_TEST(lcmtest_multidim_array_t, 5);
MAKE_CLIENT_TEST(lcmtest_node_t, 7);
MAKE_CLIENT_TEST(lcmtest_primitives_list_t, 100);
MAKE_CLIENT_TEST(lcmtest_primitives_t, 1000);

// ================================= echo test
int g_echo_response_count = 0;
int g_echo_msg_len = 0;
uint8_t *g_echo_data = NULL;

static void echo_handler(const lcm_recv_buf_t *rbuf, const char *channel, void *user)
{
    if (rbuf->data_size != g_echo_msg_len)
        return;
    if (memcmp(rbuf->data, g_echo_data, rbuf->data_size))
        return;
    g_echo_response_count++;
}

TEST(LCM_C, EchoTest)
{
    srand(time(NULL));
    g_lcm = lcm_create(NULL);
    ASSERT_TRUE(g_lcm != NULL);

    int maxlen = 10000;
    int minlen = 10;
    g_echo_data = (uint8_t *) malloc(maxlen);
    lcm_subscription_t *subs = lcm_subscribe(g_lcm, "TEST_ECHO_REPLY", echo_handler, NULL);
    g_echo_response_count = 0;

    int iter;
    for (iter = 0; iter < 100; iter++) {
        g_echo_msg_len = rand() % (maxlen - minlen) + minlen;
        int i;
        for (i = 0; i < g_echo_msg_len; i++)
            g_echo_data[i] = rand() % 256;

        lcm_publish(g_lcm, "TEST_ECHO", g_echo_data, g_echo_msg_len);

        ASSERT_GT(lcm_handle_timeout(g_lcm, 500), 0);
        ASSERT_EQ(g_echo_response_count, iter + 1);

        if (g_echo_response_count != iter + 1) {
            info("echo test failed to receive response on iteration %d", iter);
            lcm_unsubscribe(g_lcm, subs);
            free(g_echo_data);
            return;
        }
    }

    lcm_unsubscribe(g_lcm, subs);
    free(g_echo_data);
}

// Typed tests
TEST(LCM_C, primitives_t)
{
    ASSERT_TRUE(g_lcm != NULL);
    EXPECT_EQ(1, do_lcmtest_primitives_t_test());
}

TEST(LCM_C, primitives_list_t)
{
    ASSERT_TRUE(g_lcm != NULL);
    EXPECT_EQ(1, do_lcmtest_primitives_list_t_test());
}

TEST(LCM_C, node_t)
{
    ASSERT_TRUE(g_lcm != NULL);
    EXPECT_EQ(1, do_lcmtest_node_t_test());
}

TEST(LCM_C, multidim_array_t)
{
    ASSERT_TRUE(g_lcm != NULL);
    EXPECT_EQ(1, do_lcmtest_multidim_array_t_test());
}

TEST(LCM_C, cross_package)
{
    ASSERT_TRUE(g_lcm != NULL);
    EXPECT_EQ(1, do_lcmtest2_cross_package_t_test());
    lcm_destroy(g_lcm);
}
