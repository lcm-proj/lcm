#include "../lcm/lcm-c++.hpp"
#include "example_t.hpp"

void register1(lcm::LCM *lcm);
void register2(lcm::LCM *lcm);

int main(int argc, char **argv)
{
    lcm::LCM *lcm = new lcm::LCM("");

    register1(lcm);
    register2(lcm);

    while(true) 
        lcm->handle();

    return 0;
}
