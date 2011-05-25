package lcm.lcm;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.nio.*;

import lcm.util.*;

/** Lightweight Communications and Marshalling Java implementation **/
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

    boolean closed = false;

    static LCM singleton;

    LCMDataOutputStream encodeBuffer = new LCMDataOutputStream(new byte[1024]);

    /** Create a new LCM object, connecting to one or more URLs. If
     * no URL is specified, the environment variable LCM_DEFAULT_URL is
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
            // Allow passing in NULL or the empty string to explicitly indicate
            // the default LCM URL.
            if(null == url || url.equals("")) {
                url = System.getenv("LCM_DEFAULT_URL");
                if (url == null)
                    url = "udpm://239.255.76.67:7667";
            }
        	
            URLParser up = new URLParser(url);
            String protocol = up.get("protocol");

            if (protocol.equals("udpm"))
                providers.add(new UDPMulticastProvider(this, up));
            else if (protocol.equals("tcpq"))
                providers.add(new TCPProvider(this, up));
            else if (protocol.equals("file"))
                providers.add(new LogFileProvider(this, up));
            else
                System.err.println("LCM: Unknown URL protocol: "+protocol);
        }
    }

    /** Retrieve a default instance of LCM using either the environment
     * variable LCM_DEFAULT_URL or the default. If an exception
     * occurs, System.exit(-1) is called.
     **/
    public static LCM getSingleton()
    {
        if (singleton == null) {
            try {
                singleton = new LCM();
            } catch (Exception ex) {
                System.err.println("LC singleton fail: "+ex);
                System.exit(-1);
                return null;
            }
        }

        return singleton;
    }

    /** Return the number of subscriptions. **/
    public int getNumSubscriptions()
    {
        if (this.closed) throw new IllegalStateException();
        return subscriptions.size();
    }

    /** Publish a string on a channel. This method does not use the
     * LCM type definitions and thus is not type safe. This method is
     * primarily provided for testing purposes and may be removed in
     * the future.
     **/
    public void publish(String channel, String s) throws IOException
    {
        if (this.closed) throw new IllegalStateException();
        s=s+"\0";
        byte[] b = s.getBytes();
        publish(channel, b, 0, b.length);
    }

    /** Publish an LCM-defined type on a channel. If more than one URL was
     * specified, the message will be sent on each.
     **/
    public synchronized void publish(String channel, LCMEncodable e)
    {
        if (this.closed) throw new IllegalStateException();

        try {
            encodeBuffer.reset();

            e.encode(encodeBuffer);

            publish(channel, encodeBuffer.getBuffer(), 0, encodeBuffer.size());
        } catch (IOException ex) {
            System.err.println("LC publish fail: "+ex);
        }
    }

    /** Publish raw data on a channel, bypassing the LCM type
     * specification. If more than one URL was specified when the LCM
     * object was created, the message will be sent on each.
     **/
    public synchronized void publish(String channel, byte[] data, int offset, int length)
        throws IOException
    {
        if (this.closed) throw new IllegalStateException();
        for (Provider p : providers)
            p.publish(channel, data, offset, length);
    }

    /** Subscribe to all channels whose name matches the regular
     * expression. Note that to subscribe to all channels, you must
     * specify ".*", not "*".
     **/
    public void subscribe(String regex, LCMSubscriber sub)
    {
        if (this.closed) throw new IllegalStateException();
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

    /** Remove this particular regex/subscriber pair (UNTESTED AND API
     * MAY CHANGE). If regex is null, all subscriptions for 'sub' are
     * cancelled. If subscriber is null, any previous subscriptions
     * matching the regular expression will be cancelled. If both
     * 'sub' and 'regex' are null, all subscriptions will be
     * cancelled.
     **/
    public void unsubscribe(String regex, LCMSubscriber sub) {
        if (this.closed) throw new IllegalStateException();

        synchronized(this) {
            for (Provider p : providers)
                p.unsubscribe (regex);
        }

        // TODO: providers don't seem to use anything beyond first channel

        synchronized(subscriptions) {

            // Find and remove subscriber from list
            for (Iterator<SubscriptionRecord> it = subscriptions.iterator(); it.hasNext(); ) {
                SubscriptionRecord sr = it.next();

                if ((sub == null || sr.lcsub == sub) &&
                    (regex == null || sr.regex.equals(regex))) {
                    it.remove();
                }
            }

            // Find and remove subscriber from map
            for (String channel : subscriptionsMap.keySet()) {
                for (Iterator<SubscriptionRecord> it = subscriptionsMap.get(channel).iterator(); it.hasNext(); ) {
                    SubscriptionRecord sr = it.next();

                    if ((sub == null || sr.lcsub == sub) &&
                        (regex == null || sr.regex.equals(regex))) {
                        it.remove();
                    }
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
        if (this.closed) throw new IllegalStateException();
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
                                           new LCMDataInputStream(data, offset, length));
            }
        }
    }

    /** A convenience function that subscribes to all LCM channels. **/
    public synchronized void subscribeAll(LCMSubscriber sub)
    {
        subscribe(".*", sub);
    }

    /** Call this function to release all resources used by the LCM instance.  After calling this
     * function, the LCM instance should consume no resources, and cannot be used to
     * receive or transmit messages.
     */
    public synchronized void close()
    {
        if (this.closed) throw new IllegalStateException();
        for (Provider p : providers) {
            p.close();
        }
        providers = null;
        this.closed = true;
    }

    ////////////////////////////////////////////////////////////////

    /** Minimalist test code.
     **/
    public static void main(String args[])
    {
        LCM lcm;

        try {
            lcm = new LCM();
        } catch (IOException ex) {
            System.err.println("ex: "+ex);
            return;
        }

        lcm.subscribeAll(new SimpleSubscriber());

        while (true) {
            try {
                Thread.sleep(1000);
                lcm.publish("TEST", "foobar");
            } catch (Exception ex) {
                System.err.println("ex: "+ex);
            }
        }
    }

    static class SimpleSubscriber implements LCMSubscriber
    {
        public void messageReceived(LCM lcm, String channel, LCMDataInputStream dins)
        {
            System.err.println("RECV: "+channel);
        }
    }
}
