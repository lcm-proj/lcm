package lcm.lc;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

/** Lightweight Communications implementation **/
public class LC
{
    MulticastSocket sock;

    static final String DEFAULT_ADDRESS = "239.255.76.67";
    static final int    DEFAULT_PORT    = 7667;
    static final int    DEFAULT_TTL     = 0;
    static final String LC_CONF_FILE    = "/etc/lc.conf";

    static final int    MAGIC_DEPRECATED = 0x4c433031; // ascii of "LC01"
    static final int    MAGIC_SHORT = 0x4c433032; // ascii of "LC02"
    static final int    MAGIC_LONG  = 0x4c433033; // ascii of "LC03"
    static final int    FRAGMENTATION_THRESHOLD = 64000;

    ReaderThread reader;

    InetAddress inetAddr;
    int         inetPort;
    int         ttl=2;
    int		msg_seqno=0;

    static class SubscriptionRecord
    {
	String  regex;
	Pattern pat;
	LCSubscriber lcsub;
    }

    // All subscriptions go here
    ArrayList<SubscriptionRecord> subscriptions = new ArrayList<SubscriptionRecord>();

    // For each channel name, a list of the subscriptions that match.
    // Note: this is populated when we first receive a channel
    // message-- we check all subscriptions to see if it matches.
    HashMap<String, ArrayList<SubscriptionRecord>> subscriptionsMap = new HashMap<String,ArrayList<SubscriptionRecord>>();

    HashMap<SocketAddress, FragmentBuffer> frag_bufs = new HashMap<SocketAddress, FragmentBuffer>();

    static 
    {
	System.setProperty("java.net.preferIPv4Stack", "true");
	System.out.println("LC: Disabling IPV6 support");
    }

    /** Create an LC instance using default options (or environment variable LC_MCADDR or config file if available.) **/
    public LC() throws IOException
    {
	Map<String, String> configFile = null;
	try { 
	    configFile = loadConfigFromFile(LC_CONF_FILE); 
	} catch (IOException e) {}

	String addr = System.getenv("LC_MCADDR");
	String ttl_string = System.getenv("LC_TTL");
	int ttl = -1;

	if ((addr == null || addr.length()==0) && configFile != null) {
	    addr = configFile.get("mc_addr");
	}
	if ((ttl_string == null || ttl_string.length() == 0) && configFile != null) {
	    ttl_string = configFile.get("ttl");
	}

	if (addr == null || addr.length()==0) {
	    System.out.println("LC multicast address not specified. Using default: "+DEFAULT_ADDRESS);
	    addr = DEFAULT_ADDRESS;
	}

	if (ttl_string == null || ttl_string.length() == 0) {
	    System.out.println("LC multicast TTL set to 0. Packets will not leave localhost.");
	    ttl = DEFAULT_TTL;
	} else {
	    try {
		ttl = Integer.parseInt(ttl_string);
	    } catch (NumberFormatException e) {
		ttl = DEFAULT_TTL;
	    }
	}

	int port = DEFAULT_PORT;

//	System.out.println("address: " + addr + " ttl: " + ttl);

	connect(addr, port, ttl);

	if (singleton == null)
	    singleton = this;
    }

    public LC(String addr, int port, int ttl) throws IOException
    {
	connect(addr, port, ttl);
    }
    
    static Map<String, String> loadConfigFromFile(String filename) 
	throws IOException
    {
	FileReader reader = new FileReader(filename);
	LineNumberReader linereader = new LineNumberReader(reader);

	Map<String, String>result = new Hashtable <String, String>();

	boolean have_group = false;
	while(true) {
	    String line = linereader.readLine();
	    if (null == line) break;
	    line = line.trim();

	    if (0 == line.length() || line.startsWith ("#")) continue;

	    if (line.equals("[lc]")) {
		have_group = true;
		continue;
	    } else if (!have_group) {
		throw new IOException ("Invalid LC config file\n");
	    }

	    String[] keyvalue = line.split("=", 2);

	    if (keyvalue.length < 2) {
		throw new IOException ("Invalid LC config file\n");
	    }

	    String key = keyvalue[0];
	    String value = keyvalue[1];

	    if (key.equals("mc_addr") || key.equals("ttl")) {
		result.put(key, value);
	    } else {
		throw new IOException ("Invalid LC config file\n");
	    }
	}
	return result;
    }

