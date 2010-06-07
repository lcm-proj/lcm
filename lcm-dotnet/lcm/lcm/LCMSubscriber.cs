using System;

namespace LCM.LCM
{
	/// <summary>
    /// A class which listens for messages on a particular channel.
    /// </summary>
	public interface LCMSubscriber
	{
		/// <summary>
        /// Invoked by LCM when a message is received.
		/// 
		/// This method is invoked from the LCM thread.
		/// </summary>
		/// <param name="lcm">the LCM instance that received the message</param>
		/// <param name="channel">the channel on which the message was received</param>
		/// <param name="ins">the message contents</param>
		void MessageReceived(LCM lcm, string channel, LCMDataInputStream ins);
	}
}