/* SPDX-License-Identifier: MIT-0 */

#include <iostream>

#include <lcm/lcm-cpp.hpp>

#include "exlcm/example_t.hpp"

int main() {
  exlcm::example_t message = {};
  const auto size = message.getEncodedSize();
  if (size != 82) {
    std::cerr << "size = " << size << "\n";
    return 1;
  }

  lcm::LCM memq("memq://");
  if (memq.handleTimeout(0) != 0) {
    std::cerr << "memq failure\n";
    return 1;
  }

  return 0;
}
