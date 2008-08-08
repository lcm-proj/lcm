package lcm.lcm;

/** A provider implements a communications modality for LC. (I.e., a
    URL handler.)
    
    The provider should call LC.receiveMessage() upon receipt of a
    message. LC.receiveMessage() is thread-safe and can be called from
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
}
