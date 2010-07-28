using System;
using System.Collections.Generic;
using System.Text.RegularExpressions;

namespace LCM.LCM
{	
	/// <summary>
    /// Lightweight Communications and Marshalling C#.NET implementation
    /// </summary>
	public class LCM
    {
        private class SubscriptionRecord
        {
            internal string regex;
            internal Regex pat;
            internal LCMSubscriber lcsub;
        }

        private List<SubscriptionRecord> subscriptions = new List<SubscriptionRecord>();
        private List<Provider> providers = new List<Provider>();

        private Dictionary<string, List<SubscriptionRecord>> subscriptionsMap = new Dictionary<string, List<SubscriptionRecord>>();

        private bool closed = false;

        private static LCM singleton;

        private LCMDataOutputStream encodeBuffer = new LCMDataOutputStream(new byte[1024]);
		
		/// <summary>
        /// Retrieve a default instance of LCM using either the environment
		/// variable LCM_DEFAULT_URL or the default. If an exception
		/// occurs, exit(-1) is called.
        /// </summary>
		public static LCM Singleton
		{
            get
            {
                if (singleton == null)
                {
                    try
                    {
                        singleton = new LCM();
                    }
                    catch (Exception ex)
                    {
                        Console.Error.WriteLine("LCM singleton fail: " + ex);
                        Environment.Exit(-1);
                        return null;
                    }
                }

                return singleton;
            }
		}

		/// <summary>
        /// The number of subscriptions.
        /// </summary>
		public int NumSubscriptions
		{
			get
			{
                if (this.closed)
                {
                    throw new SystemException();
                }

				return subscriptions.Count;
			}
			
		}

		/// <summary>
        /// Create a new LCM object, connecting to one or more URLs. If
		/// no URL is specified, the environment variable LCM_DEFAULT_URL is
		/// used. If that environment variable is not defined, then the
		/// default URL is used.
        /// </summary>
        /// <param name="urls">URL array</param>
        public LCM(params string[] urls)
        {
            if (urls.Length == 0)
			{
				string env = Environment.GetEnvironmentVariable("LCM_DEFAULT_URL");

                if (env == null)
                {
                    urls = new string[] { "udpm://239.255.76.67:7667" };
                }
                else
                {
                    urls = new string[] { env };
                }
			}
			
			foreach (string url in urls)
			{
				URLParser up = new URLParser(url);
				string protocol = up.Get("protocol");

                if (protocol.Equals("udpm"))
                {
                    providers.Add(new UDPMulticastProvider(this, up));
                }
                else if (protocol.Equals("tcpq"))
                {
                    providers.Add(new TCPProvider(this, up));
                }/*
                else if (protocol.Equals("file"))
                {
                    providers.Add(new LogFileProvider(this, up));
                }*/
                else
                {
                    Console.Error.WriteLine("LCM: Unknown URL protocol: " + protocol);
                }
			}
		}
		
		/// <summary>
        /// Publish a string on a channel. This method does not use the
		/// LCM type definitions and thus is not type safe. This method is
		/// primarily provided for testing purposes and may be removed in
		/// the future.
		/// </summary>
        /// <param name="channel">channel name</param>
        /// <param name="s">string to publich</param>
		public void Publish(string channel, string s)
		{
            if (this.closed)
            {
                throw new SystemException();
            }

			s = s + "\0";
            byte[] b = System.Text.Encoding.GetEncoding("US-ASCII").GetBytes(s);

			Publish(channel, b, 0, b.Length);
		}
		
		/// <summary>
        /// Publish an LCM-defined type on a channel. If more than one URL was
		/// specified, the message will be sent on each.
        /// </summary>
        /// <param name="channel">channel name</param>
        /// <param name="e">encodable object to send</param>
		public void Publish(string channel, LCMEncodable e)
		{
            if (this.closed)
            {
                throw new SystemException();
            }
				
			lock (this)
			{
				try
				{
					encodeBuffer.Reset();
					
					e.Encode(encodeBuffer);
					
					Publish(channel, encodeBuffer.Buffer, 0, encodeBuffer.Length);
				}
				catch (System.IO.IOException ex)
				{
					Console.Error.WriteLine("LC Publish fail: " + ex);
				}
			}
		}
		
		/// <summary>
        /// Publish raw data on a channel, bypassing the LCM type
		/// specification. If more than one URL was specified when the LCM
		/// object was created, the message will be sent on each.
        /// </summary>
        /// <param name="channel">channel name</param>
        /// <param name="data">data byte array</param>
        /// <param name="offset">offset of the data to write</param>
        /// <param name="length">length of the data to write</param>
		public void Publish(string channel, byte[] data, int offset, int length)
		{
			lock (this)
			{
                if (this.closed)
                {
                    throw new SystemException();
                }

                foreach (Provider p in providers)
                {
                    p.Publish(channel, data, offset, length);
                }
			}
		}
		
