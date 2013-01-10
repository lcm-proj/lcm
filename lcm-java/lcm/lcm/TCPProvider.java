package lcm.lcm;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.nio.*;

/** LCM provider for the tcpq: URL. All messages are sent to a central
 * "hub" process (that must be started separately), which will relay
 * the messages to all other processes. TCPService is an
 * implementation of the hub process.
 *
 * The tcpq:// protocol is NOT suitable for real-time or high-bandwidth
 * traffic.  It is specifically designed for playing back a log file in a
 * post-processing context (i.e., play back the log as fast as possible, but
 * without dropping anything).
 **/
public class TCPProvider implements Provider
{
    LCM lcm;

    static final int DEFAULT_PORT = 7700;
    static final String DEFAULT_NETWORK = "127.0.0.1:7700";
    InetAddress inetAddr;
    int         inetPort;

    TCPThread tcp;

    public static final int MAGIC_SERVER = 0x287617fa; // first word sent by server
    public static final int MAGIC_CLIENT = 0x287617fb; // first word sent by client
    public static final int VERSION = 0x0100;    // what version do we implement?
    public static final int MESSAGE_TYPE_PUBLISH = 1;
    public static final int MESSAGE_TYPE_SUBSCRIBE = 2;
    public static final int MESSAGE_TYPE_UNSUBSCRIBE = 3;

    HashSet<String> subscriptions = new HashSet<String>();

    public TCPProvider(LCM lcm, URLParser up) throws IOException
    {
        this.lcm = lcm;

        String addrport[] = up.get("network", DEFAULT_NETWORK).split(":");
        if (addrport.length == 1) {
            inetAddr = InetAddress.getByName(addrport[0]);
            inetPort = 7700;
        } else if (addrport.length == 2) {
            inetAddr = InetAddress.getByName(addrport[0]);
            inetPort = Integer.valueOf(addrport[1]);
        } else {
            System.err.println("TCPProvider: Don't know how to parse "+up.get("network", DEFAULT_NETWORK));
            System.exit(-1);
        }

        tcp = new TCPThread();
        tcp.start();
    }

    /** Publish a message synchronously. However, if the server is not
     * available, it will return immediately.
     **/
    public synchronized void publish(String channel, byte data[], int offset, int length)
    {
        try {
            publishEx(channel, data, offset, length);
        } catch (Exception ex) {
            System.err.println("TCPProvider ex: "+ex);
        }
    }

    byte[] stringToBytes(String s)
    {
        try {
            return s.getBytes("US-ASCII");
        } catch(UnsupportedEncodingException ex) {
            System.err.println("lcm.TCPProvider: Bad channel name" + s);
            throw new RuntimeException("Don't know how to recover from this");
        }
    }

/*    private void sockWriteAndFlush(byte[] b)
    {
        // try to send message on socket. If the socket is not
        // connected, we'll simply fail. The tcpthread is
        // responsible for maintaining a connection to the hub.
        OutputStream sockOuts = tcp.getOutputStream();
        if (sockOuts != null) {
            try {
                sockOuts.write(b);
                sockOuts.flush();
            } catch (IOException ex) {
            }
        }
    }
*/
    public synchronized void subscribe(String channel)
    {
        subscriptions.add(channel);
        tcp.sendSubscribe(channel);
    }

    public synchronized void unsubscribe(String channel)
    {
        subscriptions.remove(channel);
        tcp.sendUnsubscribe(channel);
    }

    public synchronized void close()
    {
        if (null != tcp) {
            tcp.close();
            try {
                tcp.join();
            } catch (InterruptedException ex) {
            }
        }

        tcp = null;
    }

