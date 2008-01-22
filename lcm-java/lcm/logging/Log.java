package lcm.logging;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import lcm.util.*;

public class Log
{
    BufferedRandomAccessFile raf;
    static final int LOG_MAGIC = 0xEDA1DA01;
    String path;

    public static class Event
    {
	public long   utime;
	public long   eventNumber;

	public byte   data[];
	public String channel;
    }

    public Log(String path, String mode) throws IOException
    {
	this.path = path;
	raf = new BufferedRandomAccessFile(path, mode);
    }

    public String getPath()
    {
	return path;
    }

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

    public synchronized double getPercent() throws IOException
    {
	return raf.getFilePointer()/((double) raf.length());
    }

    public synchronized void seekPercent(double percent) throws IOException
    {
	raf.seek((long) (raf.length()*percent));
    }

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
    
    public synchronized void close() throws IOException
    {
	raf.close();
    }
}
