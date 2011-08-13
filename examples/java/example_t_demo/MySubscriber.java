import java.io.*;

import lcm.lcm.*;
import exlcm.*;

public class MySubscriber implements LCMSubscriber
{
    LCM lcm;

    public MySubscriber()
        throws IOException
    {
        this.lcm = new LCM();
        this.lcm.subscribe("EXAMPLE", this);
    }

    public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins)
    {
        System.out.println("Received message on channel " + channel);

        try {
            if (channel.equals("EXAMPLE")) {
                example_t msg = new example_t(ins);

                System.out.println("  timestamp    = " + msg.timestamp);
                System.out.println("  position     = [ " + msg.position[0] +
                                   ", " + msg.position[1] + ", " + msg.position[2] + " ]");
                System.out.println("  orientation  = [ " + msg.orientation[0] +
                                   ", " + msg.orientation[1] +
                                   ", " + msg.orientation[2] +
                                   ", " + msg.orientation[3] + " ]");

                System.out.print("  ranges       = [ ");
                for (int i=0; i<msg.num_ranges; i++) {
                    System.out.print("" + msg.ranges[i]);
                    if (i < msg.num_ranges-1)
                        System.out.print (", ");
                }
                System.out.println (" ]");
                System.out.println("  name         = '" + msg.name + "'");
                System.out.println("  enabled      = '" + msg.enabled + "'");
            }

        } catch (IOException ex) {
            System.out.println("Exception: " + ex);
        }
    }

    public static void main(String args[])
    {
        try {
            MySubscriber m = new MySubscriber();
            while(true) {
                Thread.sleep(1000);
            }
        } catch (IOException ex) {
            System.out.println("Exception: " + ex);
        } catch (InterruptedException ex) { }
    }
}
