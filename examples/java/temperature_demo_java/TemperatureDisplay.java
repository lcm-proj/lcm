import lcm.lcm.*;
import java.io.*;

import lcmtypes.*;

public class TemperatureDisplay implements LCMSubscriber
{
    public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins)
    {
        try {
            temperature_t temp = new temperature_t(ins);
            System.out.println("The temperature is: "+temp.deg_celsius);
        } catch (IOException ex) {
            System.out.println("Error decoding temperature message: "+ex);
        }
    }

    public static void main(String args[])
    {
        LCM myLCM = LCM.getSingleton();

        myLCM.subscribe("HALLWAY_TEMPERATURE", new TemperatureDisplay());

        // Sleep forever: if we quit, so will the LCM thread.
        while (true)
	    {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
	    }
    }
}
