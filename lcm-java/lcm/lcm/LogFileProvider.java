package lcm.lcm;

import java.io.*;

import lcm.logging.*;

public class LogFileProvider implements Provider
{
    LCM lcm;
    Log log;

    double speed; // how fast do we play? <=0 for "as fast as possible"
    double delay; // how many seconds to delay before starting to play? (crude race-condition hack)
    boolean verbose; // report actual speed periodically
    double skip; // skip a fraction of the log file [0, 1.0]

    boolean writemode;
    long nanotime_start;
    long utime_start;

    ReaderThread reader;

    public LogFileProvider(LCM lcm, URLParser up) throws IOException
    {
        this.lcm = lcm;

        String logPath = up.get("network","");

        speed = up.get("speed", 1.0);
        delay = up.get("delay", 0.5);
        verbose = up.get("verbose", false);
        skip = up.get("skip", 0.0); // skip this fraction of the log file.
        writemode = up.get("mode", "r").equals("w");

        if(writemode) {
            log = new Log(logPath, "rw");
            nanotime_start = System.nanoTime();
            utime_start = System.currentTimeMillis() * 1000;
        } else {
            log = new Log(logPath, "r");
            reader = new ReaderThread();
            reader.start();
        }
    }

    boolean publishWarned = false;
    public synchronized void publish(String channel, byte data[], int offset, int length)
    {
        if(!writemode) {
            if (publishWarned)
                return;
            System.err.println("LogFileProvider opened in read mode, no publishing allowed.");
            publishWarned = true;
        }

        Log.Event event = new Log.Event();
        event.utime = utime_start + (System.nanoTime() - nanotime_start) / 1000;
        event.eventNumber = 0;
        event.data = new byte[length];
        System.arraycopy(data, offset, event.data, 0, length);
        event.channel = channel;
        try {
            log.write(event);
        } catch (IOException ex) {
            System.err.println("ex: "+ex);
        }
    }

    public synchronized void subscribe(String channel) { }
    public void unsubscribe(String channel) { }

    public synchronized void close() {
        if (reader != null) {
            reader.interrupt();
            try {
                reader.join();
            } catch (InterruptedException ex) {
            }
        }
        reader = null;
        try {
            log.close();
        } catch (IOException ex) {
        }
        log = null;
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
                return;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        void runEx() throws IOException, InterruptedException
        {
            log.seekPositionFraction(skip);

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
                    System.err.printf("LogFile: rate = %8.3f, position = %8.3f %%\n",
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
