/* Test lcm.vapi
 *
 * This test only checks lcm library binding.
 *
 * {{{
 * valac --vapidir ../../lcm-vala/vapi --pkg lcm -C buf-xfer.vala
 * gcc buf-xfer.c -o buf-xfer `pkg-config --libs --cflags lcm gobject-2.0`
 * }}}
 */

using Lcm;

static void topic_cb(RecvBuf rbuf, string channel, void *user_data)
{
	stdout.printf("got message from %s channel, size %u bytes\n", channel, rbuf.data.length);
}

int main(string[] args)
{
	int64[] testdatamesg = { 0xd00dfeed, 0xdeadbeef, 0xeeeeeeee, 0xffff0000, 0x5a5aa5a5 };

	// hello
	stdout.printf("testing vapi for LCM %d.%d.%d\n", Lcm.Version.MAJOR, Lcm.Version.MINOR, Lcm.Version.MICRO);

	// create object
	var lcm = new Lcm.LcmNode();

	// check file no
	var fn = lcm.get_fileno();
	stdout.printf("lcm fileno: %d\n", fn);

	// make subscription
	unowned Subscription sub = lcm.subscribe("test_topic", topic_cb);

	// publish
	stdout.printf("try to publish message\n");
	lcm.publish("test_topic", (void[])testdatamesg);

	// handle
	stdout.printf("handle\n");
	lcm.handle();

	lcm.publish("test_topic", (void[])testdatamesg);
	lcm.handle_timeout(1000);

	// change queue
	stdout.printf("set_queue_capacity\n");
	sub.set_queue_capacity(100);

	lcm.unsubscribe(sub);

	return 0;
}
