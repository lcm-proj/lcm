#!/usr/bin/python
import os
import random
import struct
import time
import unittest

import lcm
import lcmtest

class TestByteArray(unittest.TestCase):

    def test_construct(self):
        msg = lcmtest.byte_array_t()
        msg.encode()

    def test_fill(self):
        msg = lcmtest.byte_array_t()
        msg.num_bytes = 5
        msg.data = struct.pack("bbbbb", 1, 2, 3, 4, 5)

        data = msg.encode()

        decoded = lcmtest.byte_array_t.decode(data)
        self.assertEqual(5, decoded.num_bytes)
        self.assertEqual(msg.data, decoded.data)

if __name__ == '__main__':
    unittest.main()
