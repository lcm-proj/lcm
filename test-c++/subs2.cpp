#include "example_t.hpp"
#include "../lcm/lcm-c++.hpp"

void
handler2(const lcm_recv_buf_t *rbuf, std::string channel, const example_t *msg, lcm::LCM *lcm)
{
    printf("handler 2 -- data = %d\n", msg->data);
}

void register2(lcm::LCM *lcm)
{
    lcm->subscribe<example_t, lcm::LCM *>("channel2", handler2, lcm);
}
