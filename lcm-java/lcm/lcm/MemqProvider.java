package lcm.lcm;

import java.util.concurrent.*;

public class MemqProvider implements Provider
{
    static class Message
    {
        String  channel;
        byte data[];
    }

    LCM lcm;

    ReaderThread reader;
    LinkedBlockingQueue<Message> queue;

    public MemqProvider(LCM lcm, URLParser up)
    {
        this.lcm = lcm;

        this.queue = new LinkedBlockingQueue<Message>();
        reader = new ReaderThread();
        reader.start();
    }

    public void publish(String channel, byte data[], int offset, int length)
    {
        Message msg = new Message();
        msg.channel = channel;
        msg.data = new byte[length];
        System.arraycopy(data, offset, msg.data, 0, length);
        try {
          queue.put(msg);
        } catch (InterruptedException ex) {
        }
    }

    public synchronized void subscribe(String channel) {}
    public void unsubscribe(String channel) {}

    public synchronized void close()
    {
        if (reader != null) {
            reader.interrupt();
            try {
                reader.join();
            } catch (InterruptedException ex) {
            }
        }
        reader = null;
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
            }
        }

        void runEx() throws InterruptedException
        {
            while (true) {
                Message msg = queue.take();
                lcm.receiveMessage(msg.channel, msg.data, 0, msg.data.length);
            }
        }
    }
}
