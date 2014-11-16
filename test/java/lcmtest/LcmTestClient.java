import java.io.IOException;
import java.io.DataInput;
import java.lang.IllegalArgumentException;
import java.lang.Thread;
import java.util.Arrays;
import java.util.Random;

import lcm.lcm.LCM;
import lcm.lcm.LCMSubscriber;
import lcm.lcm.LCMDataInputStream;

import lcmtest.primitives_t;
import lcmtest.primitives_list_t;
import lcmtest.node_t;
import lcmtest.multidim_array_t;
import lcmtest2.cross_package_t;
import lcmtest2.another_type_t;

public class LcmTestClient implements LCMSubscriber {
    private boolean test_success_ = false;
    private boolean test_failed_ = false;
    private boolean any_test_failed_ = false;

    private LCM lcm_;
    private byte echo_data_[];
    private int echo_response_count_ = 0;
    private Random random_ = new Random();

    private long abort_time_ = 0;

    LcmTestClient() {}

    static private <T> void checkField(T actual, T expected, String field) {
        if (!actual.equals(expected)) {
            throw new IllegalArgumentException(
                    String.format("%s : Expected %s, got %s",
                    field, String.valueOf(expected), String.valueOf(actual)));
        }
    }

    private void setupSubscribers() throws IOException {
        lcm_ = new LCM();

        lcm_.subscribe("TEST_ECHO_REPLY", this);
    }

    public void messageReceived(LCM lcm, String channel,
        LCMDataInputStream ins) {
        if (channel.equals("TEST_ECHO_REPLY")) {
            handleEchoReply(ins);
        }
    }

    private void handleEchoReply(LCMDataInputStream ins) {
        try {
            int len = ins.available();
            byte data[] = new byte[len];
            ins.readFully(data);

            if (!Arrays.equals(data, echo_data_)) {
                test_failed_ = true;
                return;
            }

            echo_response_count_++;
            if (echo_response_count_ >= 100) {
                test_success_ = true;
            } else {
                sendNextEchoMessage();
            }
        } catch (IOException ex) {
            System.err.println("Failed to handle echo reply");
        }
    }

    private void sendNextEchoMessage() throws IOException {
        echo_data_ = new byte[random_.nextInt(9990) + 10];
        random_.nextBytes(echo_data_);
        lcm_.publish("TEST_ECHO", echo_data_, 0, echo_data_.length);
    }

    private boolean waitForSuccessOrAbort() {
        while(true) {
            synchronized(this) {
                if (test_success_) {
                    return true;
                }
            }
            try {
                Thread.sleep(100);
            } catch (java.lang.InterruptedException ex) {
                return false;
            }

            if (System.currentTimeMillis() >= abort_time_) {
                System.err.printf("Timed out");
                test_failed_ = true;
                return false;
            }
        }
    }

    private boolean doEchoTest() throws IOException {
        echo_data_ = new byte[10000];
        echo_response_count_ = 0;
        sendNextEchoMessage();
        abort_time_ = System.currentTimeMillis() + 500;
        return waitForSuccessOrAbort();
    }

    private interface MessageMakerChecker<MsgType> {
        public MsgType makeMessage(int iteration);
        public boolean checkReply(MsgType msg, int iteration);
    }

    private class MessageTester<MsgType extends lcm.lcm.LCMEncodable> implements LCMSubscriber {
        private MessageMakerChecker<MsgType> makerChecker_;
        private String name_;
        private int iteration_ = 0;
        private int numIterations_;
        private LCM lcm_;
        private long abortTime_;
        private boolean success_ = false;
        private boolean done_ = false;
        private Class type_;

        MessageTester(LCM lcm, String name, MessageMakerChecker<MsgType> makerChecker,
                int numIterations) {
            lcm_ = lcm;
            makerChecker_ = makerChecker;
            name_ = name;
            numIterations_ = numIterations;
        }

