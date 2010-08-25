#include "example_t.hpp"
#include "../lcm/lcm-c++.hpp"

void
handler1(const lcm_recv_buf_t *rbuf, std::string channel, const example_t *msg, lcm::LCM *lcm)
{
    printf("handler 1 -- data = %d\n", msg->data);
}

void register1(lcm::LCM *lcm)
{
    lcm->subscribe<example_t, lcm::LCM *>("channel1", handler1, lcm);
}
