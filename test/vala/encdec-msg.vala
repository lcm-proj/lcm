/**
 * Check encode/decode routines
 *
 * {{{
 * ./encdec-msg | ./encdec-msg decode
 * }}}
 */

using lcmtest;

void print_values(primitives_t p)
{
	stderr.printf("Encoded size: %" + size_t.FORMAT +  "\n", p._encoded_size_no_hash + 8);
	stderr.printf("Hash: 0x%016" + int64.FORMAT_MODIFIER + "x\n", p.hash);
	stderr.printf("---\n");
	stderr.printf("p.i8 = %d\n", p.i8);
	stderr.printf("p.i16 = %" + int16.FORMAT + "\n", p.i16);
	stderr.printf("p.num_ranges = %" + int32.FORMAT + "\n", p.num_ranges);
	stderr.printf("p.i64 = %" + int64.FORMAT + "\n", p.i64);

	stderr.printf("p.ranges[] = ");
	foreach (var v in p.ranges)
		stderr.printf("%" + int16.FORMAT + " ", v);
	stderr.printf("\n");

	stderr.printf("p.position[3] = %f %f %f\n",
			p.position[0], p.position[1], p.position[2]);
	stderr.printf("p.orientation[4] = %f %f %f %f\n",
			p.orientation[0], p.orientation[1], p.orientation[2], p.orientation[3]);

	stderr.printf("p.name = %s\n", p.name);
	stderr.printf("p.enabled = %s\n", p.enabled? "true" : "false");
}

int main(string[] args)
{
	var mode = "encode";
	if (args.length > 1) {
		mode = args[1];
	}

	if (mode != "encode" && mode != "decode") {
		stderr.puts("encdec-msg (encode|decode)");
		return 1;
	}

	stderr.printf("===\n");

	if (mode == "encode") {
		var p = new primitives_t();

		p.i8 = 8;
		p.i16 = 16;
		p.i64 = 64;

		p.ranges = new int16[32];
		p.num_ranges = p.ranges.length;
		for (int i = 0; i < p.ranges.length; i++)
			p.ranges[i] = 42 + i;

		p.position = {1.0f, 2.0f, 3.0f};
		p.orientation = {1.0, 0.0, 0.0, 0.0};

		p.name = "test message: hello LCM!";
		p.enabled = true;

		uint8[] b = (uint8[]) p.encode();

		stderr.printf("Write size: %" + size_t.FORMAT + "\n", b.length);
		print_values(p);
		stdout.write(b);
	}
	if (mode == "decode") {
		var buf = new uint8[1400];

		var bl = stdin.read(buf);
		stderr.printf("Read size: %" + size_t.FORMAT + "\n", bl);

		var p = new primitives_t();

		p.decode((void[]) buf);

		print_values(p);
	}

	return 0;
}
