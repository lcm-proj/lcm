// file: send_message.c
//
// LCM example program.
//
// compile with:
//  $ gcc -o send_message send_message.c -llcm
//
// If using GNU/Linux, you can also use pkg-config:
//  $ gcc -o send_message send_message.c `pkg-config --cflags --libs lcm`

#include <stdio.h>
#include <lcm/lcm.h>

#include "exlcm_example_t.h"

static void
send_message(lcm_t * lcm)
{
    exlcm_example_t my_data = {
        .timestamp = 0,
        .position = { 1, 2, 3 },
        .orientation = { 1, 0, 0, 0 },
    };
    int16_t ranges[15];
    int i;
    for(i = 0; i < 15; i++)
        ranges[i] = i;

    my_data.num_ranges = 15;
    my_data.ranges = ranges;

    exlcm_example_t_publish(lcm, "EXAMPLE", &my_data);
}

int
main(int argc, char ** argv)
{
    lcm_t * lcm;

    lcm = lcm_create(NULL);
    if(!lcm)
        return 1;

    send_message(lcm);

    lcm_destroy(lcm);
    return 0;
}
