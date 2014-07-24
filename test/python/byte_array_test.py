#!/usr/bin/python
import struct
import unittest

import lcmtest

class TestByteArray(unittest.TestCase):

    def test_construct(self):
        """Verify that a message constructed without explicitly setting any
        fields can be encoded."""
        msg = lcmtest.byte_array_t()
        msg.encode()

    def test_fill(self):
        """Create a message with a small byte array and encode it.  Decode it
        and verify that the decoded message matches the original."""
        msg = lcmtest.byte_array_t()
        msg.num_bytes = 5
        msg.data = struct.pack("bbbbb", 1, 2, 3, 4, 5)

        data = msg.encode()

        decoded = lcmtest.byte_array_t.decode(data)
        self.assertEqual(5, decoded.num_bytes)
        self.assertEqual(msg.data, decoded.data)

if __name__ == '__main__':
    unittest.main()
