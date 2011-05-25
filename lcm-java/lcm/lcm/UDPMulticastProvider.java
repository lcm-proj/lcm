package lcm.lcm;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.nio.*;

/** LCM provider for the udpm: URL. All messages are broadcast over a
 * pre-arranged UDP multicast address. Subscription operations are a
 * no-op, since all messages are always broadcast.
 *
 * This mechanism is very simple, low-latency, and efficient due to
 * not having to transmit messages more than once when there are
 * multiple subscribers. Since it uses UDP, it is lossy.
 **/
public class UDPMulticastProvider implements Provider
{
    MulticastSocket sock;

    static final String DEFAULT_NETWORK = "239.255.76.67:7667";
    static final int    DEFAULT_TTL     = 0;

    static final int    MAGIC_SHORT = 0x4c433032; // ascii of "LC02"
    static final int    MAGIC_LONG  = 0x4c433033; // ascii of "LC03"
    static final int    FRAGMENTATION_THRESHOLD = 64000;

    ReaderThread reader;

    int		msgSeqNumber=0;

    HashMap<SocketAddress, FragmentBuffer> fragBufs = new HashMap<SocketAddress, FragmentBuffer>();

    LCM lcm;

    InetAddress inetAddr;
    int         inetPort;

    static
    {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.err.println("LCM: Disabling IPV6 support");
    }

    public UDPMulticastProvider(LCM lcm, URLParser up) throws IOException
    {
        this.lcm = lcm;

        String addrport[] = up.get("network", DEFAULT_NETWORK).split(":");

        inetAddr = InetAddress.getByName(addrport[0]);
        inetPort = Integer.valueOf(addrport[1]);

        sock = new MulticastSocket(inetPort);

        sock.setReuseAddress(true);
        sock.setLoopbackMode(false); // true *disables* loopback

        int ttl = up.get("ttl", DEFAULT_TTL);
        if (ttl == 0)
            System.err.println("LCM: TTL set to zero, traffic will not leave localhost.");
        else if (ttl > 1)
            System.err.println("LCM: TTL set to > 1... That's almost never correct!");
        else
            System.err.println("LCM: TTL set to 1.");

        sock.setTimeToLive(up.get("ttl", DEFAULT_TTL));

        sock.joinGroup(inetAddr);
    }

    public synchronized void publish(String channel, byte data[], int offset, int length)
    {
        try {
            publishEx(channel, data, offset, length);
        } catch (Exception ex) {
            System.err.println("ex: "+ex);
        }
    }

    public synchronized void subscribe(String channel)
    {
        if (null == reader) {
            reader = new ReaderThread();
            reader.start();
        }
    }

    public void unsubscribe(String channel) { }

    public synchronized void close()
    {
        if (null != reader) {
            reader.interrupt();
            try {
                reader.join();
            } catch (InterruptedException ex) {
            }
        }
        reader = null;
        sock.close();
        sock = null;
        fragBufs = null;
    }

    void publishEx(String channel, byte data[], int offset, int length) throws Exception
    {
        byte[] channel_bytes = channel.getBytes("US-ASCII");

        int payload_size = channel_bytes.length + length;

        if (payload_size <= FRAGMENTATION_THRESHOLD) {

            LCMDataOutputStream outs = new LCMDataOutputStream(length + channel.length() + 32);

            outs.writeInt(MAGIC_SHORT);
            outs.writeInt(this.msgSeqNumber);

            outs.writeStringZ(channel);

            outs.write(data, offset, length);

            sock.send(new DatagramPacket(outs.getBuffer(), 0, outs.size(), inetAddr, inetPort));

        } else {
            int nfragments = payload_size / FRAGMENTATION_THRESHOLD;
            if (payload_size % FRAGMENTATION_THRESHOLD > 0) nfragments++;

            if (nfragments > 65535) {
                System.err.println("LC error: too much data for a single message");
                return;
            }

            // first fragment is special.  insert channel before data
            ByteArrayOutputStream bouts = new ByteArrayOutputStream(10 + FRAGMENTATION_THRESHOLD);
            DataOutputStream outs = new DataOutputStream(bouts);

            int fragment_offset = 0;
            int frag_no = 0;

            outs.writeInt(MAGIC_LONG);
            outs.writeInt(this.msgSeqNumber);
            outs.writeInt(length);
            outs.writeInt(fragment_offset);
            outs.writeShort(frag_no);
            outs.writeShort(nfragments);
            outs.write(channel_bytes, 0, channel_bytes.length);
            outs.writeByte(0);
            int firstfrag_datasize = FRAGMENTATION_THRESHOLD -
                (channel_bytes.length + 1);

            outs.write(data, offset, firstfrag_datasize);

            byte[] b = bouts.toByteArray();
            sock.send(new DatagramPacket(b, 0, b.length, inetAddr, inetPort));

            fragment_offset += firstfrag_datasize;

            for (frag_no=1; frag_no < nfragments; frag_no++) {
                bouts = new ByteArrayOutputStream(10 + FRAGMENTATION_THRESHOLD);
                outs = new DataOutputStream(bouts);

                outs.writeInt(MAGIC_LONG);
                outs.writeInt(this.msgSeqNumber);
                outs.writeInt(length);
                outs.writeInt(fragment_offset);
                outs.writeShort(frag_no);
                outs.writeShort(nfragments);
                int fraglen = java.lang.Math.min(FRAGMENTATION_THRESHOLD, length - fragment_offset);
                outs.write(data, offset+fragment_offset, fraglen);

                b = bouts.toByteArray();
                sock.send(new DatagramPacket(b, 0, b.length, inetAddr, inetPort));

                fragment_offset += fraglen;
            }
        }

        this.msgSeqNumber++;
    }

