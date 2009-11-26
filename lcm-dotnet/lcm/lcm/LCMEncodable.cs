using System;

namespace LCM.LCM
{
	/// <summary>
    /// A message which can be easily sent using LCM.
    /// </summary>
	public interface LCMEncodable
	{
		/// <summary>
        /// Invoked by LCM.
        /// </summary>
		/// <param name="outs">any data to be sent should be written to this output stream.</param>
		void Encode(System.IO.BinaryWriter outs);
	}
}