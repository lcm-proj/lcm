#include <gtest/gtest.h>
#include <stdlib.h>
#include <string.h>

#include <lcm/lcm.h>

TEST(LCM_C, MemqConstructDestroy)
{
    lcm_t *lcm = lcm_create("memq://");
    EXPECT_TRUE(lcm != NULL);
    lcm_destroy(lcm);
}

struct MemqSimpleState {
    std::vector<uint8_t> buf;
    bool handled;
};

void MemqSimpleHandler(const lcm_recv_buf_t *rbuf, const char *channel, void *user_data)
{
    std::vector<uint8_t> *received_buf = (std::vector<uint8_t> *) user_data;
    received_buf->resize(rbuf->data_size);
    memcpy(&(*received_buf)[0], rbuf->data, rbuf->data_size);
}

TEST(LCM_C, MemqSimple)
{
    // Publish a single message, then subscribe to it and check it's read.
    lcm_t *lcm = lcm_create("memq://");
    const int buf_size = 1024;
    std::vector<uint8_t> buf(buf_size);
    std::vector<uint8_t> received_buf;

    lcm_subscribe(lcm, "channel", MemqSimpleHandler, &received_buf);

    for (int iter = 0; iter < 10; ++iter) {
        for (int byte_index = 0; byte_index < buf_size; ++byte_index) {
            buf[byte_index] = rand() % 255;
        }

        lcm_publish(lcm, "channel", &buf[0], buf_size);

        lcm_handle(lcm);

        EXPECT_EQ(buf, received_buf);
    }

    lcm_destroy(lcm);
}

void MemqBufferedHandler(const lcm_recv_buf_t *rbuf, const char *channel, void *user_data)
{
    std::vector<std::vector<uint8_t> > *received_buffers =
        (std::vector<std::vector<uint8_t> > *) user_data;
    std::vector<uint8_t> buf(rbuf->data_size);
    memcpy(&buf[0], rbuf->data, rbuf->data_size);
    received_buffers->push_back(buf);
}

TEST(LCM_C, MemqBuffered)
{
    // Publish many messages so that they get buffered up, then read them all.
    lcm_t *lcm = lcm_create("memq://");
    std::vector<std::vector<uint8_t> > received_buffers;

    lcm_subscribe(lcm, "channel", MemqBufferedHandler, &received_buffers);

    int num_bufs = 100;
    int buf_size = 100;
    std::vector<std::vector<uint8_t> > buffers(num_bufs);
    for (int buf_num = 0; buf_num < num_bufs; ++buf_num) {
        std::vector<uint8_t> &buf = buffers[buf_num];
        buf.resize(buf_size);
        for (int byte_index = 0; byte_index < buf_size; ++byte_index) {
            buf[byte_index] = rand() % 255;
        }
        lcm_publish(lcm, "channel", &buf[0], buf.size());
    }

    for (int buf_num = 0; buf_num < num_bufs; ++buf_num) {
        lcm_handle(lcm);
    }

    EXPECT_EQ(buffers, received_buffers);

    lcm_destroy(lcm);
}

void MemqTimeoutHandler(const lcm_recv_buf_t *rbuf, const char *channel, void *user_data)
{
    int *msg_handled = (int *) user_data;
    *msg_handled = 1;
}

TEST(LCM_C, MemqTimeout)
{
    // Test various usages of lcm_handle_timeout() using the memq provider
    lcm_t *lcm = lcm_create("memq://");

    // No messages available.  Call should timeout immediately.
    EXPECT_EQ(0, lcm_handle_timeout(lcm, 0));

    // No messages available.  Call should timeout in a few ms.
    EXPECT_EQ(0, lcm_handle_timeout(lcm, 10));

    // Invalid timeout specification should result in an error.
    EXPECT_GT(0, lcm_handle_timeout(lcm, -1));

    // Subscribe to and publish on a channel.  Expect that the message gets
    // handled with an ample timeout.
    int msg_handled = 0;
    lcm_subscribe(lcm, "channel", MemqTimeoutHandler, &msg_handled);
    lcm_publish(lcm, "channel", "", 0);
    EXPECT_LT(0, lcm_handle_timeout(lcm, 10000));

    EXPECT_EQ(1, msg_handled);

    lcm_destroy(lcm);
}
