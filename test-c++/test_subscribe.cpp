#include "../lcm/lcm-c++.hpp"
#include "example_t.hpp"

struct AppData
{
    lcm::LCM* lcm;
};

void
handler1(const lcm_recv_buf_t* rbuf, 
		std::string channel, 
		const example_t* msg, 
		lcm::LCM* lcm)
{
    printf("handler 1 -- data = %d\n", msg->data);
}

void
handler2(const lcm_recv_buf_t* rbuf, 
		std::string channel, 
		const example_t* msg, AppData* appData)
{
    printf("handler 2 -- data = %d\n", msg->data);
}

int main(int argc, char** argv)
{
    AppData appData;
    appData.lcm = new lcm::LCM("");

    appData.lcm->subscribe("channel1", handler1, appData.lcm);
    appData.lcm->subscribe("channel2", handler2, &appData);

    while(true) 
        appData.lcm->handle();

    delete appData.lcm;

    return 0;
}
