#include <gtest/gtest.h>
#include <stdlib.h>
#include <string.h>

#include <lcm/lcm-cpp.hpp>

TEST(LCM_CPP, MemqConstructDestroy)
{
    lcm::LCM lcm("memq://");
    EXPECT_TRUE(lcm.good());
}

struct MemqSimpleState {
    std::vector<uint8_t> buf;
    bool handled;
};

void MemqSimpleHandler(const lcm::ReceiveBuffer *rbuf, const std::string &channel,
                       std::vector<uint8_t> *received_buf)
{
    received_buf->resize(rbuf->data_size);
    memcpy(&(*received_buf)[0], rbuf->data, rbuf->data_size);
}

TEST(LCM_CPP, MemqSimple)
{
    // Publish a single message, then subscribe to it and check it's read.
    lcm::LCM lcm("memq://");
    const int buf_size = 1024;
    std::vector<uint8_t> buf(buf_size);
    std::vector<uint8_t> received_buf;

    lcm.subscribeFunction("channel", MemqSimpleHandler, &received_buf);

    for (int iter = 0; iter < 10; ++iter) {
        for (int byte_index = 0; byte_index < buf_size; ++byte_index) {
            buf[byte_index] = rand() % 255;
        }

        lcm.publish("channel", &buf[0], buf_size);

        lcm.handle();

        EXPECT_EQ(buf, received_buf);
    }
}

void MemqBufferedHandler(const lcm::ReceiveBuffer *rbuf, const std::string &channel,
                         std::vector<std::vector<uint8_t> > *received_buffers)
{
    std::vector<uint8_t> buf(rbuf->data_size);
    memcpy(&buf[0], rbuf->data, rbuf->data_size);
    received_buffers->push_back(buf);
}

TEST(LCM_CPP, MemqBuffered)
{
    // Publish many messages so that they get buffered up, then read them all.
    lcm::LCM lcm("memq://");
    std::vector<std::vector<uint8_t> > received_buffers;

    lcm.subscribeFunction("channel", MemqBufferedHandler, &received_buffers);

    int num_bufs = 100;
    int buf_size = 100;
    std::vector<std::vector<uint8_t> > buffers(num_bufs);
    for (int buf_num = 0; buf_num < num_bufs; ++buf_num) {
        std::vector<uint8_t> &buf = buffers[buf_num];
        buf.resize(buf_size);
        for (int byte_index = 0; byte_index < buf_size; ++byte_index) {
            buf[byte_index] = rand() % 255;
        }
        lcm.publish("channel", &buf[0], buf.size());
    }

    for (int buf_num = 0; buf_num < num_bufs; ++buf_num) {
        lcm.handle();
    }

    EXPECT_EQ(buffers, received_buffers);
}

void MemqTimeoutHandler(const lcm::ReceiveBuffer *rbuf, const std::string &channel,
                        bool *msg_handled)
{
    *msg_handled = true;
}

TEST(LCM_CPP, MemqTimeout)
{
    // Test various usages of lcm_handle_timeout() using the memq provider
    lcm::LCM lcm("memq://");

    // No messages available.  Call should timeout immediately.
    EXPECT_EQ(0, lcm.handleTimeout(0));

    // No messages available.  Call should timeout in a few ms.
    EXPECT_EQ(0, lcm.handleTimeout(10));

    // Invalid timeout specification should result in an error.
    EXPECT_GT(0, lcm.handleTimeout(-1));

    // Subscribe to and publish on a channel.  Expect that the message gets
    // handled with an ample timeout.
    bool msg_handled = false;
    lcm.subscribeFunction("channel", MemqTimeoutHandler, &msg_handled);
    lcm.publish("channel", "", 0);
    EXPECT_LT(0, lcm.handleTimeout(10000));
    EXPECT_TRUE(msg_handled);
}
