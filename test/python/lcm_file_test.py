#!/usr/bin/python
import os
import random
import tempfile
import time
import unittest

import lcm
import lcmtest
import client

class TestLcmFile(unittest.TestCase):
    def setUp(self):
        # tempfile.mktemp() is deprecated, so make our own random filename :-/
        self.log_filename = os.path.join(tempfile.gettempdir(), "lcm_unit_test_log_{}.lcmlog".format(random.random()))
        self.tester = client.PrimitivesTest();
        self.msg_type = self.tester.get_message_type()
        self.num_iterations = 100
        self.test_channel = "CHAN"

    def tearDown(self):
        os.remove(self.log_filename)

    def handler(self, channel, data):
        msg = self.msg_type.decode(data)
        try:
            self.tester.check_reply(msg, self.iteration)
        except ValueError as xcp:
            self.fail(str(xcp))


    def test_read_or_write_only(self):
        lc = lcm.LCM("file://%s?mode=w" % self.log_filename)
        for iteration in range(self.num_iterations):
            self.iteration = iteration
            msg = self.tester.make_message(iteration)
            lc.publish(self.test_channel, msg.encode())

        print("opening read mode")
        lc = lcm.LCM("file://%s?mode=r" % self.log_filename)
        subs = lc.subscribe(self.test_channel, self.handler)
        for iteration in range(self.num_iterations):
            self.iteration = iteration
            msg = self.tester.make_message(iteration)
            lc.handle()
        with self.assertRaises(IOError):
            lc.handle()
        with self.assertRaises(IOError):
            lc.publish(self.test_channel, msg.encode())
        lc.unsubscribe(subs)


def main():
    unittest.main()

if __name__ == '__main__':
    main()
