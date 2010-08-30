#include "../lcm/lcm-c++.hpp"
#include "example_t.hpp"

int main(int argc, char **argv)
{
    lcm::LCM *lcm = new lcm::LCM("");

    example_t msg;

    msg.data = 12345;
    lcm->publish("channel1", &msg);

    msg.data = 54321;
    lcm->publish("channel2", &msg);

    delete lcm;

    return 0;
}