    static LC singleton;
    public static LC getSingleton()
    {
	try {
	    if (singleton == null)
		singleton = new LC();
	} catch (Exception ex) {
	    System.out.println("Couldn't create LC object");
	    System.exit(0);
	    return null;
	}

	return singleton;
    }

    public void stopReader()
    {
	reader.stop = true;
    }

    void connect(String addr, int port, int ttl) throws IOException
    {
	this.inetAddr = InetAddress.getByName(addr);
	this.inetPort = port;
	this.ttl = ttl;

	sock = new MulticastSocket(inetPort);

	sock.setReuseAddress(true);
	sock.setLoopbackMode(false); // true *disables* loopback
	sock.setTimeToLive(ttl);

	sock.joinGroup(inetAddr);

	reader = new ReaderThread();
	reader.start();
    }

    class FragmentBuffer
    {
	SocketAddress from = null;
	String channel = null;
	int msg_seqno = 0;
	int data_size = 0;
	int fragments_remaining = 0;
	byte[] data = null;

	FragmentBuffer (SocketAddress from, String channel, int msg_seqno, int data_size,
		int fragments_remaining)
	{
	    this.from = from;
	    this.channel = channel;
	    this.msg_seqno = msg_seqno;
	    this.data_size = data_size;
	    this.fragments_remaining = fragments_remaining;
	    this.data = new byte[data_size];
	}
    }

    class ReaderThread extends Thread
    {
	boolean stop = false;

	ReaderThread()
	{
	    setDaemon(true);
	}
	
	public void run()
	{
	    DatagramPacket packet = new DatagramPacket(new byte[65536], 65536);
	    
	    while (!stop) {
		try {
		    sock.receive(packet);
		    handle_packet(packet);
		} catch (IOException ex) {
		    System.out.println("ex: "+ex);
		    continue;
		}
	    }
	}

	/*
	boolean has_subscribers (String channel)
	{
	    return subscriptions.containsKey(channel) || snoopers.size() > 0;
	}
	*/

	void dispatch_message (String channel, byte[] data, int data_offset, int data_length)
	{
	    synchronized (subscriptions) {
		ArrayList<SubscriptionRecord> srecs = subscriptionsMap.get(channel);
		
		if (srecs == null) {
		    // must build this list!
		    srecs = new ArrayList<SubscriptionRecord>();
		    subscriptionsMap.put(channel, srecs);
		    
		    for (SubscriptionRecord srec : subscriptions) {
			if (srec.pat.matcher(channel).matches())
			    srecs.add(srec);
		    }
		}
		
		for (SubscriptionRecord srec : srecs) {
		    srec.lcsub.messageReceived(channel, 
					       new DataInputStream(new ByteArrayInputStream(data, data_offset, 
											    data_length)));
		}
	    }
	}

	void handle_deprecated (DataInputStream ins) throws IOException
	{
	    int channelSize  = ins.readShort()&0xffff;
	    int dataSize = ins.readShort()&0xffff;
	    
	    /////////////////////////////////////////
	    // read channel name
	    /////////////////////////////////////////
	    byte channelbytes[] = new byte[channelSize];
	    ins.read(channelbytes);
	    String channel = new String(channelbytes);
	    System.out.println("deprecated magic: LC01 channel discarded:"+ channel + " recompile this source");
	}


	void handle_short_message (DatagramPacket packet, DataInputStream ins) throws IOException
	{
	    int msg_seqno = ins.readInt();

	    /////////////////////////////////////////
	    // read channel name and data
	    /////////////////////////////////////////
	    byte channel_and_data[] = new byte[ins.available()];
	    ins.read(channel_and_data);

	    // extract channel name
	    int channel_len = 0;
	    for (; channel_len < channel_and_data.length; channel_len++) {
		if (0 == channel_and_data[channel_len]) {
		    break;
		}
	    }
	    int data_offset = channel_len + 1;

	    String channel = new String(channel_and_data, 0, channel_len, "US-ASCII");

	    int data_length = channel_and_data.length - (channel_len + 1);

	    if (ins.available() > 0) {
		System.out.println("Unread data! "+ins.available());
	    }

	    dispatch_message (channel, channel_and_data, data_offset, data_length);
	}

