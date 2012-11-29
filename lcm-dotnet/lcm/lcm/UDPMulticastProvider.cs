using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using System.Text.RegularExpressions;
using System.Threading;

namespace LCM.LCM
{
	/// <summary>
    /// LCM provider for the udpm: URL. All messages are broadcast over a
	/// pre-arranged UDP multicast address. Subscription operations are a
	/// no-op, since all messages are always broadcast.
	/// 
	/// This mechanism is very simple, low-latency, and efficient due to
	/// not having to transmit messages more than once when there are
	/// multiple subscribers. Since it uses UDP, it is lossy.
	/// </summary>
	public class UDPMulticastProvider : Provider
    {
        /* Variables */
		private UdpClient sock;

        private const string DEFAULT_NETWORK = "239.255.76.67:7667";
        private const int DEFAULT_TTL = 0;

        private const int MAGIC_SHORT = 0x4c433032; // ascii of "LC02"
        private const int MAGIC_LONG = 0x4c433033; // ascii of "LC03"
        private const int FRAGMENTATION_THRESHOLD = 64000;

        private Thread reader;

        private int msgSeqNumber = 0;

        private Dictionary<SocketAddress, FragmentBuffer> fragBufs = new Dictionary<SocketAddress, FragmentBuffer>();

        private LCM lcm;

        private IPAddress inetAddr;
        private int inetPort;
        private IPEndPoint inetEP;

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
        /// UDP multicast provider constructor
        /// </summary>
        /// <param name="lcm">LCM object</param>
        /// <param name="up">URL parser object</param>
		public UDPMulticastProvider(LCM lcm, URLParser up)
		{
			this.lcm = lcm;
			
			string[] addrport = up.Get("network", DEFAULT_NETWORK).Split(':');
			
			inetAddr = Dns.GetHostAddresses(addrport[0])[0];
			inetPort = Int32.Parse(addrport[1]);
            inetEP = new IPEndPoint(inetAddr, inetPort);

            sock = new UdpClient();
            sock.MulticastLoopback = true;
            sock.ExclusiveAddressUse = false;
            sock.Client.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.ReuseAddress, 1);
            sock.Client.Bind(new IPEndPoint(IPAddress.Any, inetPort));
			
			int ttl = up.Get("ttl", DEFAULT_TTL);
            if (ttl == 0)
            {
                Console.Error.WriteLine("LCM: TTL set to zero, traffic will not leave localhost.");
            }
            else if (ttl > 1)
            {
                Console.Error.WriteLine("LCM: TTL set to > 1... That's almost never correct!");
            }
            else
            {
                Console.Error.WriteLine("LCM: TTL set to 1.");
            }
            sock.Ttl = (short) ttl;
			
			sock.JoinMulticastGroup(inetAddr);
		}

        /// <summary>
        /// Publish a message
        /// </summary>
        /// <param name="channel">channel name</param>
        /// <param name="data">data byte array</param>
        /// <param name="offset">offset of the data to write</param>
        /// <param name="length">length of the data to write</param>
		public void Publish(string channel, byte[] data, int offset, int length)
		{
			lock (this)
			{
				try
				{
					PublishEx(channel, data, offset, length);
				}
				catch (System.Exception ex)
				{
					Console.Error.WriteLine("ex: " + ex);
				}
			}
		}
		
        /// <summary>
        /// Subscribe to a channel
        /// </summary>
        /// <param name="channel">channel name</param>
		public void Subscribe(string channel)
		{
			lock (this)
			{
				if (reader == null)
				{
					reader = new Thread(ReaderThreadRun);
                    reader.IsBackground = true;
					reader.Start();
				}
			}
		}

        /// <summary>
        /// Unsubscribe from a channel
        /// </summary>
        /// <param name="channel">channel name</param>
        public void Unsubscribe(String channel)
        {
        }
		
        /// <summary>
        /// Call to close the provider
        /// </summary>
		public void Close()
		{
			lock (this)
			{
				if (reader != null)
				{
					reader.Interrupt();
					try
					{
						reader.Join();
					}
					catch (System.Threading.ThreadInterruptedException)
					{
					}
				}

				reader = null;
				sock.Close();
				sock = null;
				fragBufs = null;
			}
		}
		
		private void PublishEx(string channel, byte[] data, int offset, int length)
		{
			byte[] channelBytes = System.Text.Encoding.GetEncoding("US-ASCII").GetBytes(channel);
			
			int payloadSize = channelBytes.Length + length;
			
			if (payloadSize <= FRAGMENTATION_THRESHOLD)
			{
				LCMDataOutputStream outs = new LCMDataOutputStream(length + channel.Length + 32);
				
				outs.Write(MAGIC_SHORT);
				outs.Write(this.msgSeqNumber);

                outs.Write(channelBytes, 0, channelBytes.Length);
                outs.Write((byte) 0);
				
				outs.Write(data, offset, length);

                sock.Send(outs.Buffer, outs.Length, inetEP);
			}
			else
			{
				int nfragments = payloadSize / FRAGMENTATION_THRESHOLD;
				if (payloadSize % FRAGMENTATION_THRESHOLD > 0)
					nfragments++;
				
				if (nfragments > 65535)
				{
					Console.Error.WriteLine("LC error: too much data for a single message");
					return;
				}

                // first fragment is special.  insert channel before data
                LCMDataOutputStream outs = new LCMDataOutputStream(10 + FRAGMENTATION_THRESHOLD);
				
				int fragmentOffset = 0;
				int fragNo = 0;
				
				outs.Write(MAGIC_LONG);
				outs.Write(this.msgSeqNumber);
				outs.Write(length);
				outs.Write(fragmentOffset);
				outs.Write((short) fragNo);
				outs.Write((short) nfragments);
				outs.Write(channelBytes, 0, channelBytes.Length);
				outs.Write((byte) 0);

				int firstfragDatasize = FRAGMENTATION_THRESHOLD - (channelBytes.Length + 1);
				
				outs.Write(data, offset, firstfragDatasize);

                sock.Send(outs.Buffer, outs.Length, inetEP);
				
				fragmentOffset += firstfragDatasize;
				
				for (fragNo = 1; fragNo < nfragments; fragNo++)
				{
                    outs = new LCMDataOutputStream(10 + FRAGMENTATION_THRESHOLD);
					
					outs.Write(MAGIC_LONG);
					outs.Write(this.msgSeqNumber);
					outs.Write(length);
					outs.Write(fragmentOffset);
					outs.Write((short) fragNo);
					outs.Write((short) nfragments);
					int fragLen = System.Math.Min(FRAGMENTATION_THRESHOLD, length - fragmentOffset);
					outs.Write(data, offset + fragmentOffset, fragLen);

                    sock.Send(outs.Buffer, outs.Length, inetEP);
					
					fragmentOffset += fragLen;
				}
			}
			
			this.msgSeqNumber++;
		}
		
