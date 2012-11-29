using System;
using System.Collections.Generic;
using System.IO;
using System.Net.Sockets;
using System.Text.RegularExpressions;
using System.Threading;

namespace LCM.LCM
{	
	/// <summary>
    /// LCM provider for the tcp: URL. All messages are sent to a central
	/// "hub" process (that must be started separately), which will relay
	/// the messages to all other processes. TCPService is an
    /// implementation of the hub process.
    /// 
    /// The tcpq:// protocol is NOT suitable for real-time or high-bandwidth
    /// traffic.  It is specifically designed for playing back a log file in a
    /// post-processing context (i.e., play back the log as fast as possible, but
    /// without dropping anything).
    /// 
    /// The .NET implementation is functionally equal to the Java version.
    /// </summary>
	public class TCPProvider : Provider
	{
        /* Variables */
        private LCM lcm;

        private const int DEFAULT_PORT = 7700;
        private const string DEFAULT_NETWORK = "127.0.0.1:7700";
        private System.Net.IPAddress inetAddr;
        private int inetPort;
		
		private ReaderThread reader;
		
		internal const int MAGIC_SERVER = 0x287617fa; // first word sent by server
        internal const int MAGIC_CLIENT = 0x287617fb; // first word sent by client
        internal const int VERSION = 0x0100; // what version do we implement?
        internal const int MESSAGE_TYPE_PUBLISH = 1;
        internal const int MESSAGE_TYPE_SUBSCRIBE = 2;
        internal const int MESSAGE_TYPE_UNSUBSCRIBE = 3;

        /* Properties */
        public System.Net.IPAddress InetAddr
        {
            get { return inetAddr; }
        }

        public int InetPort
        {
            get { return inetPort; }
        }
		
        /* Methods */
        /// <summary>
        /// TCP provider constructor
        /// </summary>
        /// <param name="lcm">LCM object</param>
        /// <param name="up">URL parser object</param>
		public TCPProvider(LCM lcm, URLParser up)
		{
			this.lcm = lcm;
			
			string[] addrport = up.Get("network", DEFAULT_NETWORK).Split(':');
			if (addrport.Length == 1)
			{
                inetAddr = System.Net.Dns.GetHostAddresses(addrport[0])[0];
				inetPort = 7700;
			}
			else if (addrport.Length == 2)
			{
                inetAddr = System.Net.Dns.GetHostAddresses(addrport[0])[0];
				inetPort = Int32.Parse(addrport[1]);
			}
			else
			{
				Console.Error.WriteLine("TCPProvider: Don't know how to parse " + up.Get("network", DEFAULT_NETWORK));
				Environment.Exit(-1);
			}
			
			reader = new ReaderThread(this);
			reader.Start();
		}

        /// <summary>
        /// Publish a message synchronously. However, if the server is not available, it will return immediately.
        /// </summary>
        /// <param name="channel">channel name</param>
        /// <param name="data">data byte array</param>
        /// <param name="offset">offset of the data to write</param>
        /// <param name="length">length of the data to write</param>
		public virtual void Publish(string channel, byte[] data, int offset, int length)
		{
			lock (this)
			{
				try
				{
					PublishEx(channel, data, offset, length);
				}
				catch (System.Exception ex)
				{
					Console.Error.WriteLine("Ex: " + ex);
				}
			}
		}

        /// <summary>
        /// Try to send message on socket. If the socket is not
        /// connected, we'll simply fail. The readerthread is
        /// responsible for maintaining a connection to the hub.
        /// </summary>
        /// <param name="b">byte array to write</param>
        private void SockWriteAndFlush(byte[] b)
        {
            Stream sockOuts = reader.OutputStream;
            if (sockOuts != null)
            {
                try
                {
                    sockOuts.Write(b, 0, b.Length);
                    sockOuts.Flush();
                }
                catch
                {
                }
            }
        }

        /// <summary>
        /// Subscribe method
        /// </summary>
        /// <param name="channel">channel name</param>
		public virtual void Subscribe(string channel)
		{
			lock (this)
            {
                byte[] channelBytes;
                try
                {
                    channelBytes = System.Text.Encoding.GetEncoding("US-ASCII").GetBytes(channel);
                }
                catch (Exception)
                {
                    Console.Error.WriteLine("LCM.TCPProvider: Bad channel name " + channel);
                    return;
                }

                try
                {
                    MemoryStream bouts = new MemoryStream(channel.Length + 8);
                    BinaryWriter outs = new BinaryWriter(bouts);

                    outs.Write(MESSAGE_TYPE_SUBSCRIBE);
                    outs.Write(channelBytes.Length);
                    outs.Write(channelBytes, 0, channelBytes.Length);

                    SockWriteAndFlush(bouts.ToArray());
                }
                catch (IOException)
                {
                }
			}
		}
		
        /// <summary>
        /// Unsubscribe method
        /// </summary>
        /// <param name="channel">channel name</param>
        public void Unsubscribe(string channel)
        {
            lock (this)
            {
                byte[] channelBytes;
                try
                {
                    channelBytes = System.Text.Encoding.GetEncoding("US-ASCII").GetBytes(channel);
                }
                catch (Exception)
                {
                    Console.Error.WriteLine("LCM.TCPProvider: Bad channel name " + channel);
                    return;
                }

                try
                {
                    MemoryStream bouts = new MemoryStream(channel.Length + 8);
                    BinaryWriter outs = new BinaryWriter(bouts);

                    outs.Write(MESSAGE_TYPE_UNSUBSCRIBE);
                    outs.Write(channelBytes.Length);
                    outs.Write(channelBytes, 0, channelBytes.Length);

                    SockWriteAndFlush(bouts.ToArray());
                }
                catch (IOException)
                {
                }
            }
        }