	void add_fragment_buffer (FragmentBuffer fbuf)
	{
	    frag_bufs.put (fbuf.from, fbuf);
	    // TODO
	}

	void destroy_fragment_buffer (FragmentBuffer fbuf)
	{
	    frag_bufs.remove (fbuf.from);
	    // TODO
	}

	void handle_fragment (DatagramPacket packet, DataInputStream ins) throws IOException
	{
	    int msg_seqno = ins.readInt();
	    int msg_size = ins.readInt() & 0xffffffff;
	    int fragment_offset = ins.readInt() & 0xffffffff;
	    int fragment_id = ins.readShort() & 0xffff;
	    int fragments_in_msg = ins.readShort() & 0xffff;

	    // read entire packet payload
	    byte payload[] = new byte[ins.available()];
	    ins.read(payload);

	    if (ins.available() > 0) {
		System.out.println("Unread data! "+ins.available());
	    }

	    int data_start = 0;
	    int frag_size = payload.length;

	    SocketAddress from = packet.getSocketAddress();
	    FragmentBuffer fbuf = frag_bufs.get(from);

	    if (fbuf != null && ((fbuf.msg_seqno != msg_seqno) ||
			(fbuf.data_size != msg_size))) {
		destroy_fragment_buffer(fbuf);
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

		// discard if no subscribers
		/*		if (! has_subscribers (channel)) 
		    return;
		*/

		fbuf = new FragmentBuffer (from, channel, msg_seqno, msg_size, fragments_in_msg);

		add_fragment_buffer (fbuf);
	    }

	    if (null == fbuf) {
		// TODO
		return;
	    }

	    if (fragment_offset + frag_size > fbuf.data_size) {
		System.out.println ("LC: dropping invalid fragment");
		destroy_fragment_buffer (fbuf);
		return;
	    }

	    System.arraycopy(payload, data_start, fbuf.data, fragment_offset, frag_size);
	    fbuf.fragments_remaining --;

	    if (0 == fbuf.fragments_remaining) {
		dispatch_message (fbuf.channel, fbuf.data, 0, fbuf.data_size);
		destroy_fragment_buffer (fbuf);
	    }
	}

	void handle_packet(DatagramPacket packet) throws IOException
	{
	    DataInputStream ins = new DataInputStream(new ByteArrayInputStream(packet.getData(), 
									       packet.getOffset(), 
									       packet.getLength()));
	    
	    int magic = ins.readInt();
	    if (magic == MAGIC_SHORT) {
		handle_short_message (packet, ins);
	    } else if (magic == MAGIC_LONG) {
		handle_fragment (packet, ins);
	    } else if (magic == MAGIC_DEPRECATED) {
		handle_deprecated(ins);
		return;
	    } else {
		System.out.println("bad magic: " + Integer.toHexString(magic));
		return;
	    }
	   
	}
    }

    public void publish(String channel, String s) throws IOException
    {
	s=s+"\0";
	byte[] b = s.getBytes();
	publish(channel, b, 0, b.length);
    }

    public void publish(String channel, LCEncodable e)
    {
	try {
	    ByteArrayOutputStream bouts = new ByteArrayOutputStream(256);
	    DataOutputStream outs = new DataOutputStream(bouts);
	    
	    e.encode(outs);
	    
	    byte[] b = bouts.toByteArray();
	    
	    publish(channel, b, 0, b.length);
	} catch (IOException ex) {
	    System.out.println("LC publish fail: "+ex);
	}
    }

