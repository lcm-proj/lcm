#ifndef __example_t_hpp__
#define __example_t_hpp__

#include <inttypes.h>

class example_t {
    public:
        int data;


        example_t();

        unsigned int getEncodedSize() const;
        void encode(void* buf, int offset, unsigned int maxlen) const;

        void decode(const void* data, int offset, int max_data_len);
        void decodeCleanup();
};

#endif
