import hashlib
import os
import unittest
import time

from lcm import LCM


def lcm_handle_all(lcm, timeout=10):
    total_count = 0
    while True:
        count = lcm.handle_timeout(timeout)
        if count == 0:
            return total_count
        total_count += count


FAKE_CHANNEL = "FAKE_CHANNEL"


class TestLcmUtil(unittest.TestCase):
    def test_lcm_handle_all(self):
        lcm = LCM('udpm://239.107.71.73:20042?ttl=0')
        received = []

        def callback(channel, msg):
            self.assertEqual(channel, FAKE_CHANNEL)
            received.append(msg)

        sub = lcm.subscribe(FAKE_CHANNEL, callback)
        sub.set_queue_capacity(3)
        messages = [
            b"message1",
            b"message2",
            b"message3",
        ]

        # Case 1: lcm.handle() on its own will do a FIFO queue.
        for message in messages:
            lcm.publish(FAKE_CHANNEL, message)
        for i, message in enumerate(messages):
            lcm.handle()
            self.assertEqual(len(received), i + 1)
            self.assertEqual(received[-1], message)

        # Case 2: lcm.handle_all() will consume all.
        received = []
        for message in messages:
            lcm.publish(FAKE_CHANNEL, message)
        # Wait for LCM recv thread to handle incoming messages.
        time.sleep(0.001)
        count = lcm_handle_all(lcm)
        self.assertEqual(count, 3)
        self.assertEqual(received, messages)

        # Case 3: Show a failure case as mentioned in #345 where queue size
        # causes *newer* messages to be ignored in the LCM recv queue.
        received = []
        messages_extra = messages + [b"will be ignored"]
        for message in messages_extra:
            lcm.publish(FAKE_CHANNEL, message)
        # Wait for LCM recv thread to handle incoming messages.
        time.sleep(0.001)
        count = lcm_handle_all(lcm)
        self.assertEqual(count, 3)
        # WARNING: These are the OLD messages.
        self.assertEqual(received, messages)


if __name__ == '__main__':
    unittest.main()
