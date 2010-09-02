#include "../lcm/lcm-cpp.hpp"
#include "example_t.hpp"
#include "example2_t.hpp"

int main(int argc, char **argv)
{
    lcm::LCM *lcm = new lcm::LCM("");

    example_t msg;

    msg.data = 12345;
    lcm->publish("channel1", &msg);

    example2_t msg2;
    msg2.data = 54321;
    lcm->publish("channel2", &msg2);

    delete lcm;

    return 0;
}
