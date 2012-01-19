import sys
import time
import array
import random
import select

import lcm

import lcmtest

def _lcm_handle_timeout(lc, ms):
    try:
        rfds, wfds, efds = select.select([lc.fileno()], [], [], ms * 1e-3)
        if rfds:
            lc.handle()
            return True
        return False
    except KeyboardInterrupt:
        return False

def info(txt):
    sys.stderr.write("client: %s\n" % txt)

def check_field(actual, expected, field):
    if actual != expected:
        raise ValueError("reply.%s : Expected %s, got %s" % (field, actual, expected))

class MultidimArrayTest(object):
    def get_message_type(self):
        return lcmtest.multidim_array_t

    def get_num_iters(self):
        return 5

    def make_message(self, iteration):
        msg = lcmtest.multidim_array_t()
        msg.size_a = iteration
        msg.size_b = iteration
        msg.size_c = iteration
        msg.data = []
        n = 0
        for i in range(msg.size_a):
            item_a = []
            msg.data.append(item_a)
            for j in range(msg.size_b):
                item_b = []
                item_a.append(item_b)
                for k in range(msg.size_c):
                    item_b.append(n)
                    n += 1

        n = 0
        msg.strarray = []
        for i in range(2):
            item_a = []
            msg.strarray.append(item_a)
            for k in range(msg.size_c):
                item_a.append("%d" % n)
                n += 1
        return msg

    def check_reply(self, reply, iteration):
        check_field(reply.size_a, iteration, "size_a")
        check_field(reply.size_b, iteration, "size_b")
        check_field(reply.size_c, iteration, "size_c")

        n = 0
        for i in range(reply.size_a):
            for j in range(reply.size_b):
                for k in range(reply.size_c):
                    check_field(reply.data[i][j][k], n, "data[%d][%d][%d]" % (i, j, k))
                    n += 1

        n = 0
        for i in range(2):
            for k in range(reply.size_c):
                check_field(reply.strarray[i][k], "%d" % n, "strarray[%d][%d]" % (i, k))
                n += 1

class NodeTest(object):
    def get_message_type(self):
        return lcmtest.node_t

    def get_num_iters(self):
        return 7

    def make_message(self, iteration):
        msg = lcmtest.node_t()
        msg.num_children = iteration
        if not msg.num_children:
            return msg
        msg.children = [ self.make_message(iteration - 1) for i in range(msg.num_children) ]
        return msg

    def check_reply(self, reply, iteration):
        check_field(reply.num_children, iteration, "num_children")
        for child in reply.children:
            self.check_reply(child, iteration - 1)

class PrimitivesListTest(object):
    def get_message_type(self):
        return lcmtest.primitives_list_t

    def get_num_iters(self):
        return 100

    def make_message(self, iteration):
        msg = lcmtest.primitives_list_t()
        msg.num_items = iteration
        msg.items = [ lcmtest.primitives_t() for i in range(msg.num_items) ]
        for n in range(msg.num_items):
            ex = msg.items[n]
            ex.i8 = -(n % 100)
            ex.i16 = -n * 10
            ex.i64 = -n * 10000
            ex.position[0] = -n
            ex.position[1] = -n
            ex.position[2] = -n
            ex.orientation[0] = -n
            ex.orientation[1] = -n
            ex.orientation[2] = -n
            ex.orientation[3] = -n
            ex.num_ranges = n
            ex.ranges = [ -i for i in range(n) ]
            ex.name = "%d" % -n
            ex.enabled = bool((n+1) % 2)
        return msg

    def check_reply(self, reply, iteration):
        check_field(reply.num_items, iteration, "num_items")
        for n in range(iteration):
            ex = reply.items[n]
            pa = ".items[%d]." % n
            check_field(ex.i8, -(n % 100), pa + "i8")
            check_field(ex.i16, -n * 10, pa + "i16")
            check_field(ex.i64, -n * 10000, pa + "i64")
            check_field(ex.position[0], float(-n), pa + "position[0]")
            check_field(ex.position[1], float(-n), pa + "position[1]")
            check_field(ex.position[2], float(-n), pa + "position[2]")
            check_field(ex.orientation[0], float(-n), pa + "orientation[0]")
            check_field(ex.orientation[1], float(-n), pa + "orientation[1]")
            check_field(ex.orientation[2], float(-n), pa + "orientation[2]")
            check_field(ex.orientation[3], float(-n), pa + "orientation[3]")
            check_field(ex.num_ranges, n, pa + "num_ranges")
            for i in range(n):
                check_field(ex.ranges[i], -i, pa + "ranges[%d]" % i)
            check_field(ex.name, "%d" % -n, pa + "name")
            check_field(ex.enabled, bool((n+1)%2), pa + "enabled")