    class FragmentBuffer
    {
        SocketAddress from = null;
        String channel = null;
        int msgSeqNumber = 0;
        int data_size = 0;
        int fragments_remaining = 0;
        byte[] data = null;
        boolean frag_received[];

        FragmentBuffer(SocketAddress from, String channel, int msgSeqNumber, int data_size,
                       int fragments_remaining)
        {
            this.from = from;
            this.channel = channel;
            this.msgSeqNumber = msgSeqNumber;
            this.data_size = data_size;
            this.fragments_remaining = fragments_remaining;
            this.data = new byte[data_size];
            this.frag_received = new boolean[fragments_remaining];
        }
    }

    class ReaderThread extends Thread
    {
        ReaderThread()
        {
            setDaemon(true);
        }

        public void run()
        {
            DatagramPacket packet = new DatagramPacket(new byte[65536], 65536);

            while (true) {
                try {
                    sock.receive(packet);
                    handlePacket(packet);
                } catch (IOException ex) {
                    System.err.println("ex: "+ex);
                    continue;
                }
            }
        }

        void handleShortMessage(DatagramPacket packet, LCMDataInputStream ins) throws IOException
        {
            int msgSeqNumber = ins.readInt();
            String channel = ins.readStringZ();

            lcm.receiveMessage(channel, ins.getBuffer(), ins.getBufferOffset(), ins.available());
        }

        void handleFragment (DatagramPacket packet, LCMDataInputStream ins) throws IOException
        {
            int msgSeqNumber = ins.readInt();
            int msg_size = ins.readInt() & 0xffffffff;
            int fragment_offset = ins.readInt() & 0xffffffff;
            int fragment_id = ins.readShort() & 0xffff;
            int fragments_in_msg = ins.readShort() & 0xffff;

            // read entire packet payload
            byte payload[] = new byte[ins.available()];
            ins.readFully(payload);

            if (ins.available() > 0) {
                System.err.println("Unread data! "+ins.available());
            }

            int data_start = 0;
            int frag_size = payload.length;

            SocketAddress from = packet.getSocketAddress();
            FragmentBuffer fbuf = fragBufs.get(from);

            if (fbuf != null && ((fbuf.msgSeqNumber != msgSeqNumber) ||
                                 (fbuf.data_size != msg_size))) {
                fragBufs.remove(fbuf.from);
                fbuf = null;
            }

            if (null == fbuf && 0 == fragment_id) {

                // extract channel name
                int channel_len = 0;
                for (; channel_len < payload.length; channel_len++) {
                    if (0 == payload[channel_len]) {
                        break;
                    }
                }
                data_start = channel_len + 1;
                frag_size -= channel_len + 1;
                String channel = new String(payload, 0, channel_len, "US-ASCII");

                fbuf = new FragmentBuffer (from, channel, msgSeqNumber, msg_size, fragments_in_msg);

                fragBufs.put (fbuf.from, fbuf);
            }

            if (null == fbuf) {
                // TODO
                return;
            }

            if (fragment_offset + frag_size > fbuf.data_size) {
                System.err.println ("LC: dropping invalid fragment");
                fragBufs.remove (fbuf.from);
                return;
            }

            if (!fbuf.frag_received[fragment_id]) {
                fbuf.frag_received[fragment_id] = true;

                System.arraycopy(payload, data_start, fbuf.data, fragment_offset, frag_size);

                fbuf.fragments_remaining --;
            }

            if (0 == fbuf.fragments_remaining) {
                lcm.receiveMessage(fbuf.channel, fbuf.data, 0, fbuf.data_size);
                fragBufs.remove (fbuf.from);
            }
        }

        void handlePacket(DatagramPacket packet) throws IOException
        {
            LCMDataInputStream ins = new LCMDataInputStream(packet.getData(),
                                                            packet.getOffset(),
                                                            packet.getLength());

            int magic = ins.readInt();
            if (magic == MAGIC_SHORT) {
                handleShortMessage(packet, ins);
            } else if (magic == MAGIC_LONG) {
                handleFragment(packet, ins);
            } else {
                System.err.println("bad magic: " + Integer.toHexString(magic));
                return;
            }
        }
    }
}
