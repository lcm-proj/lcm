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

    public LCM(Object... urls) throws IOException
    {
	if (urls.length==0) {
	    String env = System.getenv("LC_URL");
	    if (env == null)
		urls = new String[] {"udpm://"};
	    else
		urls = new String[] { env };
	}
	
	for (Object o : urls) {
	    String url = (String) o;

	    URLParser up = new URLParser(url);
	    String protocol = up.get("protocol");
	    
	    if (protocol.equals("udpm"))
		providers.add(new UDPMulticastProvider(this, url));
	    else if (protocol.equals("file"))
		providers.add(new LogFileProvider(this, url));
	    else
		System.out.println("LC: Unknown URL protocol: "+protocol);
	}
    }

    public static LCM getSingleton()
    {
	if (singleton == null) {
	    try {
		singleton = new LCM();
	    } catch (Exception ex) {
		System.out.println("LC singleton fail: "+ex);
		System.exit(0);
		return null;
	    }
	}

	return singleton;
    }

    public int getNumSubscriptions()
    {
	return subscriptions.size();
    }

    public void publish(String channel, String s) throws IOException
    {
	s=s+"\0";
	byte[] b = s.getBytes();
	publish(channel, b, 0, b.length);
    }

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

    public synchronized void publish(String channel, byte[] data, int offset, int length) 
	throws IOException
    {
	for (Provider p : providers) 
	    p.publish(channel, data, offset, length);
    }

    public void subscribe(String regex, LCMSubscriber sub)
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
    
    public synchronized void subscribeAll(LCMSubscriber sub)
    {
	subscribe(".*", sub);
    }

    ////////////////////////////////////////////////////////////////

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
