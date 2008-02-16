package lcm.lcm;

/** A provider implements a communications modality for LC.
    
    It should call LC.receiveMessage() upon receipt of a
    message. Publish() will be called. Publish must be thread-safe.

 **/
public interface Provider
{
    public void publish(String channel, byte data[], int offset, int len);
}
