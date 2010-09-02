#include <sys/time.h>
#include <time.h>
#include "example2_t.hpp"

example2_t::example2_t()
{
}

unsigned int
example2_t::getEncodedSize() const
{
    return 4;
}

void 
example2_t::encode(void* buf, int offset, unsigned int maxlen) const
{
    *(int*)((uint8_t*)buf + offset) = this->data;
}

int
example2_t::decode(const void *data, int offset, int max_data_len)
{
    // decode data buffer here
    //
    this->data = *((int*)((uint8_t*)data + offset));
    return 4;
}

void 
example2_t::decodeCleanup()
{
    // cleanup resources allocated by decode
}

const char* 
example2_t::getTypeName()
{
    return "example2_t";
}
