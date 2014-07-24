#!/usr/bin/python
import unittest

import lcmtest

class TestBools(unittest.TestCase):

    def test_bool(self):
        """Encode a bools_t message, then verify that it decodes correctly.
        Also check that the decoded fields are all of type bool.
        """
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

            msg.one_dim_array.append(bool((a_index + 1) % 2))
        data = msg.encode()

        decoded = lcmtest.bools_t.decode(data)

        self.assertEqual(msg.one_bool, decoded.one_bool)
        self.assertEqual(list(msg.fixed_array), list(decoded.fixed_array))
        self.assertEqual(msg.num_a, decoded.num_a)
        self.assertEqual(msg.num_b, decoded.num_b)

        self.assertEqual(bool, type(decoded.one_bool))
        self.assertTrue(all([type(elem) == bool
            for elem in decoded.fixed_array]))
        for sublist in decoded.two_dim_array:
            self.assertTrue(all([type(elem) == bool for elem in sublist]))
        self.assertTrue(all([type(elem) == bool
            for elem in decoded.one_dim_array]))

        for a_index in xrange(msg.num_a):
            for b_index in xrange(msg.num_b):
                self.assertEqual(msg.two_dim_array[a_index][b_index],
                                 decoded.two_dim_array[a_index][b_index])
            self.assertEqual(msg.one_dim_array[a_index],
                             decoded.one_dim_array[a_index])


if __name__ == '__main__':
    unittest.main()
