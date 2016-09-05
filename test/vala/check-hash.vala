/* Check hash calculations
 *
 * {{{
 * P=../..
 * $P/lcmgen/lcm-gen --vala $(find ../types -name '*.lcm')
 * valac --vapidir=$P/lcm-vala/vapi --pkg=lcm check-hash.vala $P/lcm-vala/lcm_message.vala lcmtest*.vala
 * }}}
 */

using lcmtest;
using lcmtest2;

static const string PRI64 = int64.FORMAT_MODIFIER;

void print_hash(Lcm.IMessage msg)
{
	// XXX: this discards dots from namespace.
	var t = GLib.Type.from_instance(msg);
	var ch = msg.hash;
	var s = sizeof(int64) + msg._encoded_size_no_hash;

	stdout.printf("%-30s : 0x%016" + PRI64 + "x : %" + size_t.FORMAT + "\n", t.name(), ch, s);
}


int main(string[] args)
{
	// ???: static typename.hash don't work, but instance.hash works!
	var pl = new primitives_list_t();
	if (pl.hash != primitives_list_t._compute_hash(null))
		stdout.printf("hash init problem!");

	stdout.printf("%-30s : %-18s : MIN SIZE\n", "NAME", "FINGERPRINT");
	print_hash(new bools_t());
	print_hash(new byte_array_t());
	print_hash(new comments_t());
	print_hash(new exampleconst_t());
	print_hash(new multidim_array_t());
	print_hash(new node_t());
	print_hash(new primitives_t());
	print_hash(new primitives_list_t());
	print_hash(new primitives_array_t());
	print_hash(new another_type_t());
	print_hash(new cross_package_t());

	return 0;
}