        /// <summary>
        /// Close the TCP provider
        /// </summary>
        public virtual void Close()
		{
			lock (this)
			{
				if (reader != null)
				{
					reader.Close();

					try
					{
						reader.Join();
					}
					catch (System.Threading.ThreadInterruptedException)
					{
					}
				}
				
				reader = null;
			}
		}
		
		private static void SafeSleep(int ms)
		{
			try
			{
				System.Threading.Thread.Sleep(ms);
			}
			catch (System.Threading.ThreadInterruptedException)
			{
			}
		}

        private void PublishEx(string channel, byte[] data, int offset, int length)
		{
			byte[] channelBytes = System.Text.Encoding.GetEncoding("US-ASCII").GetBytes(channel);
			
			int payload_size = channelBytes.Length + length;
			
			MemoryStream bouts = new MemoryStream(length + channel.Length + 32);
			BinaryWriter outs = new BinaryWriter(bouts);
			
			outs.Write(MESSAGE_TYPE_PUBLISH);
			
			outs.Write(channelBytes.Length);
			outs.Write(channelBytes, 0, channelBytes.Length);
			
			outs.Write(length);
			outs.Write(data, offset, length);
			
			SockWriteAndFlush(bouts.ToArray());
		}

        private class ReaderThread
		{
            private TcpClient sock;
            private BinaryReader ins;
            private Stream outs;
            private bool exit = false;
            private int serverVersion;
            private Thread thread;
            private TCPProvider provider;

            public Stream OutputStream
			{
				get { return outs; }
			}

            public ReaderThread(TCPProvider provider)
            {
                this.provider = provider;
            }

            public void Start()
            {
                if (thread == null)
                {
                    thread = new Thread(Run);
                    thread.IsBackground = true;
                    thread.Start();
                }
            }
			
			public void Run()
			{
				while (!exit)
				{
					// reconnect
					try
					{
                        sock = new System.Net.Sockets.TcpClient(provider.inetAddr.ToString(), provider.inetPort);
						Stream _outs = sock.GetStream();
						BinaryWriter _douts = new BinaryWriter(_outs);
						_douts.Write(TCPProvider.MAGIC_CLIENT);
						_douts.Write(TCPProvider.VERSION);
						_douts.Flush();
						outs = _outs;
						ins = new BinaryReader(new BufferedStream(sock.GetStream()));
						
						int magic = ins.ReadInt32();
						if (magic != TCPProvider.MAGIC_SERVER)
						{
							sock.Close();
							continue;
						}
						
						serverVersion = ins.ReadInt32();
					}
					catch (IOException)
					{
                        Console.Error.WriteLine("LCM.TCPProvider: Unable to connect to " + provider.inetAddr + ":" + provider.inetPort);
						TCPProvider.SafeSleep(500);
						
						// try connecting again.
						continue;
					}
					
					// read loop
					try
					{
						while (!exit)
						{
							int type = ins.ReadInt32();

							int channelLen = ins.ReadInt32();
							byte[] channel = new byte[channelLen];
							ReadInput(ins.BaseStream, channel, 0, channel.Length);

							int dataLen = ins.ReadInt32();
							byte[] data = new byte[dataLen];
							ReadInput(ins.BaseStream, data, 0, data.Length);

                            provider.lcm.ReceiveMessage(System.Text.Encoding.GetEncoding("US-ASCII").GetString(channel), data, 0, data.Length);
						}
					}
					catch (IOException)
					{
						// exit read loop so we'll create a new connection.
					}
				}
			}
			
			public void Close()
			{
				try
				{
					sock.Close();
				}
				catch (IOException)
				{
				}
				
				exit = true;
			}

            public void Join()
            {
                thread.Join();
            }

            // Helper
            /// <summary>Reads a number of characters from the current source Stream and writes the data to the target array at the specified index.</summary>
            /// <param name="sourceStream">The source Stream to read from.</param>
            /// <param name="target">Contains the array of characteres read from the source Stream.</param>
            /// <param name="start">The starting index of the target array.</param>
            /// <param name="count">The maximum number of characters to read from the source Stream.</param>
            /// <returns>The number of characters read. The number will be less than or equal to count depending on the data available in the source Stream. Returns -1 if the end of the stream is reached.</returns>
            private static int ReadInput(Stream sourceStream, byte[] target, int start, int count)
            {
                // Returns 0 bytes if not enough space in target
                if (target.Length == 0)
                {
                    return 0;
                }

                byte[] receiver = new byte[target.Length];
                int bytesRead = sourceStream.Read(receiver, start, count);

                // Returns -1 if EOF
                if (bytesRead == 0)
                {
                    return -1;
                }

                for (int i = start; i < start + bytesRead; i++)
                {
                    target[i] = (byte) receiver[i];
                }

                return bytesRead;
            }
		}
	}
}