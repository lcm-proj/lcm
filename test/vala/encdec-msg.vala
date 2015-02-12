/**
 * Check encode/decode routines
 */

using lcmtest;

void print_values(primitives_t p)
{
	stderr.printf("Encoded size: %" + size_t.FORMAT +  "\n", p._encoded_size_no_hash + 8);
	stderr.printf("Hash: 0x%016" + int64.FORMAT_MODIFIER + "x\n", p.hash);
	stderr.printf("p.i8 = %d\n", p.i8);
	stderr.printf("p.i16 = %" + int16.FORMAT + "\n", p.i16);
	stderr.printf("p.num_ranges = %" + int32.FORMAT + "\n", p.num_ranges);
	stderr.printf("p.i64 = %" + int64.FORMAT + "\n", p.i64);
	// TODO other fields
}

int main(string[] args)
{
	var mode = "encode";
	if (args.length > 1) {
		mode = args[1];
	}

	if (mode != "encode") {
		stderr.puts("XXX only encode supported");
		return 1;
	}

	if (mode == "encode") {
		var p = new primitives_t();

		p.i8 = 8;
		p.i16 = 16;
		p.i64 = 64;

		p.ranges = new int16[32];
		p.num_ranges = p.ranges.length;
		for (int i = 0; i < p.ranges.length; i++)
			p.ranges[i] = 42;

		p.position = {1.0f, 2.0f, 3.0f};
		p.orientation = {1.0f, 0.0f, 0.0f, 0.0f};

		p.name = "test message: hello LCM!";
		p.enabled = true;

		uint8[] b = (uint8[]) p.encode();

		stderr.printf("Buf size: %d\n", b.length);
		print_values(p);

		stdout.write(b);
	}

	return 0;
}
