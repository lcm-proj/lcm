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

    def test_unsubscribe_during_handle(self):
        """Regression test for the data race between pylcm_unsubscribe and
        pylcm_msg_handler's prologue. Previously the handler dereferenced
        subs_obj->lcm_obj without holding the GIL, racing with unsubscribe
        nulling that field. The race window is tight and not reachable
        deterministically from pure Python, so this test stresses it across
        many iterations.
        """
        for iteration in range(50):
            lcm_obj = lcm.LCM("memq://")
            stop = threading.Event()

            def loop():
                while not stop.is_set():
                    lcm_obj.handle_timeout(10)

            def noop(channel, data):
                pass

            worker = threading.Thread(target=loop)
            worker.daemon = True
            worker.start()

            try:
                subs = []
                for i in range(200):
                    s = lcm_obj.subscribe("channel", noop)
                    subs.append(s)
                    lcm_obj.publish("channel", b"x")

                for s in subs:
                    lcm_obj.unsubscribe(s)
            finally:
                stop.set()
                worker.join(timeout=2.0)

            self.assertFalse(
                worker.is_alive(),
                "handle_timeout worker hung at iteration %d" % iteration)

    def test_unsubscribe_during_callback(self):
        """Deterministic regression: call unsubscribe() while a user
        callback is actively running in another thread, then let the
        callback raise. Previously this crashed in pylcm_msg_handler's
        epilogue, which wrote to subs_obj->lcm_obj->exception_raised after
        unsubscribe had nulled subs_obj->lcm_obj.
        """
        lcm_obj = lcm.LCM("memq://")
        in_callback = threading.Event()
        release_callback = threading.Event()

        def handler(channel, data):
            in_callback.set()
            release_callback.wait(timeout=5.0)
            raise ValueError("intentional - exercises epilogue path")

        sub = lcm_obj.subscribe("channel", handler)
        lcm_obj.publish("channel", b"x")

        def worker():
            try:
                lcm_obj.handle_timeout(5000)
            except ValueError:
                pass  # expected

        t = threading.Thread(target=worker)
        t.start()

        # Event.wait releases the GIL, so the main thread can now run.
        self.assertTrue(in_callback.wait(timeout=5.0),
                        "callback never reached")

        # Should complete cleanly even though the worker is still inside
        # PyObject_CallObject (under the broken code, the epilogue would
        # then deref a NULLed subs_obj->lcm_obj and segfault).
        lcm_obj.unsubscribe(sub)

        release_callback.set()
        t.join(timeout=5.0)

        self.assertFalse(t.is_alive())

    def test_python_lcm_lifetime_with_attributeeror(self):
        # if python reference counts are incorrect, this is enough to trigger
        # either a SEGV or an ASAN warning from PyType_Ready
        lcm_obj = lcm.LCM("memq://")
        with self.assertRaises(AttributeError):
            lcm_obj.does_not_have_this_attribute


def main():
    unittest.main()

if __name__ == '__main__':
    main()
