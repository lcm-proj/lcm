#include <sys/time.h>
#include <time.h>
#include "example_t.hpp"

example_t::example_t()
{
}

unsigned int
example_t::getEncodedSize() const
{
    return 4;
}

void 
example_t::encode(void* buf, int offset, unsigned int maxlen) const
{
    *(int*)((uint8_t*)buf + offset) = this->data;
}

void
example_t::decode(const void *data, int offset, int max_data_len)
{
    // decode data buffer here
    //
    this->data = *((int*)((uint8_t*)data + offset));
}

void 
example_t::decodeCleanup()
{
    // cleanup resources allocated by decode
}
