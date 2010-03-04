import lcm.lcm.*;

import lcmtypes.*;

public class TemperatureTransmit
{
    public static void main(String args[])
    {
        LCM myLCM = LCM.getSingleton();

        while (true)
	    {
            temperature_t temp = new temperature_t();
            temp.utime = System.nanoTime()/1000;
            temp.deg_celsius = 25.0 + 5*Math.sin(System.nanoTime()/1000000000.0);

            myLCM.publish("HALLWAY_TEMPERATURE", temp);

            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
            }
	    }
    }
}
