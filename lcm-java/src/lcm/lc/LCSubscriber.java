package lcm.lc;

import java.io.*;

/** A class which listens for messages on a particular channel. **/
public interface LCSubscriber
{
    public void messageReceived(String channel, DataInputStream ins);
}