class PrimitivesTest(object):
    def get_message_type(self):
        return lcmtest.primitives_t

    def get_num_iters(self):
        return 1000

    def make_message(self, iteration):
        n = iteration
        msg = lcmtest.primitives_t()
        msg.i8 = n % 100
        msg.i16 = n * 10
        msg.i64 = n * 10000
        msg.position[0] = n
        msg.position[1] = n
        msg.position[2] = n
        msg.orientation[0] = n
        msg.orientation[1] = n
        msg.orientation[2] = n
        msg.orientation[3] = n
        msg.num_ranges = n
        msg.ranges = [ i for i in range(msg.num_ranges) ]
        msg.name = "%d" % n
        msg.enabled = n % 2
        return msg

    def check_reply(self, reply, iteration):
        n = iteration
        check_field(reply.i8, n % 100, "i8")
        check_field(reply.i16, n * 10, "i16");
        check_field(reply.i64, n * 10000, "i64");
        check_field(reply.position[0], float(n), "position[0]");
        check_field(reply.position[1], float(n), "position[1]");
        check_field(reply.position[2], float(n), "position[2]");
        check_field(reply.orientation[0], float(n), "orientation[0]");
        check_field(reply.orientation[1], float(n), "orientation[1]");
        check_field(reply.orientation[2], float(n), "orientation[2]");
        check_field(reply.orientation[3], float(n), "orientation[3]");
        check_field(reply.num_ranges, n, "%d");
        for i in range(n):
            check_field(reply.ranges[i], i, "ranges[%d]" % i)
        check_field(reply.name, "%d" % n, "name")
        check_field(reply.enabled, bool(n % 2), "enabled")

class StandardTester(object):
    def __init__(self, lc, test):
        self.lc = lc
        self.test = test
        self.failed = False
        self.msg_type = test.get_message_type()
        self.msg_name = self.msg_type.__name__

    def handler(self, channel, data):
        try:
            self.test.check_reply(self.msg_type.decode(data), self.iteration + 1)
            self.response_count += 1
        except ValueError, xcp:
            self.failed = True
            info(str(xcp))

    def run(self):
        self.response_count = 0
        pub_channel = "test_lcmtest_%s" % self.msg_name
        subs = self.lc.subscribe("test_lcmtest_%s_reply" % self.msg_name, self.handler)
        for iteration in range(self.test.get_num_iters()):
            self.iteration = iteration
            msg = self.test.make_message(iteration)
            self.lc.publish(pub_channel, msg.encode())

            if not _lcm_handle_timeout(self.lc, 500):
                info("%-17s : Timeout waiting for reply (iteration %d)" % (self.msg_name, iteration));
                return False
            elif self.failed:
                info("%-17s : Error on iteration %d" % (self.msg_name, iteration))
                return False

        self.lc.unsubscribe(subs)
        info("lcmtest_%-17s : PASSED" % self.msg_name)
        return True


class EchoTester(object):
    def __init__(self, lc):
        self.lc = lc
        self.data = None
        self.response_count = 0
        self.subs = self.lc.subscribe("TEST_ECHO_REPLY", self.handler)
        self.num_iters = 100

    def handler(self, channel, data):
        if data != self.data:
            return
        self.response_count += 1

    def run(self):
        for i in range(self.num_iters):
            datalen = random.randint(10, 10000)
            nums = [ random.randint(-128, 127) for j in range(datalen) ]
            self.data = array.array('b', nums).tostring()
            self.lc.publish("TEST_ECHO", self.data)

            if not _lcm_handle_timeout(self.lc, 500) or self.response_count != i + 1:
                print("echo test failed to receive a response on iteration %d" % i)
                raise RuntimeError()
        info("%-25s : PASSED" % "echo test")
        self.lc.unsubscribe(self.subs)

class Tester(object):
    def __init__(self):
        self.lc = lcm.LCM()

    def run(self):
        EchoTester(self.lc).run()
        StandardTester(self.lc, PrimitivesTest()).run()
        StandardTester(self.lc, PrimitivesListTest()).run()
        StandardTester(self.lc, NodeTest()).run()
        StandardTester(self.lc, MultidimArrayTest()).run()
        info("All tests passed")

if __name__ == "__main__":
    random.seed()
    Tester().run()
