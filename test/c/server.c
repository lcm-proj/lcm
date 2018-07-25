#include <lcm/lcm.h>
#include <stdio.h>
#include <stdlib.h>

#include "common.h"

static lcm_t *g_lcm = NULL;
static int g_quit = 0;
static int g_lcmtest_primitives_t_count = 0;
static int g_lcmtest_primitives_list_t_count = 0;
static int g_lcmtest_node_t_count = 0;
static int g_lcmtest_multidim_array_t_count = 0;
static int g_lcmtest2_cross_package_t_count = 0;

static void reset_counts()
{
    g_lcmtest_primitives_t_count = 0;
    g_lcmtest_primitives_list_t_count = 0;
    g_lcmtest_node_t_count = 0;
    g_lcmtest_multidim_array_t_count = 0;
    g_lcmtest2_cross_package_t_count = 0;
}

static void lcmtest_primitives_t_handler(const lcm_recv_buf_t *rbuf, const char *channel,
                                         const lcmtest_primitives_t *msg, void *user)
{
    // Reset all counts (maybe)
    if (msg->i64 == 0) {
        reset_counts();
    }

    lcmtest_primitives_t reply;
    fill_lcmtest_primitives_t(g_lcmtest_primitives_t_count + 1, &reply);
    lcmtest_primitives_t_publish(g_lcm, "test_lcmtest_primitives_t_reply", &reply);
    clear_lcmtest_primitives_t(&reply);
    g_lcmtest_primitives_t_count++;
}

static void lcmtest_primitives_list_t_handler(const lcm_recv_buf_t *rbuf, const char *channel,
                                              const lcmtest_primitives_list_t *msg, void *user)
{
    lcmtest_primitives_list_t reply;
    fill_lcmtest_primitives_list_t(g_lcmtest_primitives_list_t_count + 1, &reply);
    lcmtest_primitives_list_t_publish(g_lcm, "test_lcmtest_primitives_list_t_reply", &reply);
    clear_lcmtest_primitives_list_t(&reply);
    g_lcmtest_primitives_list_t_count++;
}

static void lcmtest_node_t_handler(const lcm_recv_buf_t *rbuf, const char *channel,
                                   const lcmtest_node_t *msg, void *user)
{
    lcmtest_node_t reply;
    fill_lcmtest_node_t(g_lcmtest_node_t_count + 1, &reply);
    lcmtest_node_t_publish(g_lcm, "test_lcmtest_node_t_reply", &reply);
    clear_lcmtest_node_t(&reply);
    g_lcmtest_node_t_count++;
}

static void lcmtest_multidim_array_t_handler(const lcm_recv_buf_t *rbuf, const char *channel,
                                             const lcmtest_multidim_array_t *msg, void *user)
{
    lcmtest_multidim_array_t reply;
    fill_lcmtest_multidim_array_t(g_lcmtest_multidim_array_t_count + 1, &reply);
    lcmtest_multidim_array_t_publish(g_lcm, "test_lcmtest_multidim_array_t_reply", &reply);
    clear_lcmtest_multidim_array_t(&reply);
    g_lcmtest_multidim_array_t_count++;
}

static void lcmtest2_cross_package_t_handler(const lcm_recv_buf_t *rbuf, const char *channel,
                                             const lcmtest2_cross_package_t *msg, void *user)
{
    lcmtest2_cross_package_t reply;
    fill_lcmtest2_cross_package_t(g_lcmtest2_cross_package_t_count + 1, &reply);
    lcmtest2_cross_package_t_publish(g_lcm, "test_lcmtest2_cross_package_t_reply", &reply);
    clear_lcmtest2_cross_package_t(&reply);
    g_lcmtest2_cross_package_t_count++;
}

static void echo_handler(const lcm_recv_buf_t *rbuf, const char *channel, void *user)
{
    lcm_publish(g_lcm, "TEST_ECHO_REPLY", rbuf->data, rbuf->data_size);
}

static void quit_handler()
{
    g_quit = 1;
}

// ============================

int main(int argc, char **argv)
{
    g_lcm = lcm_create(NULL);
    if (!g_lcm)
        return 1;

    lcm_subscribe(g_lcm, "TEST_QUIT", (lcm_msg_handler_t) &quit_handler, NULL);

    lcm_subscribe(g_lcm, "TEST_ECHO", &echo_handler, NULL);

    lcmtest_primitives_t_subscribe(g_lcm, "test_lcmtest_primitives_t",
                                   &lcmtest_primitives_t_handler, NULL);

    lcmtest_primitives_list_t_subscribe(g_lcm, "test_lcmtest_primitives_list_t",
                                        &lcmtest_primitives_list_t_handler, NULL);

    lcmtest_node_t_subscribe(g_lcm, "test_lcmtest_node_t", &lcmtest_node_t_handler, NULL);

    lcmtest_multidim_array_t_subscribe(g_lcm, "test_lcmtest_multidim_array_t",
                                       &lcmtest_multidim_array_t_handler, NULL);

    lcmtest2_cross_package_t_subscribe(g_lcm, "test_lcmtest2_cross_package_t",
                                       &lcmtest2_cross_package_t_handler, NULL);

    while (lcm_handle(g_lcm) == 0 && !g_quit) {
        // Do nothing
    }

    lcm_destroy(g_lcm);
    return 0;
}
