package lcm.test;

import lcm.lcm.*;
import java.io.*;

public class SlowLCMSubscriber implements LCMSubscriber
{
    LCM lcm;

    public SlowLCMSubscriber()
    {
	try {
	    lcm = new LCM("tcp://");
	    lcm.subscribe(".*", this);
	} catch (IOException ex) {
	    System.out.println("Ex: "+ex);
	}
    }

    public void messageReceived(LCM lcm, String channel, DataInputStream ins)
    {
	try {
	    Thread.sleep(10);
	} catch (InterruptedException ex) {
	}
    }

    public static void main(String args[])
    {
	new SlowLCMSubscriber();
    }

}