		/// <summary>
        /// Subscribe to all channels whose name matches the regular
		/// expression. Note that to subscribe to all channels, you must
		/// specify ".*", not "*".
        /// </summary>
        /// <param name="regex">regular expression determining the channels to subscribe</param>
        /// <param name="sub">subscribing object providing callback</param>
		public void Subscribe(string regex, LCMSubscriber sub)
		{
            if (this.closed)
            {
                throw new SystemException();
            }

			SubscriptionRecord srec = new SubscriptionRecord();
			srec.regex = regex;
			srec.pat = new Regex(regex);
			srec.lcsub = sub;
			
			lock (this)
			{
				foreach (Provider p in providers)
                {
				    p.Subscribe(regex);
                }
			}
			
			lock (subscriptions)
			{
                subscriptions.Add(srec);
                List<SubscriptionRecord> subs;
				
				foreach (string channel in subscriptionsMap.Keys)
				{
					if (srec.pat.IsMatch(channel))
                    {
                        if (subscriptionsMap.TryGetValue(channel, out subs))
                        {
                            subs.Add(srec);
                        }
					}
				}
			}
		}
		
		/// <summary>
        /// A convenience function that subscribes to all LCM channels.
        /// </summary>
        /// <param name="sub">subscribing object providing callback</param>
		public void SubscribeAll(LCMSubscriber sub)
		{
			Subscribe(".*", sub);
		}
		
		/// <summary>
        /// Remove this particular regex/subscriber pair (UNTESTED AND API
		/// MAY CHANGE). If regex is null, all subscriptions for 'sub' are
		/// cancelled. If subscriber is null, any previous subscriptions
		/// matching the regular expression will be cancelled. If both
		/// 'sub' and 'regex' are null, all subscriptions will be
		/// cancelled.
        /// </summary>
        /// <param name="regex">regular expression determining the channels to unsubscribe</param>
        /// <param name="sub">unsubscribing object</param>
		public void Unsubscribe(string regex, LCMSubscriber sub)
		{
            if (this.closed)
            {
                throw new SystemException();
            }
			
            lock (this)
            {
                foreach (Provider p in providers)
                {
                    p.Unsubscribe(regex);
                }
            }

			// TODO: need providers to unsubscribe?
			// TODO: providers don't seem to use anything beyond first channel
			
			lock (subscriptions)
			{	
				// Find and remove subscriber from list
                foreach (SubscriptionRecord sr in subscriptions.ToArray())
                {
                    if ((sub == null || sr.lcsub == sub) && (regex == null || sr.regex.Equals(regex)))
                    {
                        subscriptions.Remove(sr);
                    }
                }

                // Find and remove subscriber from map
                List<SubscriptionRecord> srecs;
	            foreach (string channel in subscriptionsMap.Keys)
                {
                    if (subscriptionsMap.TryGetValue(channel, out srecs))
                    {
                        foreach (SubscriptionRecord sr in srecs.ToArray())
                        {
		                    if ((sub == null || sr.lcsub == sub) && (regex == null || sr.regex.Equals(regex)))
                            {
                                srecs.Remove(sr);
		                    }
                        }
                    }
	            }
            }
		}
		
		/// <summary>
        /// Call this function to release all resources used by the LCM instance.  After calling this
		/// function, the LCM instance should consume no resources, and cannot be used to 
		/// receive or transmit messages.
		/// </summary>
		public void Close()
		{
			if (this.closed)
            {
				throw new SystemException();
            }

			lock (this)
			{
				foreach (Provider p in providers)
				{
					p.Close();
				}

				providers = null;
				this.closed = true;
			}
		}
		
		/// <summary>
        /// Not for use by end users. Provider back ends call this method
		/// when they receive a message. The subscribers that match the
		/// channel name are synchronously notified.
        /// </summary>
		internal void ReceiveMessage(string channel, byte[] data, int offset, int length)
		{
            if (this.closed)
            {
                throw new SystemException();
            }

			lock (subscriptions)
            {
                List<SubscriptionRecord> srecs;
                subscriptionsMap.TryGetValue(channel, out srecs);

				if (srecs == null)
				{
					// must build this list!
					srecs = new List<SubscriptionRecord>();
					subscriptionsMap.Add(channel, srecs);
					
					foreach (SubscriptionRecord srec in subscriptions)
					{
                        if (srec.pat.IsMatch(channel))
                        {
                            srecs.Add(srec);
                        }
					}
				}
				
				foreach (SubscriptionRecord srec in srecs)
				{
					srec.lcsub.MessageReceived(this, channel, new LCMDataInputStream(data, offset, length));
				}
			}
		}
	}
}