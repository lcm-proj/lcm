/* Check hash calculations
 *
 * {{{
 * P=../..
 * $P/lcmgen/lcm-gen --vala ../types/lcmtest/p*.lcm
 * valac --vapidir=$P/lcm-vala/vapi --pkg=lcm check-hash.vala $P/lcm-vala/lcm_message.vala lcmtest.primitives_*vala
 * }}}
 */

using lcmtest;

static const string PRI64 = int64.FORMAT_MODIFIER;

void print_hash(Lcm.IMessage msg)
{
	// XXX: this discards dots from namespace.
	var t = GLib.Type.from_instance(msg);
	var ch = msg.hash;

	stdout.printf("%-30s : 0x%016" + PRI64 + "x\n", t.name(), ch);
}


int main(string[] args)
{
	//p.name = "some string";
	//var p_s = p._encoded_size_no_hash;

	// ???: static typename.hash don't work, but instance.hash works!
	var pl = new primitives_list_t();
	if (pl.hash != primitives_list_t._compute_hash(null))
		stdout.printf("hash init problem!");

	stdout.printf("FINGERPRINTS:\n");
	print_hash(new bools_t());
	print_hash(new byte_array_t());
	print_hash(new comments_t());
	print_hash(new exampleconst_t());
	print_hash(new multidim_array_t());
	print_hash(new node_t());
	print_hash(new primitives_t());
	print_hash(new primitives_list_t());

	return 0;
}
