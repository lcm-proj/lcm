#include <sys/time.h>
#include <time.h>
#include "example_t.hpp"

example_t *
example_t::decode(const void *data, int offset, int max_data_len)
{
    example_t *result = new example_t();

    result->data = time(NULL);

    return result;
}
