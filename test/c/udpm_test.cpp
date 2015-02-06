#include <gtest/gtest.h>

#include <lcm/lcm.h>

TEST(LCM_C, InvalidCreation) {
  lcm_t* lcm = lcm_create("udpm://asdf");
  EXPECT_EQ(NULL, lcm);

  lcm = lcm_create("udpm://0.0.0.0");
  EXPECT_EQ(NULL, lcm);

  lcm = lcm_create("udpm://239.255.1.1:65536");
  EXPECT_EQ(NULL, lcm);
}
