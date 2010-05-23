package lcm.lcm;

/** A provider implements a communications modality for LCM. (I.e., a
    URL handler.)

    The provider should call LCM.receiveMessage() upon receipt of a
    message. LCM.receiveMessage() is thread-safe and can be called from
    any thread.

**/
public interface Provider
{
    /**
       Publish() will be called when an application sends a message, and
       could be called on an arbitrary thread.
    **/
    public void publish(String channel, byte data[], int offset, int len);

    /**
       subscribe() will be called when a channel subscription has been
       made. Providers that do not use a broadcast communications
       mechanism could use this notification to establish communications
       with additional hosts.
    **/
    public void subscribe(String channel);

    /**
       unsubscribe() will be called when a channel subscription is cancelled.
    **/
    public void unsubscribe(String channel);

    /**
     * close() will be called when the application no longer requires the provider
     * and wishes to free the resources used by the provider.  For example,
     * file handles and network sockets should be closed.  After this method is
     * called, the results of any calls to publish or subscribe are undefined.
     */
    public void close();
}
