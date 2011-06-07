// file: listener.c
//
// LCM example program.
//
// compile with:
//  $ gcc -o listener listener.c -llcm
//
// If using GNU/Linux, you can also use pkg-config:
//  $ gcc -o listener listener.c `pkg-config --cflags --libs lcm`

#include <stdio.h>

#include <lcm/lcm-cpp.hpp>
#include "exlcm/example_t.hpp"

class Handler : 
    public lcm::MessageHandler<exlcm::example_t>
{
    public:
        ~Handler() {}

        void handleMessage(const lcm::ReceiveBuffer* rbuf,
                std::string chan, 
                const exlcm::example_t* msg)
        {
            int i;
            printf("Received message on channel \"%s\":\n", chan.c_str());
            printf("  timestamp   = %lld\n", (long long)msg->timestamp);
            printf("  position    = (%f, %f, %f)\n",
                    msg->position[0], msg->position[1], msg->position[2]);
            printf("  orientation = (%f, %f, %f, %f)\n",
                    msg->orientation[0], msg->orientation[1], msg->orientation[2],
                    msg->orientation[3]);
            printf("  ranges:");
            for(i = 0; i < msg->num_ranges; i++)
                printf(" %d", msg->ranges[i]);
            printf("\n");
        }
};

int main(int argc, char** argv)
{
    lcm::LCM lcm;

    if(0 != lcm.init())
        return 1;

    Handler handlerObject;
    lcm::Subscription* subscription;
    subscription = lcm.subscribe<exlcm::example_t>("EXAMPLE", &handlerObject);

    while(0 == lcm.handle());

    lcm.unsubscribe(subscription);

    return 0;
}
