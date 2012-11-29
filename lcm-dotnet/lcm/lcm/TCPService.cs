using System;
using System.Collections.Generic;
using System.IO;
using System.Net.Sockets;
using System.Text.RegularExpressions;
using System.Threading;

namespace LCM.LCM
{
	public class TCPService
	{
        private TcpListener serverSocket;

        private Thread acceptThread;
        private List<ClientThread> clients = new List<ClientThread>();

        private int bytesCount = 0;
		
        /// <summary>
        /// Constructor of the TCP service object
        /// </summary>
        /// <param name="port">TCP port number</param>
		public TCPService(int port)
		{
			TcpListener tempTCPListener;
			tempTCPListener = new TcpListener(System.Net.Dns.GetHostAddresses(System.Net.Dns.GetHostName())[0], port);
			tempTCPListener.Start();
			serverSocket = tempTCPListener;
            // serverSocket.setReuseAddress(true);
            // serverSocket.setLoopbackMode(false); // true *disables* loopback
			
			acceptThread = new Thread(AcceptThreadRun);
            acceptThread.IsBackground = true;
			acceptThread.Start();
			
			long inittime = DateTime.Now.Ticks / 10000;
			long starttime = DateTime.Now.Ticks / 10000;

			while (true)
			{
				try
				{
					System.Threading.Thread.Sleep(1000);
				}
				catch (System.Threading.ThreadInterruptedException)
				{
				}

				long endtime = System.DateTime.Now.Ticks / 10000;
				double dt = (endtime - starttime) / 1000.0;
				starttime = endtime;

				Console.WriteLine("{0,10:N} : {1,10:N} kB/s, {2:D} clients", (endtime - inittime) / 1000.0, bytesCount / 1024.0 / dt, clients.Count);
				bytesCount = 0;
			}
		}
		
        /// <summary>
        /// Synchronously send a message to all clients
        /// </summary>
        /// <param name="channel">channel name</param>
        /// <param name="data">data to be relayed</param>
		public void Relay(byte[] channel, byte[] data)
        {
            string chanstr = System.Text.Encoding.GetEncoding("US-ASCII").GetString(channel);

			lock (clients)
			{
				foreach (ClientThread client in clients)
				{
					client.Send(chanstr, channel, data);
				}
			}
		}
		
		private void AcceptThreadRun()
		{
			while (true)
			{
				try
				{
					System.Net.Sockets.TcpClient clientSock = serverSocket.AcceptTcpClient();
					
					ClientThread client = new ClientThread(this, clientSock);
					client.Start();
					
					lock (clients)
					{
						clients.Add(client);
					}
				}
				catch (IOException)
				{
				}
			}
		}

        private class ClientThread
        {
            private TCPService service;
            private TcpClient sock;
            private BinaryReader ins;
            private BinaryWriter outs;
            private Thread thread;

            private class SubscriptionRecord
            {
                internal string regex;
                internal Regex pat;

                public SubscriptionRecord(string regex)
                {
                    this.regex = regex;
                    this.pat = new Regex(regex);
                }
            }

            List<SubscriptionRecord> subscriptions = new List<SubscriptionRecord>();

            public ClientThread(TCPService service, TcpClient sock)
            {
                this.service = service;
                this.sock = sock;

                ins = new BinaryReader(sock.GetStream());
                outs = new BinaryWriter(sock.GetStream());

                outs.Write(TCPProvider.MAGIC_SERVER);
                outs.Write(TCPProvider.VERSION);
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
                // read messages until something bad happens.
                try
                {
                    while (true)
                    {
                        int type = ins.ReadInt32();
                        if (type == TCPProvider.MESSAGE_TYPE_PUBLISH)
                        {
                            int channellen = ins.ReadInt32();
                            byte[] channel = new byte[channellen];
                            ReadInput(ins.BaseStream, channel, 0, channel.Length);

                            int datalen = ins.ReadInt32();
                            byte[] data = new byte[datalen];
                            ReadInput(ins.BaseStream, data, 0, data.Length);

                            service.Relay(channel, data);

                            service.bytesCount += channellen + datalen + 8;
                        }
                        else if (type == TCPProvider.MESSAGE_TYPE_SUBSCRIBE)
                        {
                            int channellen = ins.ReadInt32();
                            byte[] channel = new byte[channellen];
                            ReadInput(ins.BaseStream, channel, 0, channel.Length);

                            lock (subscriptions)
                            {
                                subscriptions.Add(new SubscriptionRecord(
                                    System.Text.Encoding.GetEncoding("US-ASCII").GetString(channel)));
                            }
                        }
                        else if (type == TCPProvider.MESSAGE_TYPE_UNSUBSCRIBE)
                        {
                            int channellen = ins.ReadInt32();
                            byte[] channel = new byte[channellen];
                            ReadInput(ins.BaseStream, channel, 0, channel.Length);

                            string re = System.Text.Encoding.GetEncoding("US-ASCII").GetString(channel);
                            lock (subscriptions)
                            {
                                for (int i = 0, n = subscriptions.Count; i < n; i++)
                                {
                                    if (subscriptions[i].pat.IsMatch(re))
                                    {
                                        subscriptions.RemoveAt(i);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                catch (IOException)
                {
                }

                // Something bad happened, close this connection.
                try
                {
                    sock.Close();
                }
                catch (IOException)
                {
                }

                lock (service.clients)
                {
                    service.clients.Remove(this);
                }
            }

            public virtual void Send(string chanstr, byte[] channel, byte[] data)
            {
                try
                {
                    lock (subscriptions)
                    {
                        foreach (SubscriptionRecord sr in subscriptions)
                        {
                            if (sr.pat.IsMatch(chanstr))
                            {
                                outs.Write(TCPProvider.MESSAGE_TYPE_PUBLISH);
                                outs.Write(channel.Length);
                                outs.Write(channel);
                                outs.Write(data.Length);
                                outs.Write(data);
                                outs.Flush();

                                return;
                            }
                        }
                    }
                }
                catch (IOException)
                {
                }
            }

            /******************************* Helper methods *******************************/

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