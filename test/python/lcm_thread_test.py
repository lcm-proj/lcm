#!/usr/bin/python
import threading
import time
import unittest

import lcm

class TestLcmThreads(unittest.TestCase):
    def test_handle_and_publish(self):
        """Worker thread calls LCM.handle(), while main thread calls
        LCM.publish()
        """
        lcm_obj = lcm.LCM("memq://")

        def on_msg(channel, data):
            on_msg.msg_handled = True
        on_msg.msg_handled = False

        lcm_obj.subscribe("channel", on_msg)

        def thread_func():
            lcm_obj.handle()

        worker = threading.Thread(target=thread_func)
        worker.start()
        time.sleep(0.1)
        lcm_obj.publish("channel", "")

        worker.join()

        self.assertTrue(on_msg.msg_handled)

    def test_two_handle_timeout(self):
        """Check that calling LCM.handle_timeout() from two threads at once is
        not allowed.
        """
        lcm_obj = lcm.LCM("memq://")

        def thread_func():
            lcm_obj.handle_timeout(5000)

        lcm_obj.subscribe("channel", lambda *x: None)

        worker = threading.Thread(target=thread_func)
        worker.start()
        time.sleep(0.1)

        with self.assertRaises(RuntimeError):
            lcm_obj.handle_timeout(1000)

        lcm_obj.publish("channel", "")

        worker.join()

    def test_two_handle(self):
        """Check that calling LCM.handle() from two threads at once is not
        allowed.
        """
        lcm_obj = lcm.LCM("memq://")

        def thread_func():
            lcm_obj.handle()

        lcm_obj.subscribe("channel", lambda *x: None)

        worker = threading.Thread(target=thread_func)
        worker.start()
        time.sleep(0.1)

        with self.assertRaises(RuntimeError):
            lcm_obj.handle()

        lcm_obj.publish("channel", "")

        worker.join()

def main():
    unittest.main()

if __name__ == '__main__':
    main()
