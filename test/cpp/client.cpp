#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <time.h>

#include <lcm/lcm.h>

#include "common.hpp"

#define info(...) do { printf("cpp_client: "); printf(__VA_ARGS__); } while(0)

class LcmTest {
 public:
  LcmTest(lcm::LCM* lcm, const std::string test_name, int num_trails)
      : lcm_(lcm),
        test_channel_(test_name),
        response_count_(0),
        num_trials_(num_trails),
        subscription_() {
  }
  virtual ~LcmTest() {
  }
  virtual bool Run()=0;

 protected:
  lcm::LCM* lcm_;
  int response_count_;
  int num_trials_;
  lcm::Subscription* subscription_;
  std::string test_channel_;
};

template<class LcmType>
class LcmTypeTest : public LcmTest {
 public:
  LcmTypeTest(lcm::LCM* lcm, const std::string test_name, int num_trails)
      : LcmTest(lcm, test_name, num_trails) {
  }

  bool Run(void) {
    LcmType msg;
    lcm::Subscription* subscription = lcm_->subscribe(
        test_channel_ + "_reply", &LcmTypeTest<LcmType>::Handler, this);
    bool result = true;
    for (int trial = 0; trial < num_trials_ && result; trial++) {
      FillLcmType(trial, &msg);
      lcm_->publish(test_channel_, &msg);
      if (!LcmHandleTimeout(lcm_, 500)) {
        info("%s test: Timeout waiting for reply\n", test_channel_.c_str());
        result = false;
        break;
      } else if (response_count_ != trial + 1) {
        info("%s test: failed on iteration %d\n", test_channel_.c_str(), trial);
        result = false;
        break;
      }
      ClearLcmType(&msg);
    }
    lcm_->unsubscribe(subscription);
    if (result) {
      info("%-30s : PASSED\n", test_channel_.c_str());
    } else {
      info("%-30s : FAILED\n", test_channel_.c_str());
    }
    return result;
  }

 private:
  void Handler(const lcm::ReceiveBuffer* rbuf, const std::string& channel,
               const LcmType* msg) {
    if (CheckLcmType(msg, response_count_ + 1)) {
      response_count_++;
      return;
    }
  }
};

class EchoTest : public LcmTest {
 public:
  EchoTest(lcm::LCM* lcm, int num_trails)
      : LcmTest(lcm, "TEST_ECHO", num_trails),
        echo_data_(NULL),
        echo_msg_len_(0) {
  }
  bool Run(void) {
    int maxlen = 10000;
    int minlen = 10;
    echo_data_ = (uint8_t*) malloc(maxlen);
    subscription_ = lcm_->subscribe(test_channel_ + "_REPLY",
                                    &EchoTest::Handler, this);
    response_count_ = 0;

    for (int iter = 0; iter < num_trials_; iter++) {
      echo_msg_len_ = rand() % (maxlen - minlen) + minlen;
      for (int i = 0; i < echo_msg_len_; i++)
        echo_data_[i] = rand() % 256;

      lcm_->publish(test_channel_, echo_data_, echo_msg_len_);

      if (!LcmHandleTimeout(lcm_, 500) || (response_count_ != iter + 1)) {
        info("echo test failed to receive response on iteration %d\n", iter);
        free(echo_data_);
        return 0;
      }
    }

    info("%-30s : PASSED\n", "echo test");
    lcm_->unsubscribe(subscription_);
    free(echo_data_);
    return 1;
  }

 private:
  void Handler(const lcm::ReceiveBuffer *rbuf, const std::string& channel) {
    if (rbuf->data_size != echo_msg_len_)
      return;
    if (memcmp(rbuf->data, echo_data_, rbuf->data_size))
      return;
    response_count_++;
  }

  int echo_msg_len_;
  uint8_t* echo_data_;
};

// ========================== main
int main(int argc, char ** argv) {
  srand(time(NULL));

  lcm::LCM lcm;
  if (!lcm.good()) {
    info("Unable to initialize LCM\n");
    return 1;
  }

  if (!EchoTest(&lcm, 100).Run())
    goto failed;

  if (!LcmTypeTest<lcmtest::primitives_t>(&lcm, "test_lcmtest_primitives_t",
                                          1000).Run())
    goto failed;
  if (!LcmTypeTest<lcmtest::primitives_list_t>(&lcm,
                                               "test_lcmtest_primitives_list_t",
                                               100).Run())
    goto failed;
  if (!LcmTypeTest<lcmtest::node_t>(&lcm, "test_lcmtest_node_t", 7).Run())
    goto failed;
  if (!LcmTypeTest<lcmtest::multidim_array_t>(&lcm,
                                              "test_lcmtest_multidim_array_t",
                                              5).Run())
    goto failed;
  if (!LcmTypeTest<lcmtest2::cross_package_t>(&lcm,
                                              "test_lcmtest2_cross_package_t",
                                              100).Run())
    goto failed;

  info("All tests passed.\n");
  return 0;

  failed:
  info("failed\n");
  return 1;
}
