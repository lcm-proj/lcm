#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <time.h>

#include <lcm/lcm.h>

#include "common.h"

#define info(...) do { printf("client: "); printf(__VA_ARGS__); } while(0)

static lcm_t* g_lcm = NULL;

// ====================================== node_t test
#define MAKE_CLIENT_TEST(type, num_iters) \
\
static int g_##type##_response_count = 0; \
\
static void \
type##_handler(const lcm_recv_buf_t* rbuf, const char* channel, \
        const type* msg, void* user) \
{ \
    if(!check_##type(msg, g_##type##_response_count + 1)) { \
        return; \
    } \
    g_##type##_response_count++; \
} \
\
static int \
do_##type##_test(void) \
{ \
    type msg; \
    type##_subscription_t* subs = type##_subscribe(g_lcm, \
            "test_" #type "_reply", type##_handler, NULL); \
    g_##type##_response_count = 0; \
    int result = 1; \
    int iter; \
    for(iter=0; iter<num_iters && result; iter++) { \
        fill_##type(iter, &msg); \
        type##_publish(g_lcm, "test_" #type, &msg); \
        if(!_lcm_handle_timeout(g_lcm, 500)) { \
            info(#type " test: Timeout waiting for reply\n"); \
            result = 0; \
        } else if(g_##type##_response_count != iter+1) { \
            info(#type " test: failed on iteration %d\n", iter); \
            result = 0; \
        } \
        clear_##type(&msg); \
    } \
    type##_unsubscribe(g_lcm, subs); \
    if(result) { \
        info("%-25s : PASSED\n", #type); \
    } else { \
        info("%-25s : FAILED\n", #type); \
    } \
    return result; \
}

MAKE_CLIENT_TEST(lcmtest_multidim_array_t, 5);
MAKE_CLIENT_TEST(lcmtest_node_t, 7);
MAKE_CLIENT_TEST(lcmtest_primitives_list_t, 100);
MAKE_CLIENT_TEST(lcmtest_primitives_t, 1000);

// ================================= echo test
int g_echo_response_count = 0;
int g_echo_msg_len = 0;
uint8_t* g_echo_data = NULL;

static void
echo_handler(const lcm_recv_buf_t *rbuf, const char * channel, void * user)
{
    if(rbuf->data_size != g_echo_msg_len)
        return;
    if(memcmp(rbuf->data, g_echo_data, rbuf->data_size))
        return;
    g_echo_response_count++;
}

static int
do_echo_test(void)
{
    int maxlen = 10000;
    int minlen = 10;
    g_echo_data = malloc(maxlen);
    lcm_subscription_t* subs = lcm_subscribe(g_lcm, "TEST_ECHO_REPLY", echo_handler, NULL);
    g_echo_response_count = 0;

    int iter;
    for(iter=0; iter<100; iter++)
    {
        g_echo_msg_len = rand() % (maxlen - minlen) + minlen;
        int i;
        for(i=0; i<g_echo_msg_len; i++)
            g_echo_data[i] = rand() % 256;

        lcm_publish(g_lcm, "TEST_ECHO", g_echo_data, g_echo_msg_len);

        if(!_lcm_handle_timeout(g_lcm, 500) || (g_echo_response_count != iter+1))
        {
            info("echo test failed to receive response on iteration %d\n", iter);
            free(g_echo_data);
            return 0;
        }
    }

    info("%-25s : PASSED\n", "echo test");
    lcm_unsubscribe(g_lcm, subs);
    free(g_echo_data);
    return 1;
}

// ========================== main
int
main(int argc, char ** argv)
{
    srand(time(NULL));

    g_lcm = lcm_create(NULL);
    if(!g_lcm)
    {
        info("Unable to initialize LCM\n");
        return 1;
    }

    if(!do_echo_test())
        goto failed;

    if(!do_lcmtest_primitives_t_test())
        goto failed;

    if(!do_lcmtest_primitives_list_t_test())
        goto failed;

    if(!do_lcmtest_node_t_test())
        goto failed;

    if(!do_lcmtest_multidim_array_t_test())
        goto failed;

    info("All tests passed.\n");
    lcm_destroy(g_lcm);
    return 0;
failed:
    info("failed\n");
    lcm_destroy(g_lcm);
    return 1;
}
