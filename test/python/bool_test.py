#!/usr/bin/python
import os
import random
import time
import unittest

import lcm
import lcmtest

class TestBools(unittest.TestCase):

    def test_construct(self):
        msg = lcmtest.bools_t()
        msg.encode()

    def test_decode(self):
        msg = lcmtest.bools_t()
        msg.one_bool = True
        msg.fixed_array = [False, True, False]
        msg.num_a = 3
        msg.num_b = 2
        for a_index in xrange(msg.num_a):
            inner_list = []
            for b_index in xrange(msg.num_b):
                inner_list.append(bool(b_index % 2))
            msg.two_dim_array.append(inner_list)

            msg.one_dim_array.append(bool((b_index + 1) % 2))
        data = msg.encode()

        decoded = lcmtest.bools_t.decode(data)

        self.assertEqual(msg.one_bool, decoded.one_bool)
        self.assertEqual(list(msg.fixed_array), list(decoded.fixed_array))
        self.assertEqual(msg.num_a, decoded.num_a)
        self.assertEqual(msg.num_b, decoded.num_b)

        for a_index in xrange(msg.num_a):
            for b_index in xrange(msg.num_b):
                self.assertEqual(msg.two_dim_array[a_index][b_index],
                                 decoded.two_dim_array[a_index][b_index])
            self.assertEqual(msg.one_dim_array[a_index],
                             decoded.one_dim_array[a_index])


if __name__ == '__main__':
    unittest.main()
