#!/usr/bin/python
import os
import random
import unittest

import lcm

class TestLcmEventLog(unittest.TestCase):
    def test_read_eventlog(self):
        mydir = os.path.dirname(__file__)
        log = lcm.EventLog(os.path.join(mydir, 'example.lcmlog'))
        event = log.read_next_event()

def main():
    unittest.main()

if __name__ == '__main__':
    main()