    public synchronized void publish(String channel, byte[] data, int offset, int length) 
	throws IOException
    {
	byte[] channel_bytes = channel.getBytes("US-ASCII");
	int payload_size = channel_bytes.length + length;

	if (payload_size <= FRAGMENTATION_THRESHOLD) {
	    ByteArrayOutputStream bouts = new ByteArrayOutputStream(length + channel.length() + 32);
	    DataOutputStream outs = new DataOutputStream(bouts);

	    outs.writeInt(MAGIC_SHORT);
	    outs.writeInt(this.msg_seqno);

	    outs.write(channel_bytes, 0, channel_bytes.length);
	    outs.writeByte(0);

	    outs.write(data, offset, length);

	    byte[] b = bouts.toByteArray();
	    sock.send(new DatagramPacket(b, 0, b.length, inetAddr, inetPort));
	} else {
	    int nfragments = payload_size / FRAGMENTATION_THRESHOLD;
	    if (payload_size % FRAGMENTATION_THRESHOLD > 0) nfragments++;

	    if (nfragments > 65535) {
		System.out.println("LC error: too much data for a single message");
		return;
	    }

	    // first fragment is special.  insert channel before data
	    ByteArrayOutputStream bouts = new ByteArrayOutputStream(10 + FRAGMENTATION_THRESHOLD);
	    DataOutputStream outs = new DataOutputStream(bouts);

	    int fragment_offset = 0;
	    int frag_no = 0;

	    outs.writeInt(MAGIC_LONG);
	    outs.writeInt(this.msg_seqno);
	    outs.writeInt(length);
	    outs.writeInt(fragment_offset);
	    outs.writeShort(frag_no);
	    outs.writeShort(nfragments);
	    outs.write(channel_bytes, 0, channel_bytes.length);
	    outs.writeByte(0);
	    int firstfrag_datasize = FRAGMENTATION_THRESHOLD - 
		(channel_bytes.length + 1);
//	    System.out.println("" + this.msg_seqno + " " + channel + " " + offset + " " + firstfrag_datasize + " / " + length);
	    outs.write(data, offset, firstfrag_datasize);

	    byte[] b = bouts.toByteArray();
	    sock.send(new DatagramPacket(b, 0, b.length, inetAddr, inetPort));

	    fragment_offset += firstfrag_datasize;

	    for (frag_no=1; frag_no<nfragments; frag_no++) {
		bouts = new ByteArrayOutputStream(10 + FRAGMENTATION_THRESHOLD);
		outs = new DataOutputStream(bouts);

		outs.writeInt(MAGIC_LONG);
		outs.writeInt(this.msg_seqno);
		outs.writeInt(length);
		outs.writeInt(fragment_offset);
		outs.writeShort(frag_no);
		outs.writeShort(nfragments);
		int fraglen = java.lang.Math.min(FRAGMENTATION_THRESHOLD, length - fragment_offset);
		outs.write(data, offset+fragment_offset, fraglen);

		b = bouts.toByteArray();
		sock.send(new DatagramPacket(b, 0, b.length, inetAddr, inetPort));
//		System.out.println ("   " + fragment_offset + ", " + fraglen);

		fragment_offset += fraglen;
	    }
//	    System.out.println ("transmitted " + fragment_offset);
	}

	this.msg_seqno++;

    }

    public void subscribe(String regex, LCSubscriber sub)
    {
	SubscriptionRecord srec = new SubscriptionRecord();
	srec.regex = regex;
	srec.pat = Pattern.compile(regex);
	srec.lcsub = sub;

	synchronized(subscriptions) {
	    subscriptions.add(srec);
	    
	    for (String channel : subscriptionsMap.keySet()) {
		if (srec.pat.matcher(channel).matches()) {
		    ArrayList<SubscriptionRecord> subs = subscriptionsMap.get(channel);
		    subs.add(srec);
		}
	    }
	}
    }

    public synchronized void subscribeAll(LCSubscriber sub)
    {
	subscribe(".*", sub);
    }

    public static void main(String args[])
    {
	LC lc;

	try {
	    lc = new LC();
	} catch (IOException ex) {
	    System.out.println("ex: "+ex);
	    return;
	}

	lc.subscribeAll(new SimpleSubscriber());

	while (true) {				
	    try {
		Thread.sleep(1000);
		lc.publish("TEST", "foobar");
	    } catch (Exception ex) {
		System.out.println("ex: "+ex);
	    }
	}
    }

    static class SimpleSubscriber implements LCSubscriber
    {
	public void messageReceived(String channel, DataInputStream dins)
	{
	    System.out.println("RECV: "+channel);
	}
    }
}
