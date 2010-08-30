#include "../lcm/lcm-c++.hpp"
#include "example_t.hpp"

void
handler1(const lcm_recv_buf_t *rbuf, 
		std::string channel, 
		const example_t *msg, 
		lcm::LCM *lcm)
{
    printf("handler 1 -- data = %d\n", msg->data);
}

void
handler2(const lcm_recv_buf_t *rbuf, 
		std::string channel, 
		const example_t *msg, lcm::LCM *lcm)
{
    printf("handler 2 -- data = %d\n", msg->data);
}

int main(int argc, char **argv)
{
    lcm::LCM *lcm = new lcm::LCM("");

    lcm->subscribe("channel1", handler1, lcm);
    lcm->subscribe("channel2", handler2, lcm);

    while(true) 
        lcm->handle();

    delete lcm;

    return 0;
}
