package lcm.logging;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import lcm.util.*;
import lcm.lcm.*;

/**
 * A class for reading and writing LCM log files.
 */
public class Log
{
    BufferedRandomAccessFile raf;

    static final int LOG_MAGIC = 0xEDA1DA01;
    String path;

    /** Used to count the number of messages written so far. **/
    long numMessagesWritten = 0;

    /**
     * Represents a single received LCM message.
     */
    public static class Event
    {
        /**
         * Time of message receipt, represented in microseconds since 00:00:00
         * UTC January 1, 1970.
         */
        public long   utime;
        /**
         * Event number assigned to the message in the log file.
         */
        public long   eventNumber;

        /**
         * Raw data bytes of the message body.
         */
        public byte   data[];

        /**
         * Channel on which the message was received.
         */
        public String channel;
    }

    /**
     * Opens a log file for reading or writing.
     *
     * @param path the filename to open
     * @param mode Specifies the access mode, must be one of "r", "rw",
     * "rws", or "rwd".  See {@link java.io.RandomAccessFile#RandomAccessFile RandomAccessFile} for more detail.
     */
    public Log(String path, String mode) throws IOException
    {
        this.path = path;
        raf = new BufferedRandomAccessFile(path, mode);
        //raf = new RandomAccessFile(path, mode);
    }

    /**
     * Retrieves the path to the log file.
     * @return the path to the log file
     */
    public String getPath()
    {
        return path;
    }

    /**
     * Flush any unwritten data to the underlying file descriptor.
     **/
    public void flush() throws IOException
    {
        raf.flush();
    }

    /**
     * Reads the next event in the log file
     *
     * @throws java.io.EOFException if the end of the file has been reached.
     */
    public synchronized Event readNext() throws IOException
    {
        int magic = 0;
        Event e = new Event();
        int channellen = 0, datalen = 0;

        while (true)
	    {
            int v = raf.readByte()&0xff;

            magic = (magic<<8) | v;

            if (magic != LOG_MAGIC)
                continue;

            e.eventNumber = raf.readLong();
            e.utime       = raf.readLong();

            channellen    = raf.readInt();
            datalen       = raf.readInt();

            if (channellen <= 0 || datalen <= 0 || channellen >= 256 || datalen >= 16*1024*1024) {
                System.out.printf("Bad log event eventnumber = 0x%08x utime = 0x%08x channellen = 0x%08x datalen=0x%08x\n",
                                  e.eventNumber, e.utime, channellen, datalen);
                continue;
            }
            break;
	    }

        byte bchannel[] = new byte[channellen];
        e.data = new byte[datalen];

        raf.readFully(bchannel);
        e.channel = new String(bchannel);
        raf.readFully(e.data);

        return e;
    }

    public synchronized double getPositionFraction() throws IOException
    {
        return raf.getFilePointer()/((double) raf.length());
    }

    /**
     * Seek to a position in the log file, specified by a fraction.
     *
     * @param frac a number in the range [0, 1)
     */
    public synchronized void seekPositionFraction(double frac) throws IOException
    {
        raf.seek((long) (raf.length()*frac));
    }

    /**
     * Writes an event to the log file. The user is responsible for
     * filling in the eventNumber field, which should be sequentially
     * increasing integers starting with 0.
     */
    public synchronized void write(Event e) throws IOException
    {
        byte[] channelb = e.channel.getBytes();

        raf.writeInt(LOG_MAGIC);
        raf.writeLong(e.eventNumber);
        raf.writeLong(e.utime);
        raf.writeInt(channelb.length);
        raf.writeInt(e.data.length);

        raf.write(channelb, 0, channelb.length);
        raf.write(e.data, 0, e.data.length);
    }

    /** A convenience method for write. It internally manages the
     * eventNumber field, and so calls to this method should not be
     * mixed with calls to the other write methods. **/
    public synchronized void write(long utime, String channel, LCMEncodable msg) throws IOException
    {
        Log.Event le = new Log.Event();
        le.utime = utime;
        le.channel = channel;
        LCMDataOutputStream outs = new LCMDataOutputStream();
        msg.encode(outs);
        le.data = outs.toByteArray();
        le.eventNumber = numMessagesWritten;

        write(le);
        numMessagesWritten++;
    }

    /**
     * Closes the log file and releases and system resources used by it.
     */
    public synchronized void close() throws IOException
    {
        raf.close();
    }
}
