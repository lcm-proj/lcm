/**
 * Lightweight Communications and Marshalling.
 */

namespace Lcm {

	namespace CoreTypes {
		// intptr defined in VAPI


	}

	public errordomain MessageError {
		DECODE
	}

	public interface IMessage {
		public abstract void decode(void[] data) throws MessageError;
		public abstract void[] encode();

		public abstract ssize_t _decode_no_hash(void[] data, Posix.off_t offset);
		public abstract ssize_t _encode_no_hash(void[] data, Posix.off_t offset);
		public abstract size_t _encoded_size_no_hash { get; }

		public abstract int64 hash { get; }
	}

}