        public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins) {
            MsgType msg;
            try {
                msg = (MsgType)type_.getConstructor(DataInput.class).newInstance(ins);
            } catch (java.lang.Exception ex) {
                done_ = true;
                return;
            }
            if (!makerChecker_.checkReply(msg, iteration_ + 1)) {
                success_ = false;
                done_ = true;
                return;
            }
            abortTime_ = System.currentTimeMillis() + 500;
            iteration_++;
            if (iteration_ >= numIterations_) {
                success_ = true;
                done_ = true;
                return;
            }
            sendMessage();
        }

        private void sendMessage() {
            MsgType msg = makerChecker_.makeMessage(iteration_);
            lcm_.publish("test_" + name_, msg);
        }

        public boolean run() {
            lcm_.subscribe("test_" + name_ + "_reply", this);
            abortTime_ = System.currentTimeMillis() + 500;
            MsgType msg = makerChecker_.makeMessage(iteration_);
            lcm_.publish("test_" + name_, msg);
            type_ = msg.getClass();
            while(true) {
                synchronized(this) {
                    if (done_) {
                        break;
                    }
                }
                try {
                    Thread.sleep(10);
                } catch (java.lang.InterruptedException ex) {
                    break;
                }

                if (System.currentTimeMillis() >= abortTime_) {
                    System.err.printf("Timed out on iteration %d\n", iteration_);
                    break;
                }
            }
            lcm_.unsubscribe("test_" + name_ + "_reply", this);
            if (success_) {
                System.out.println("      OK - " + name_);
            } else {
                System.out.printf(" FAIL    - %s - iteration %d\n", name_, iteration_);
            }
            return success_;
        }
    }

    private class PrimitivesMakerChecker implements MessageMakerChecker<primitives_t> {
        public primitives_t makeMessage(int iteration) {
            primitives_t msg = new primitives_t();
            int n = iteration;
            msg.i8 = (byte)(n % 100);
            msg.i16 = (short)(n * 10);
            msg.i64 = n * 10000;
            msg.position[0] = n;
            msg.position[1] = n;
            msg.position[2] = n;
            msg.orientation[0] = n;
            msg.orientation[1] = n;
            msg.orientation[2] = n;
            msg.orientation[3] = n;
            msg.num_ranges= n;
            msg.ranges = new short[n];
            msg.name = String.valueOf(n);
            msg.enabled = n % 2 == 1;
            return msg;
        }

        public boolean checkReply(primitives_t reply, int iteration) {
            try {
                int n = iteration;
                checkField(reply.i8, (byte)(n % 100), "i8");
                checkField(reply.i16, (short)(n * 10), "i16");
                checkField((long)reply.i64, (long)(n * 10000), "i64");
                checkField(reply.position[0], (float)n, "position[0]");
                checkField(reply.position[1], (float)n, "position[1]");
                checkField(reply.position[2], (float)n, "position[2]");
                checkField(reply.orientation[0], (double)n, "orientation[0]");
                checkField(reply.orientation[1], (double)n, "orientation[1]");
                checkField(reply.orientation[2], (double)n, "orientation[2]");
                checkField(reply.orientation[3], (double)n, "orientation[3]");
                checkField(reply.num_ranges, n, "num_ranges");
                for (int i = 0; i < reply.num_ranges; i++) {
                    checkField(reply.ranges[i], (short)i, String.format("ranges[%d]", i));
                }
                checkField(reply.name, String.valueOf(n), "name");
                checkField(reply.enabled, n % 2 == 1, "enabled");
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace(System.err);
                return false;
            }
            return true;
        }
    }

    private class PrimitivesListMakerChecker implements MessageMakerChecker<primitives_list_t> {
        public primitives_list_t makeMessage(int iteration) {
            primitives_list_t msg = new primitives_list_t();
            msg.num_items = iteration;
            msg.items = new primitives_t[msg.num_items];
            for (int n = 0; n < msg.num_items; n++) {
                primitives_t submsg = new primitives_t();
                msg.items[n] = submsg;
                submsg.i8 = (byte)(-(n % 100));
                submsg.i16 = (short)(-n * 100);
                submsg.i64 = (long)(-n * 10000);
                submsg.position[0] = -n;
                submsg.position[1] = -n;
                submsg.position[2] = -n;
                submsg.orientation[0] = -n;
                submsg.orientation[1] = -n;
                submsg.orientation[2] = -n;
                submsg.orientation[3] = -n;
                submsg.num_ranges = n;
                submsg.ranges = new short[n];
                for (int j = 0; j < n; j++) {
                    submsg.ranges[j] = (short)-j;
                }
                submsg.name = String.format("%d", -n);
                submsg.enabled = ((n + 1) % 2) == 1;
            }
            return msg;
        }

        public boolean checkReply(primitives_list_t reply, int iteration) {
            try {
                checkField(reply.num_items, iteration, "num_items");
                for (int n = 0; n < iteration; n++) {
                    primitives_t submsg = reply.items[n];
                    String errPrefix = String.format(".items[%d].", n);
                    checkField(submsg.i8, (byte)(-(n % 100)), errPrefix + "i8");
                    checkField(submsg.i16, (short)(-n * 10), errPrefix + "i8");
                    checkField(submsg.i64, (long)(-n * 10000), errPrefix + "i8");
                    checkField(submsg.position[0], (float)-n, errPrefix + "position[0]");
                    checkField(submsg.position[1], (float)-n, errPrefix + "position[1]");
                    checkField(submsg.position[2], (float)-n, errPrefix + "position[2]");
                    checkField(submsg.orientation[0], (double)-n, errPrefix + "orientation[0]");
                    checkField(submsg.orientation[1], (double)-n, errPrefix + "orientation[1]");
                    checkField(submsg.orientation[2], (double)-n, errPrefix + "orientation[2]");
                    checkField(submsg.orientation[3], (double)-n, errPrefix + "orientation[3]");
                    checkField(submsg.num_ranges, n, errPrefix + "num_ranges");
                    for (int i = 0; i < submsg.num_ranges; i++) {
                        checkField(submsg.ranges[i], (short)-i, errPrefix + String.format("ranges[%d]", i));
                    }
                    checkField(submsg.name, String.valueOf(-n), errPrefix + "name");
                    checkField(submsg.enabled, ((n + 1) % 2) == 1, errPrefix + "enabled");
                }
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace(System.err);
                return false;
            }
            return true;
        }
    }

    private class NodeMakerChecker implements MessageMakerChecker<node_t> {
        public node_t makeMessage(int iteration) {
            node_t msg = new node_t();
            msg.num_children = iteration;
            if (msg.num_children == 0) {
                return msg;
            }
            msg.children = new node_t[msg.num_children];
            for (int i = 0; i < msg.num_children; i++) {
                msg.children[i] = makeMessage(iteration - 1);
            }
            return msg;
        }

        public boolean checkReply(node_t reply, int iteration) {
            try {
                checkField(reply.num_children, iteration, "num_children");
                for (int i = 0; i < reply.num_children; i++) {
                    if (!checkReply(reply.children[i], iteration -1)) {
                        return false;
                    }
                }
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace(System.err);
                return false;
            }
            return true;
        }
    }

    private class MultidimArrayMakerChecker implements MessageMakerChecker<multidim_array_t> {
        public multidim_array_t makeMessage(int iteration) {
            multidim_array_t msg = new multidim_array_t();
            msg.size_a = iteration;
            msg.size_b = iteration;
            msg.size_c = iteration;
            msg.data = new int[msg.size_a][msg.size_b][msg.size_c];
            int n = 0;
            for (int a_iter = 0; a_iter < msg.size_a; a_iter++) {
                for (int b_iter = 0; b_iter < msg.size_b; b_iter++) {
                    for (int c_iter = 0; c_iter < msg.size_c; c_iter++) {
                        msg.data[a_iter][b_iter][c_iter] = n++;
                    }
                }
            }
            n = 0;
            msg.strarray = new String[2][msg.size_c];
            for (int i = 0; i < 2; i++) {
                for (int c_iter = 0; c_iter < msg.size_c; c_iter++) {
                    msg.strarray[i][c_iter] = String.valueOf(n);
                    n++;
                }
            }
            return msg;
        }

        public boolean checkReply(multidim_array_t reply, int iteration) {
            try {
                checkField(reply.size_a, iteration, "size_a");
                checkField(reply.size_b, iteration, "size_b");
                checkField(reply.size_c, iteration, "size_c");
                int n = 0;
                for (int a_iter = 0; a_iter < reply.size_a; a_iter++) {
                    for (int b_iter = 0; b_iter < reply.size_b; b_iter++) {
                        for (int c_iter = 0; c_iter < reply.size_c; c_iter++) {
                            checkField(reply.data[a_iter][b_iter][c_iter], n,
                                    String.format("data[%d][%d][%d]", a_iter, b_iter, c_iter));
                            n++;
                        }
                    }
                }
                n = 0;
                for (int i = 0; i < 2; i++) {
                    for (int c_iter = 0; c_iter < reply.size_c; c_iter++) {
                        checkField(reply.strarray[i][c_iter],
                                String.valueOf(n),
                                String.format("strarray[%d][%d]", i, c_iter));
                        n++;
                    }
                }
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace(System.err);
                return false;
            }
            return true;
        }
    }

    private class CrossPackageMakerChecker implements MessageMakerChecker<cross_package_t> {
        private PrimitivesMakerChecker primitivesMakerChecker_ =
            new PrimitivesMakerChecker();

        public cross_package_t makeMessage(int iteration) {
            cross_package_t msg = new cross_package_t();
            msg.primitives = primitivesMakerChecker_.makeMessage(iteration);
            msg.another = new another_type_t();
            msg.another.val = iteration;
            return msg;
        }

        public boolean checkReply(cross_package_t reply, int iteration) {
            try {
                if (!primitivesMakerChecker_.checkReply(reply.primitives, iteration)) {
                    return false;
                }
                checkField(reply.another.val, iteration, "another.val");
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace(System.err);
                return false;
            }
            return true;
        }
    }

    public int runTests() {
        try {
            setupSubscribers();

            boolean echo_test_success = doEchoTest();
            if (echo_test_success) {
                System.out.println("      OK - echo test");
            } else {
                System.out.println(" FAIL    - echo test");
                return 1;
            }

            MessageTester<primitives_t> primitives_tester = new MessageTester<primitives_t>(
                    lcm_, "lcmtest_primitives_t", new PrimitivesMakerChecker(), 1000);
            if (!primitives_tester.run()) {
                return 1;
            }

            MessageTester<primitives_list_t> primitives_list_tester = new MessageTester<primitives_list_t>(
                    lcm_, "lcmtest_primitives_list_t", new PrimitivesListMakerChecker(), 100);
            if (!primitives_list_tester.run()) {
                return 1;
            }

            MessageTester<node_t> node_tester = new MessageTester<node_t>(
                    lcm_, "lcmtest_node_t", new NodeMakerChecker(), 7);
            if (!node_tester.run()) {
                return 1;
            }

            MessageTester<multidim_array_t> multidim_array_tester = new MessageTester<multidim_array_t>(
                    lcm_, "lcmtest_multidim_array_t", new MultidimArrayMakerChecker(), 5);
            if (!multidim_array_tester.run()) {
                return 1;
            }

            MessageTester<cross_package_t> cross_package_tester = new MessageTester<cross_package_t>(
                    lcm_, "lcmtest2_cross_package_t", new CrossPackageMakerChecker(), 100);
            if (!cross_package_tester.run()) {
                return 1;
            }
        } catch(IOException ex) {
            System.err.println("IOException");
            return 1;
        }

        return 0;
    }

    public static void main(String[] args) {
        LcmTestClient client = new LcmTestClient();
        System.exit(client.runTests());
    }
}
