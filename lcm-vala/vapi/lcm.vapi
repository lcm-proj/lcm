/* LCM bindings
 *
 */

[CCode (cheader_filename = "lcm/lcm.h")]
namespace lcm {
	[CCode (cname = "lcm_t", free_function = "lcm_destroy", cprefix = "lcm_")]
	[Compact]
	public class LCM {
		[CCode (cname = "lcm_create")]
		public LCM(string? provider);

		public int get_fileno();
		public unowned Subscription subscribe(string channel, Subscription.Handler handler, void *user_data = null);
		public int unsubscribe(Subscription handler);
		public int publish(string channel, void[] data);
		public int handle();
		public int handle_timeout(int timeout_millis);
	}

	// XXX!!!!
	[CCode (cname = "lcm_subscription_t", cprefix = "lcm_subscription_")]
	[Compact]
	public class Subscription {
		public int set_queue_capacity(int num_messages);

		[CCode (cname = "lcm_msg_handler_t", has_target = false)]
		public delegate void Handler(RecvBuf rbuf, string channel, void *user_data);
	}

	[CCode (cname = "lcm_recv_buf_t")]
	public struct RecvBuf {
		void *data;
		uint32 data_size;
		int64 recv_utime;
		unowned LCM lcm;
	}

	/* Library version
	 */
	[CCode (cname = "int", has_type_id = false)]
	public enum Version {
		[CCode (cname = "LCM_MAJOR_VERSION")]
		MAJOR,
		[CCode (cname = "LCM_MINOR_VERSION")]
		MINOR,
		[CCode (cname = "LCM_MICRO_VERSION")]
		MICRO
	}
}


