%This function is used in the compute_hash function, which is only called once for each data type.
%Arguments a and b must be both uint32[2].
%
%author Georg Bremer <georg.bremer@autonomos-systems.de>
%
%#codegen
function c = add_overflow(a, b)
	c = uint32([0, 0]);

	if littleEndian()
		[c(1), overflow] = add(a(1), b(1));
		[c(2), ~] = add(a(2), b(2));
		[c(2), ~] = add(c(2), overflow);
	else
		[c(2), overflow] = add(a(2), b(2));
		[c(1), ~] = add(a(1), b(1));
		[c(1), ~] = add(c(1), overflow);
	end
%endfunction

function [c, overflow] = add(a, b)
	if b > intmax('uint32') - a
		c = b - (intmax('uint32') - a + 1);
		overflow = uint32(1);
	else
		c = a + b;
		overflow = uint32(0);
	end

