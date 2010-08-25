#ifndef __example_t_hpp__
#define __example_t_hpp__

class example_t {
    public:
        static example_t *decode(const void *data, int offset, int max_data_len);

        int data;
};

#endif
