/* SPDX-License-Identifier: MIT-0 */

#include <lcm/lcm.h>
#include <stdio.h>

#include "exlcm_example_t.h"

int main()
{
    exlcm_example_t message = {};
    message.name = "";
    const int size = exlcm_example_t_encoded_size(&message);
    if (size != 82) {
        fprintf(stderr, "size = %d\n", size);
        fflush(stderr);
        return 1;
    }

    lcm_t *memq = lcm_create("memq://");
    int handled = -1;
    if (memq != NULL) {
        handled = lcm_handle_timeout(memq, 0);
        lcm_destroy(memq);
    }
    if (handled != 0) {
        fprintf(stderr, "memq failure\n");
        fflush(stderr);
        return 1;
    }

    return 0;
}
