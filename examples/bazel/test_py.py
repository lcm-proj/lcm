# SPDX-License-Identifier: MIT-0

import unittest

import lcm

from exlcm import example_t


class TestPy(unittest.TestCase):
    def test_size(self):
        message = example_t()
        size = len(message.encode())
        self.assertEqual(size, 82)

    def test_memq(self):
        memq = lcm.LCM("memq://")
        self.assertEqual(memq.handle_timeout(0), 0)


def main():
    unittest.main()


assert __name__ == '__main__'
main()
