#include "../lcm/lcm-cpp.hpp"
#include "example_t.hpp"
#include "example2_t.hpp"

struct AppData
{
    int state_var;
};

class Handler : 
    public lcm::MessageHandler<example_t>
{
    public:
        ~Handler() {}

        void handleMessage(const lcm::ReceiveBuffer* rbuf,
                std::string chan, 
                const example_t* msg)
        {
            printf("class handler -- channel %s, data = %d\n", 
                    chan.c_str(), msg->data);
        }
};

void
handlerFunction(const lcm::ReceiveBuffer* rbuf, 
		 std::string channel, 
		 const example2_t* msg, AppData* appData)
{
    printf("function handler -- channel %s, data = %d\n", 
            channel.c_str(), msg->data);
}


int main(int argc, char** argv)
{
    lcm::LCM* lcm = new lcm::LCM("");


    Handler handlerObject;
    lcm->subscribe("channel1", &handlerObject);


    AppData appData;
    lcm->subscribeFunction("channel2", handlerFunction, &appData);


    while(true) 
        lcm->handle();

    delete lcm;

    return 0;
}
