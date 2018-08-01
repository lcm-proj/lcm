#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <glib.h>
#include <lcm/lcm.h>

int main(int argc, char **argv)
{
    lcm_t *lcm = lcm_create(NULL);

    srand(0);

    int num_messages = 10000;
    for (int i = 0; i < num_messages; i++) {
        int data_sz;
        // if(i < num_messages / 2) {
        //     data_sz = rand() % 1000000;
        // } else {
        data_sz = 1200;
        // }
        char *data = (char *) calloc(1, data_sz);
        snprintf(data, data_sz, "%d", i);

        lcm_publish(lcm, "BUFTEST", data, 80);
        printf("transmitted msg # %5d size %d\n", i, data_sz);
        g_usleep(1000);
        free(data);
    }

    return 0;
}