    static final void safeSleep(int ms)
    {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
        }
    }

    void publishEx(String channel, byte data[], int offset, int length) throws Exception
    {
        byte[] channel_bytes = stringToBytes(channel);

        int payload_size = channel_bytes.length + length;

        ByteArrayOutputStream bouts = new ByteArrayOutputStream(length + channel.length() + 32);
        DataOutputStream outs = new DataOutputStream(bouts);

        outs.writeInt(MESSAGE_TYPE_PUBLISH);

        outs.writeInt(channel_bytes.length);
        outs.write(channel_bytes, 0, channel_bytes.length);

        outs.writeInt(length);
        outs.write(data, offset, length);

        tcp.write(bouts.toByteArray());
    }

    // synchronize on writes and to changes in subscription state.
    class TCPThread extends Thread
    {
        Socket sock;
        DataInputStream ins;
        OutputStream outs;
        boolean exit = false;
        int serverVersion;

        TCPThread()
        {
        }

        synchronized void write(byte b[]) throws IOException
        {
            // if our connection is dead or not yet up, we just drop
            // this message.  (subscribes will be setup again when the
            // connection comes back up).
            if (outs == null)
                return;

            outs.write(b);
            outs.flush();
        }

        synchronized void sendSubscribe(String channel)
        {
            byte channel_bytes[] = stringToBytes(channel);

            try {
                ByteArrayOutputStream bouts = new ByteArrayOutputStream(channel.length() + 8);
                DataOutputStream outs = new DataOutputStream(bouts);

                outs.writeInt(MESSAGE_TYPE_SUBSCRIBE);
                outs.writeInt(channel_bytes.length);
                outs.write(channel_bytes, 0, channel_bytes.length);

                write(bouts.toByteArray());
            } catch (IOException ex) {
                System.out.println("ex: "+ex);
            }
        }

        synchronized void sendUnsubscribe(String channel)
        {
            byte channel_bytes[] = stringToBytes(channel);

            try {
                ByteArrayOutputStream bouts = new ByteArrayOutputStream(channel.length() + 8);
                DataOutputStream outs = new DataOutputStream(bouts);

                outs.writeInt(MESSAGE_TYPE_UNSUBSCRIBE);
                outs.writeInt(channel_bytes.length);
                outs.write(channel_bytes, 0, channel_bytes.length);

                write(bouts.toByteArray());
            } catch (IOException ex) {
            }
        }

        public void run()
        {
            while (!exit) {

                synchronized (this) {
                    //////////////////////////////////
                    // reconnect
                    try {
                        sock = new Socket(inetAddr, inetPort);
                        OutputStream _outs = sock.getOutputStream();
                        DataOutputStream _douts = new DataOutputStream(_outs);
                        _douts.writeInt(MAGIC_CLIENT);
                        _douts.writeInt(VERSION);
                        _douts.flush();
                        outs = _outs;
                        ins = new DataInputStream(new BufferedInputStream(sock.getInputStream()));

                        int magic = ins.readInt();
                        if (magic != MAGIC_SERVER) {
                            sock.close();
                            continue;
                        }

                        serverVersion = ins.readInt();

                    } catch (IOException ex) {
                        System.err.println("lcm.TCPProvider: Unable to connect to "+inetAddr+":"+inetPort);
                        safeSleep(500);

                        // try connecting again.
                        continue;
                    }

                    for (String sub : subscriptions) {
                        System.out.println("resending subscription "+sub);
                        sendSubscribe(sub);
                    }
                }

                //////////////////////////////////
                // read loop
                try {
                    while (!exit) {
                        int type = ins.readInt();
                        int channellen = ins.readInt();
                        byte channel[] = new byte[channellen];
                        ins.readFully(channel);
                        int datalen = ins.readInt();
                        byte data[] = new byte[datalen];
                        ins.readFully(data);

                        lcm.receiveMessage(new String(channel), data, 0, data.length);
                    }

                } catch (IOException ex) {
                    // exit read loop so we'll create a new connection.
                }
            }
        }

        void close()
        {
            try {
                sock.close();
            } catch (IOException ex) {
            }

            exit = true;
        }

        OutputStream getOutputStream()
        {
            return outs;
        }
    }
}
