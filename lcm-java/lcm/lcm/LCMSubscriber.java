package lcm.lcm;

import java.io.*;

/** A class which listens for messages on a particular channel. **/
public interface LCMSubscriber
{
    public void messageReceived(LCM lcm, String channel, DataInputStream ins);
}
