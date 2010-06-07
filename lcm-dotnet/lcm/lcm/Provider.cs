using System;

namespace LCM.LCM
{
	/// <summary>
    /// A provider implements a communications modality for LCM. (I.e., a
	/// URL handler.)
    /// The provider should call LC.receiveMessage() upon receipt of a
	/// message. LCM.ReceiveMessage() is thread-safe and can be called from
	/// any thread.
	/// </summary>
	public interface Provider
	{
		/// <summary>
        /// Publish() will be called when an application sends a message, and
		/// could be called on an arbitrary thread.
		/// </summary>
        /// <param name="channel">channel name</param>
        /// <param name="data">data byte array</param>
        /// <param name="offset">offset of the data to write</param>
        /// <param name="length">length of the data to write</param>
		void Publish(string channel, byte[] data, int offset, int len);
		
		/// <summary>
        /// Subscribe() will be called when a channel subscription has been
		/// made. Providers that do not use a broadcast communications
		/// mechanism could use this notification to establish communications
		/// with additional hosts.
		/// </summary>
        /// <param name="channel">channel name</param>
		void Subscribe(string channel);

        /// <summary>
        /// Unsubscribe() will be called when a channel subscription is cancelled.
        /// </summary>
        /// <param name="channel">channel name</param>
        void Unsubscribe(string channel);

		/// <summary>
        /// Close() will be called when the application no longer requires the provider
		/// and wishes to free the resources used by the provider.  For example,
		/// file handles and network sockets should be closed.  After this method is
		/// called, the results of any calls to publish or subscribe are undefined.
		/// </summary>
		void Close();
	}
}