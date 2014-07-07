#!/usr/bin/python
import os
import random
import tempfile
import time
import unittest

import lcm
import lcmtest
import client

class TestLcmMemq(unittest.TestCase):
    def test_timeout(self):
        lc = lcm.LCM("memq://")

        # No messages available, timeout 0.
        self.assertEqual(0, lc.handle_timeout(0))

        # No messages available, short timeout.
        self.assertEqual(0, lc.handle_timeout(10))

        # Passing an invalid timeout should raise an exception.
        with self.assertRaises(ValueError):
            lc.handle_timeout(-1)

        # Publish a single message without a subscriber.  Since there's no
        # subscriber, handling again should timeout.
        lc.publish("channel", "")
        self.assertEqual(0, lc.handle_timeout(0))
        self.assertEqual(0, lc.handle_timeout(10))

        # Subscribe and publish to a channel.
        def on_msg(channel, data):
            on_msg.msg_handled = True
        on_msg.msg_handled = False
        lc.subscribe("channel", on_msg)
        lc.publish("channel", "")

        # Now handle the message
        self.assertLess(0, lc.handle_timeout(10000))
        self.assertTrue(on_msg.msg_handled)

def main():
    unittest.main()

if __name__ == '__main__':
    main()
