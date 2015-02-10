
using lcmtest;

int main(string[] args)
{
	var p = new primitives_t();
	var pl = new primitives_list_t();

	//var p_hh = primitives_t.hash;
	var p_h = primitives_t._compute_hash(null);
	var pl_h = primitives_list_t._compute_hash(null);

	//var p_s = p._encoded_size_no_hash;

	//if (p_h != p_hh)
	//	stdout.printf("hash init problem");


	stdout.printf("FINGERPRINTS:\n");
	stdout.printf("lcmtest.primitives_t      : 0x%08x%08xllu\n", (uint)(p_h >> 32), (uint)(p_h));
	stdout.printf("lcmtest.primitives_t      : 0x%08x%08xllu\n", (uint)(pl_h >> 32), (uint)(pl_h));
	return 0;
}
