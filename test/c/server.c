#include <stdio.h>
#include <stdlib.h>
#include <lcm/lcm.h>

#ifndef WIN32
#include <sys/select.h>
typedef int SOCKET;
#else
#include <winsock2.h>
#endif

#include "common.h"

static lcm_t* g_lcm = NULL;

// overall test status
static int g_test_complete = 0;
static int g_test_passed = 0;

#define info(...) do { fprintf(stderr, "server: "); fprintf(stderr, __VA_ARGS__); } while(0)

static void
all_tests_passed(void)
{
    g_test_passed = 1;
    g_test_complete = 1;
}

// ========================== Generic macro for declaring a test
#define MAKE_TEST(type, num_iters, success_func) \
\
static int g_##type##_count = 0; \
static type##_subscription_t* g_##type##_subscription = NULL; \
\
static void \
type##_handler(const lcm_recv_buf_t* rbuf, const char* channel, \
    const type* msg, void* user) \
{ \
    if(!check_##type(msg, g_##type##_count)) { \
        g_test_passed = 0; \
        g_test_complete = 1; \
        return; \
    } \
    type reply; \
    fill_##type(g_##type##_count + 1, &reply); \
    type##_publish(g_lcm, "test_" #type "_reply", &reply); \
    clear_##type(&reply); \
    g_##type##_count++; \
    if(g_##type##_count == num_iters) { \
        type##_unsubscribe(g_lcm, g_##type##_subscription); \
        info("%-25s : PASSED\n", #type); \
        success_func(); \
    } \
} \
\
static void \
begin_##type##_test() \
{ \
    g_##type##_subscription = type##_subscribe(g_lcm, "test_" #type, & type##_handler, NULL); \
}

MAKE_TEST(lcmtest_multidim_array_t, 5, all_tests_passed);
MAKE_TEST(lcmtest_node_t, 7, begin_lcmtest_multidim_array_t_test);
MAKE_TEST(lcmtest_primitives_list_t, 100, begin_lcmtest_node_t_test);
MAKE_TEST(lcmtest_primitives_t, 1000, begin_lcmtest_primitives_list_t_test);

// ========================== echo test
static int g_echo_count = 0;
static lcm_subscription_t* g_echo_subscription = NULL;


static void
end_echo_test()
{
    info("%-25s : PASSED\n", "echo test");
    lcm_unsubscribe(g_lcm, g_echo_subscription);
    begin_lcmtest_primitives_t_test();
}

static void
echo_handler(const lcm_recv_buf_t *rbuf, const char* channel, void* user)
{
    lcm_publish(g_lcm, "TEST_ECHO_REPLY", rbuf->data, rbuf->data_size);
    g_echo_count++;

    if(g_echo_count >= 100)
    {
        end_echo_test();
    }
}

static void
begin_echo_test()
{
    g_echo_subscription = lcm_subscribe(g_lcm, "TEST_ECHO", &echo_handler, NULL);
}

// ============================

int main(int argc, char** argv)
{
    g_lcm = lcm_create(NULL);
    if(!g_lcm)
        return 1;

    begin_echo_test();

    if(_lcm_handle_timeout(g_lcm, 10000)) {
        while(!g_test_complete)
        {
            if(!_lcm_handle_timeout(g_lcm, 500)) {
                info("Timed out waiting for client message\n");
                g_test_complete = 1;
                break;
            }
        }
    } else {
        info("Timed out waiting for first client message\n");
    }

    lcm_destroy(g_lcm);
    if(g_test_passed)
        info("All tests passed.\n");
    else
        info("Test failed.\n");
    return !g_test_passed;
}
