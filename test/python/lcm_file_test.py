#!/usr/bin/python
import os
import random
import tempfile
import unittest

import lcm
import client

class TestLcmFile(unittest.TestCase):
    def setUp(self):
        # tempfile.mktemp() is deprecated, so make our own random filename :-/
        self.log_filename = os.path.join(tempfile.gettempdir(),
            "lcm_unit_test_log_{}.lcmlog".format(random.random()))
        self.tester = client.PrimitivesTest()
        self.msg_type = self.tester.get_message_type()
        self.num_iterations = 100
        self.test_channel = "CHAN"

    def tearDown(self):
        os.remove(self.log_filename)

    def test_read_or_write_only(self):
        lcm_obj = lcm.LCM("file://%s?mode=w" % self.log_filename)

        def handler(channel, data):
            msg = self.msg_type.decode(data)
            try:
                self.tester.check_reply(msg, handler.iteration)
            except ValueError as xcp:
                self.fail(str(xcp))

        for iteration in range(self.num_iterations):
            msg = self.tester.make_message(iteration)
            lcm_obj.publish(self.test_channel, msg.encode())

#        print("opening read mode")
        lcm_obj = lcm.LCM("file://%s?mode=r" % self.log_filename)
        subs = lcm_obj.subscribe(self.test_channel, handler)
        for iteration in range(self.num_iterations):
            handler.iteration = iteration
            msg = self.tester.make_message(iteration)
            lcm_obj.handle()
        with self.assertRaises(IOError):
            lcm_obj.handle()
        with self.assertRaises(IOError):
            lcm_obj.publish(self.test_channel, msg.encode())
        lcm_obj.unsubscribe(subs)


def main():
    unittest.main()

if __name__ == '__main__':
    main()
