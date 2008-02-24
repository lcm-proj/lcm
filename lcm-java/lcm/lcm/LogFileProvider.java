package lcm.lcm;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.nio.*;

import lcm.logging.*;

public class LogFileProvider implements Provider
{
    LCM lcm;
    Log log;

    double speed; // how fast do we play? <=0 for "as fast as possible"
    double delay; // how many seconds to delay before starting to play? (crude race-condition hack)
    boolean verbose; // report actual speed periodically

    public LogFileProvider(LCM lcm, String url) throws IOException
    {
	this.lcm = lcm;

	URLParser up = new URLParser(url);

	String logPath = up.get("host","");
	log = new Log(logPath, "r");

	speed = up.get("speed", 1.0);
	delay = up.get("delay", 0.5);
	verbose = up.get("verbose", false);

	new ReaderThread().start();
    }

    boolean publishWarned = false;
    public synchronized void publish(String channel, byte data[], int offset, int length) 
    {
	if (publishWarned)
	    return;

	System.out.println("LogFileProvider does not support publishing");
	publishWarned = true;
    }

    class ReaderThread extends Thread
    {
	ReaderThread()
	{
	    setDaemon(true);
	}

	public void run()
	{
	    try {
		runEx();
	    } catch (InterruptedException ex) {
		System.out.println("ex: "+ex);
	    } catch (IOException ex) {
		System.out.println("ex: "+ex);
	    }
	}

	void runEx() throws IOException, InterruptedException
	{
	    while (lcm.getNumSubscriptions()==0)
		Thread.sleep(10);

	    Thread.sleep((int) (delay*1000));

	    long lastLocalUtime = System.nanoTime()/1000;
	    long lastEventUtime = -1;
	    double delayAccumulator = 0; // how long do we need to delay in real time?

	    double verboseAccumulator = 0;
	    long   verboseLastEventUtime = -1;

	    while (true) {
		Log.Event ev = log.readNext();

		// how much time elapsed when the log was recorded?
		// we have to wait some fraction of the time...
		if (lastEventUtime > 0) {
		    double dt = (ev.utime - lastEventUtime) / 1000000.0;
		    if (dt > 0 && speed > 0)
			delayAccumulator += dt / speed;
		}
		lastEventUtime = ev.utime;

		// subtract any clock time that has elapsed.
		long localUtime = System.nanoTime()/1000;
		double dt = (localUtime - lastLocalUtime)/1000000.0;
		lastLocalUtime = localUtime;
		delayAccumulator -= dt;
		verboseAccumulator += dt;

		// spit out some info at 1Hz
		if (verboseAccumulator > 1.0 && verbose) {
		    double eventDt = (lastEventUtime - verboseLastEventUtime)/1000000.0;
		    verboseLastEventUtime = lastEventUtime;
		    System.out.printf("LogFile: rate = %8.3f, position = %8.3f %%\n", 
				      eventDt/verboseAccumulator,
				      log.getPositionFraction()*100.0);
		    verboseAccumulator = 0;
		}

		// sleep if necessary.
		if (delayAccumulator > 0.001) {
		    int ms = (int) (delayAccumulator*1000);
		    Thread.sleep(ms);
		}

		// dispatch the message
		lcm.receiveMessage(ev.channel, ev.data, 0, ev.data.length);
	    }
	}
    }
}
