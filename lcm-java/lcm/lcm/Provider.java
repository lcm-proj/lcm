package lcm.lcm;

/** A provider implements a communications modality for LC.
    
    It should call LC.receiveMessage() upon receipt of a
    message. Publish() will be called. Publish must be thread-safe.

    subscribe() will be called when a channel subscription has been made.
 **/
public interface Provider
{
    public void publish(String channel, byte data[], int offset, int len);

    /**
     * used to notify a Provider that a channel subscription has been made.
     *
     * Primary purpose is to allow Providers to allocate resources for
     * receiving messages
     */
    public void subscribe(String channel);
}
