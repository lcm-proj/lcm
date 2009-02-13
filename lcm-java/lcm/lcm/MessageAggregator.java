package lcm.lcm;

import java.io.*;
import java.util.*;

/**
 * Accumulates received LCM messages in a queue.
 *
 * The aggregator has configurable limits.  If too many messages are aggregated
 * without having been retrieved, then older messages are discarded.
 */
public class MessageAggregator
    implements LCMSubscriber
{
    public class Message {
	final public byte[] data;
	final public String channel;
	public Message(String channel_, byte[] data_)
	{
	    data = data_;
	    channel = channel_;
	}
    }

    ArrayDeque<Message> messages = new ArrayDeque<Message>();
    long queue_data_size = 0;
    long max_queue_data_size = 100 * (1 << 20); // 100 megabytes
    int max_queue_length = Integer.MAX_VALUE;

    /**
     * Internal method, called by LCM when a message is received.
     */
    public synchronized void messageReceived(LCM lcm, String channel, 
	    DataInputStream dins)
    {
	try {
	    byte data[] = new byte[dins.available()];
	    dins.readFully(data);

	    messages.addLast(new Message(channel, data));
	    queue_data_size += data.length;

	    while(queue_data_size > max_queue_data_size ||
		  messages.size() > max_queue_length) {
		Message to_remove = messages.removeFirst();
		queue_data_size -= to_remove.data.length;
	    }

	    notify();
	} catch (IOException xcp) {}
    }

    public synchronized void setMaxBufferSize(long val)
    {
	max_queue_data_size = val;
    }

    public synchronized long getMaxBufferSize()
    {
	return max_queue_data_size;
    }

    public synchronized void setMaxMessages(int val) { max_queue_length = val; }
    public synchronized int getMaxMessage() { return max_queue_length; }

    /**
     * Attempt to retrieve the next received LCM message.
     * @param timeout_ms Max # of milliseconds to wait for a message.  If 0,
     * then don't wait.  If less than 0, then wait indefinitely.
     * @return a Message, or null if no message was received.  
     */
    public synchronized Message getNextMessage(long timeout_ms)
    {
	if(!messages.isEmpty()) {
	    Message m = messages.removeFirst();
	    queue_data_size -= m.data.length;
	    return m;
	}

	if(timeout_ms == 0)
	    return null;

	try {
	    if(timeout_ms > 0)
		wait(timeout_ms);
	    else
		wait();

	    if(!messages.isEmpty()) {
		Message m = messages.removeFirst();
		queue_data_size -= m.data.length;
		return m;
	    }
	} catch (InterruptedException xcp) { }

	return null;
    }

    /**
     * Retrieves the next message, waiting if necessary.
     */
    public synchronized Message getNextMessage()
    {
	return getNextMessage(-1);
    }

    /**
     * Returns the number of received messages waiting to be retrieved.
     */
    public synchronized int numMessagesAvailable()
    {
	return messages.size();
    }
}
