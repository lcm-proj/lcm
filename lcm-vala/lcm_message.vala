/**
 * Lightweight Communications and Marshalling.
 */

namespace Lcm {

	namespace CoreTypes {
		/**
		 * Used to calculate message hash
		 */
		public struct HashPtr {
			public unowned HashPtr? parent;
			void *v;
		}
	}

	public interface IMessage {
		public abstract void decode(void[] data);
		public abstract void[] encode();

		public abstract int _decode_no_hash(void[] data, int offset);
		public abstract int _encode_no_hash(void[] data, int offset);
		public abstract int _encoded_size_no_hash { get; }

		public abstract int64 hash { get; }
	}
}
