#include <gtest/gtest.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#include <lcm/lcm.h>

#include "common.hpp"

#define info(...)               \
    do {                        \
        printf("cpp_client: "); \
        printf(__VA_ARGS__);    \
        printf("\n");           \
    } while (0)

class EchoTest {
  public:
    EchoTest()
        : lcm_(),
          num_trials_(100),
          echo_msg_len_(0),
          echo_data_(NULL),
          response_count_(0),
          subscription_(),
          test_channel_("TEST_ECHO")
    {
    }

    bool Run(void)
    {
        int maxlen = 10000;
        int minlen = 10;
        echo_data_ = (uint8_t *) malloc(maxlen);
        subscription_ = lcm_.subscribe(test_channel_ + "_REPLY", &EchoTest::Handler, this);
        response_count_ = 0;

        for (int iter = 0; iter < num_trials_; iter++) {
            echo_msg_len_ = rand() % (maxlen - minlen) + minlen;
            for (int i = 0; i < echo_msg_len_; i++)
                echo_data_[i] = rand() % 256;

            lcm_.publish(test_channel_, echo_data_, echo_msg_len_);

            if (lcm_.handleTimeout(500) <= 0 || (response_count_ != iter + 1)) {
                info("echo test failed to receive response on iteration %d", iter);
                free(echo_data_);
                return false;
            }
        }

        lcm_.unsubscribe(subscription_);
        free(echo_data_);
        return true;
    }

  private:
    void Handler(const lcm::ReceiveBuffer *rbuf, const std::string &channel)
    {
        if (rbuf->data_size != echo_msg_len_)
            return;
        if (memcmp(rbuf->data, echo_data_, rbuf->data_size))
            return;
        response_count_++;
    }

    lcm::LCM lcm_;
    int num_trials_;
    int echo_msg_len_;
    uint8_t *echo_data_;
    int response_count_;
    lcm::Subscription *subscription_;
    std::string test_channel_;
};

TEST(LCM_CPP, Echo)
{
    srand(time(NULL));

    EXPECT_TRUE(EchoTest().Run());
}

template <class LcmType>
class TypedTest {
  public:
    TypedTest(const std::string test_name, int num_trials)
        : lcm_(),
          response_count_(0),
          num_trials_(num_trials),
          subscription_(),
          test_channel_(test_name)
    {
    }

    bool Run(void)
    {
        LcmType msg;
        lcm::Subscription *subscription =
            lcm_.subscribe(test_channel_ + "_reply", &TypedTest<LcmType>::Handler, this);
        bool result = true;
        for (int trial = 0; trial < num_trials_ && result; trial++) {
            FillLcmType(trial, &msg);
            lcm_.publish(test_channel_, &msg);
            if (lcm_.handleTimeout(500) <= 0) {
                info("%s test: Timeout waiting for reply", test_channel_.c_str());
                result = false;
                break;
            } else if (response_count_ != trial + 1) {
                info("%s test: failed on iteration %d", test_channel_.c_str(), trial);
                result = false;
                break;
            }
            ClearLcmType(&msg);
        }
        lcm_.unsubscribe(subscription);
        return result;
    }

  private:
    void Handler(const lcm::ReceiveBuffer *rbuf, const std::string &channel, const LcmType *msg)
    {
        if (CheckLcmType(msg, response_count_ + 1)) {
            response_count_++;
            return;
        }
    }

    lcm::LCM lcm_;
    int response_count_;
    int num_trials_;
    lcm::Subscription *subscription_;
    std::string test_channel_;
};

TEST(LCM_CPP, primitives_t)
{
    EXPECT_TRUE(TypedTest<lcmtest::primitives_t>("test_lcmtest_primitives_t", 1000).Run());
}

TEST(LCM_CPP, primitives_list_t)
{
    EXPECT_TRUE(TypedTest<lcmtest::primitives_list_t>("test_lcmtest_primitives_list_t", 100).Run());
}

TEST(LCM_CPP, node_t)
{
    EXPECT_TRUE(TypedTest<lcmtest::node_t>("test_lcmtest_node_t", 7).Run());
}

TEST(LCM_CPP, multidim_array_t)
{
    EXPECT_TRUE(TypedTest<lcmtest::multidim_array_t>("test_lcmtest_multidim_array_t", 5).Run());
}

TEST(LCM_CPP, cross_package_t)
{
    EXPECT_TRUE(TypedTest<lcmtest2::cross_package_t>("test_lcmtest2_cross_package_t", 100).Run());
}

#if LCM_CXX_11_ENABLED
template <class LcmType>
class LambdaTest {
  public:
    LambdaTest(const std::string test_name, int num_trials)
        : lcm_(), num_trials_(num_trials), test_channel_(test_name)
    {
    }

    bool Run(void)
    {
        LcmType msg;
        int response_count = 0;
        lcm::LCM::HandlerFunction<LcmType> handler = [&response_count](
            const lcm::ReceiveBuffer *rbuf, const std::string &channel, const LcmType *msg) {
            if (CheckLcmType(msg, response_count + 1)) {
                response_count++;
            }
        };
        lcm::Subscription *subscription = lcm_.subscribe(test_channel_ + "_reply", handler);
        bool result = true;
        for (int trial = 0; trial < num_trials_ && result; trial++) {
            FillLcmType(trial, &msg);
            lcm_.publish(test_channel_, &msg);
            if (lcm_.handleTimeout(500) <= 0) {
                info("%s test: Timeout waiting for reply", test_channel_.c_str());
                result = false;
                break;
            } else if (response_count != trial + 1) {
                info("%s test: failed on iteration %d", test_channel_.c_str(), trial);
                result = false;
                break;
            }
            ClearLcmType(&msg);
        }
        lcm_.unsubscribe(subscription);
        return result;
    }

  private:
    lcm::LCM lcm_;
    int num_trials_;
    std::string test_channel_;
};

TEST(LCM_CPP, Lambda_A)
{
    EXPECT_TRUE(LambdaTest<lcmtest::primitives_t>("test_lcmtest_primitives_t", 1000).Run());
}

TEST(LCM_CPP, Lambda_B)
{
    EXPECT_TRUE(
        LambdaTest<lcmtest::primitives_list_t>("test_lcmtest_primitives_list_t", 100).Run());
}

TEST(LCM_CPP, Lambda_C)
{
    EXPECT_TRUE(LambdaTest<lcmtest::node_t>("test_lcmtest_node_t", 7).Run());
}
#endif
