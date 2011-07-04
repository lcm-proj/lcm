import java.io.*;

import lcm.lcm.*;
import exlcm.*;

public class SendMessage
{
    public static void main(String args[])
    {
        try {
            LCM lcm = new LCM();

            example_t msg = new example_t();

            msg.timestamp = System.nanoTime();

            msg.position = new double[] { 1, 2, 3 };
            msg.orientation = new double[] { 1, 0, 0, 0 };

            msg.ranges = new short[] {
                0, 1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14
            };
            msg.num_ranges = msg.ranges.length;
            msg.name = "example string";
            msg.enabled = true;

            lcm.publish ("EXAMPLE", msg);
        } catch (IOException ex) {
            System.out.println("Exception: " + ex);
        }
    }
}