		private class FragmentBuffer
		{
			internal SocketAddress from = null;
			internal string channel = null;
			internal int msgSeqNumber = 0;
			internal int data_size = 0;
			internal int fragments_remaining = 0;
			internal byte[] data = null;
			
			public FragmentBuffer(SocketAddress from, string channel, int msgSeqNumber, int data_size, int fragments_remaining)
			{
				this.from = from;
				this.channel = channel;
				this.msgSeqNumber = msgSeqNumber;
				this.data_size = data_size;
				this.fragments_remaining = fragments_remaining;
				this.data = new byte[data_size];
			}
		}
		
		private void ReaderThreadRun()
		{
			byte[] packetData;
            IPEndPoint from = new IPEndPoint(IPAddress.Any, 0);
			
			while (true)
			{
				try
				{
					packetData = sock.Receive(ref from);
					HandlePacket(packetData, from);
				}
				catch (Exception ex)
				{
					Console.Error.WriteLine("Ex: " + ex);
					continue;
				}
			}
		}

        private void HandleShortMessage(byte[] packetData, IPEndPoint from, LCMDataInputStream ins)
		{
			int msgSeqNumber = ins.ReadInt32();
			string channel = ins.ReadStringZ();
			
			lcm.ReceiveMessage(channel, ins.Buffer, ins.BufferOffset, ins.Available);
		}

        private void HandleFragment(byte[] packetData, IPEndPoint from, LCMDataInputStream ins)
		{
			int msgSeqNumber = ins.ReadInt32();
			int msgSize = ins.ReadInt32() & unchecked((int) 0xffffffff);
			int fragmentOffset = ins.ReadInt32() & unchecked((int) 0xffffffff);
			int fragmentId = ins.ReadInt16() & 0xffff;
			int fragmentsInMsg = ins.ReadInt16() & 0xffff;
			
			// read entire packet payload
			byte[] payload = new byte[ins.Available];
			ins.ReadFully(payload);
			
			if (ins.Available > 0)
			{
				System.Console.Error.WriteLine("Unread data! " + ins.Available);
			}
			
			int dataStart = 0;
			int fragSize = payload.Length;
			
			FragmentBuffer fbuf;
            fragBufs.TryGetValue(from.Serialize(), out fbuf);
			
			if (fbuf != null && ((fbuf.msgSeqNumber != msgSeqNumber) || (fbuf.data_size != msgSize)))
			{
                fragBufs.Remove(fbuf.from);
				fbuf = null;
			}
			
			if (fbuf == null && fragmentId == 0)
			{	
				// extract channel name
				int channelLen = 0;
				for (; channelLen < payload.Length; channelLen++)
				{
					if (payload[channelLen] == 0)
					{
						break;
					}
				}

				dataStart = channelLen + 1;
				fragSize -= (channelLen + 1);
				string tempStr;
				tempStr = System.Text.Encoding.GetEncoding("US-ASCII").GetString(payload);
				string channel = new string(tempStr.ToCharArray(), 0, channelLen);

                fbuf = new FragmentBuffer(from.Serialize(), channel, msgSeqNumber, msgSize, fragmentsInMsg);

                fragBufs.Add(fbuf.from, fbuf);
			}
			
			if (fbuf == null)
			{
				// TODO
				return ;
			}
			
			if (fragmentOffset + fragSize > fbuf.data_size)
			{
				System.Console.Error.WriteLine("LC: dropping invalid fragment");
                fragBufs.Remove(fbuf.from);
				return ;
			}
			
			Array.Copy(payload, dataStart, fbuf.data, fragmentOffset, fragSize);
			fbuf.fragments_remaining--;
			
			if (0 == fbuf.fragments_remaining)
			{
				lcm.ReceiveMessage(fbuf.channel, fbuf.data, 0, fbuf.data_size);
                fragBufs.Remove(fbuf.from);
			}
		}

        private void HandlePacket(byte[] packetData, IPEndPoint from)
		{
			LCMDataInputStream ins = new LCMDataInputStream(packetData, 0, packetData.Length);
			
			int magic = ins.ReadInt32();
            if (magic == UDPMulticastProvider.MAGIC_SHORT)
			{
				HandleShortMessage(packetData, from, ins);
			}
            else if (magic == UDPMulticastProvider.MAGIC_LONG)
			{
				HandleFragment(packetData, from, ins);
			}
			else
			{
				Console.Error.WriteLine("Bad magic: " + System.Convert.ToString(magic, 16));
				return;
			}
		}
	}
}