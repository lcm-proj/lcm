using System;
using System.Collections.Generic;

namespace LCM.LCM
{	
	/// <summary>
    /// Accumulates received LCM messages in a queue.
	/// <p>
	/// {@link LCM} normally delivers messages asynchronously by invoking the 
	/// {@link LCMSubscriber#messageReceived messageReceived} 
	/// method on a subscriber as soon as a message is received.  This class provides an 
	/// alternate way to receive messages by storing them in an internal queue, and then
	/// delivering them to synchronously to the user.
	/// <p>
	/// The aggregator has configurable limits.  If too many messages are aggregated
	/// without having been retrieved, then older messages are discarded.
	/// </summary>
	public class MessageAggregator : LCMSubscriber
    {
        /// <summary>
        /// A received message.
        /// </summary>
        public class Message
        {
            /// <summary>
            /// The raw data bytes of the message body.
            /// </summary>
            public byte[] Data;

            /// <summary>
            /// Channel on which the message was received.
            /// </summary>
            public string Channel;

            /// <summary>
            /// Constructor.
            /// </summary>
            /// <param name="channel">channel name</param>
            /// <param name="data">raw data</param>
            public Message(string channel, byte[] data)
            {
                Data = data;
                Channel = channel;
            }
        }

        private Queue<Message> messages = new Queue<Message>();
        private long queueDataSize = 0;
        private long maxQueueDataSize = 100 * (1 << 20); // 100 megabytes
        private int maxQueueLength = Int32.MaxValue;
		
		/// <summary>
        /// Retrieves and sets the maximum amount of memory that will be used to store messages.
		/// This is an alternative way to limit the messages stored by the
		/// aggregator.  Messages are discarded oldest-first to ensure that the
		/// total size of unretrieved messages stays under this limit.
		/// </summary>
		/// <param name="val">memory limit, in bytes.</param>
		public long MaxBufferSize
		{
			get { lock (this) { return maxQueueDataSize; } }
			set { lock (this) { maxQueueDataSize = value; } }
		}

		/// <summary>
        /// Retrieves and sets the maximum number of unretrieved message that will be queued up by the aggregator.
		/// Messages are discarded oldest-first to ensure that the number of unretrieved messages stays under this limit.
        /// </summary>
		public int MaxMessages
		{
			get { lock (this) { return maxQueueLength; } }
			set { lock (this) { maxQueueLength = value; } }
        }

        /// <summary>
        /// The number of received messages waiting to be retrieved.
        /// </summary>
        public int MessagesAvailable
        {
            get { lock (this) { return messages.Count; } }
        }

		/// <summary>
        /// Internal method, called by LCM when a message is received.
        /// </summary>
		public void MessageReceived(LCM lcm, string channel, LCMDataInputStream dins)
		{
			lock (this)
			{
				try
				{
					byte[] data = new byte[dins.Available];
					dins.ReadFully(data);
					
					messages.Enqueue(new Message(channel, data));
					queueDataSize += data.Length;
					
					while (queueDataSize > maxQueueDataSize || messages.Count > maxQueueLength)
					{
						Message toRemove = messages.Dequeue();
						queueDataSize -= toRemove.Data.Length;
					}
					
					System.Threading.Monitor.Pulse(this);
				}
				catch (System.IO.IOException)
				{
				}
			}
		}
		
		/// <summary>
        /// Attempt to retrieve the next received LCM message.
        /// </summary>
		/// <param name="timeout_ms">Max # of milliseconds to wait for a message.  If 0,
		/// then don't wait. If less than 0, then wait indefinitely.</param>
		/// <returns>a Message, or null if no message was received.</returns>
		public Message GetNextMessage(long timeoutMs)
		{
			lock (this)
			{
				if (messages.Count > 0)
				{
					Message m = messages.Dequeue();
					queueDataSize -= m.Data.Length;
					return m;
				}

                if (timeoutMs == 0)
                {
                    return null;
                }
    				
			    try
			    {
                    if (timeoutMs > 0)
                    {
                        System.Threading.Monitor.Wait(this, TimeSpan.FromMilliseconds(timeoutMs));
                    }
                    else
                    {
                        System.Threading.Monitor.Wait(this);
                    }

				    if (messages.Count > 0)
				    {
					    Message m = messages.Dequeue();
					    queueDataSize -= m.Data.Length;
					    return m;
				    }
			    }
			    catch (System.Threading.ThreadInterruptedException)
			    {
			    }
    			
			    return null;
            }
		}
		
		/// <summary>
        /// Retrieves the next message, waiting if necessary.
        /// </summary>
		public Message GetNextMessage()
		{
		    return GetNextMessage(-1);
		}
	}
}