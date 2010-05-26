using System;
using System.IO;

namespace LCM.LCM
{
	/// <summary>
    /// A message which can be easily sent using LCM.
    /// </summary>
	public interface LCMEncodable
    {
        /*
         * LCMEncodables will always have an empty constructor and a
         * constructor that takes a DataInput.
         */

		/// <summary>
        /// Invoked by LCM.
        /// </summary>
		/// <param name="outs">any data to be sent should be written to this output stream.</param>
		void Encode(BinaryWriter outs);
        
        /// <summary>
        /// Encode the data without the magic header. Most users will never use this function.
        /// </summary>
        /// <param name="outs"></param>
        void _encodeRecursive(BinaryWriter outs);

        /// <summary>
        /// Decode the data without the magic header. Most users will never use this function.
        /// </summary>
        /// <param name="ins"></param>
        void _decodeRecursive(BinaryReader ins);
	}
}