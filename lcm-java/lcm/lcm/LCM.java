package lcm.lcm;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.nio.*;

/** Lightweight Communications implementation **/
public class LCM
{
    static class SubscriptionRecord
    {
	String  regex;
	Pattern pat;
	LCMSubscriber lcsub;
    }

    ArrayList<SubscriptionRecord> subscriptions = new ArrayList<SubscriptionRecord>();
    ArrayList<Provider> providers = new ArrayList<Provider>();

    HashMap<String,ArrayList<SubscriptionRecord>> subscriptionsMap = new HashMap<String,ArrayList<SubscriptionRecord>>();

    static LCM singleton;

    /** Create a new LCM object, subscribing to one or more URLs. If
     * no URL is specified, the system variable LCM_DEFAULT_URL is
     * used. If that environment variable is not defined, then the
     * default URL is used.
     **/
    public LCM(String... urls) throws IOException
    {
	if (urls.length==0) {
	    String env = System.getenv("LCM_DEFAULT_URL");
	    if (env == null)
		urls = new String[] {"udpm://239.255.76.67:7667"};
	    else
		urls = new String[] { env };
	}
	
	for (String url : urls) {
	    URLParser up = new URLParser(url);
	    String protocol = up.get("protocol");
	    
	    if (protocol.equals("udpm"))
		providers.add(new UDPMulticastProvider(this, up));
	    else if (protocol.equals("file"))
		providers.add(new LogFileProvider(this, up));
	    else
		System.out.println("LCM: Unknown URL protocol: "+protocol);
	}
    }

    /** Retrieve a default instance of LCM using either the system
     * variable LCM_DEFAULT_URL or the default. If an exception
     * occurs, System.exit(-1) is called. 
     **/
    public static LCM getSingleton()
    {
	if (singleton == null) {
	    try {
		singleton = new LCM();
	    } catch (Exception ex) {
		System.out.println("LC singleton fail: "+ex);
		System.exit(-1);
		return null;
	    }
	}

	return singleton;
    }

    /** Return the number of subscriptions. **/
    public int getNumSubscriptions()
    {
	return subscriptions.size();
    }

    /** Publish a string on a channel. This method does not use the
     * LCM type definitions and thus is not type safe. This method is
     * primarily provided for testing purposes and may be removed in
     * the future.
     **/
    public void publish(String channel, String s) throws IOException
    {
	s=s+"\0";
	byte[] b = s.getBytes();
	publish(channel, b, 0, b.length);
    }

    /** Publish an LCM-defined type on a channel. If more than one URL was
     * specified, the message will be sent on each.
     **/
    public void publish(String channel, LCMEncodable e)
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

    /** Publish raw data on a channel, bypassing the LCM type
     * specification. If more than one URL was specified when the LCM
     * object was created, the message will be sent on each.
     **/
    public synchronized void publish(String channel, byte[] data, int offset, int length) 
	throws IOException
    {
	for (Provider p : providers) 
	    p.publish(channel, data, offset, length);
    }

    /** Subscribe to all channels whose name matches the regular
     * expression. Note that to subscribe to all channels, you must
     * specify ".*", not "*". 
     **/
    public void subscribe(String regex, LCMSubscriber sub)
    {
	SubscriptionRecord srec = new SubscriptionRecord();
	srec.regex = regex;
	srec.pat = Pattern.compile(regex);
	srec.lcsub = sub;

	synchronized(this) {
	    for (Provider p : providers)
		p.subscribe (regex);
	}

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

    /** Not for use by end users. Provider back ends call this method
     * when they receive a message. The subscribers that match the
     * channel name are synchronously notified. 
     **/
    public void receiveMessage(String channel, byte data[], int offset, int length)
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
		srec.lcsub.messageReceived(this,
					   channel, 
					   new DataInputStream(new ByteArrayInputStream(data, 
											offset, 
											length)));
	    }
	}
    }
    
    /** A convenience function that subscribes to all LCM channels. **/
    public synchronized void subscribeAll(LCMSubscriber sub)
    {
	subscribe(".*", sub);
    }

    ////////////////////////////////////////////////////////////////

    /** Minimalist test code. **/
    public static void main(String args[])
    {
	LCM lcm;

	try {
	    lcm = new LCM();
	} catch (IOException ex) {
	    System.out.println("ex: "+ex);
	    return;
	}

	lcm.subscribeAll(new SimpleSubscriber());

	while (true) {				
	    try {
		Thread.sleep(1000);
		lcm.publish("TEST", "foobar");
	    } catch (Exception ex) {
		System.out.println("ex: "+ex);
	    }
	}
    }

    static class SimpleSubscriber implements LCMSubscriber
    {
	public void messageReceived(LCM lcm, String channel, DataInputStream dins)
	{
	    System.out.println("RECV: "+channel);
	}
    }
}
