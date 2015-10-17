%This is used to convert the hash to an int type. Sadly Simulink does not support int64, therefore an array uint32[2] was choosen.
%author Georg Bremer <georg.bremer@autonomos-systems.de>
%
%#codegen
function d = hex2int64(h)
	if ~ischar(h)
		error('Wrong type of argument char h: %s', class(h));
	end
	if length(h) < 16
		h = [repmat('0', 1, 16 - length(h)), h];
	end
	if mod(length(h), 16)
		error('Length of hex string must be less than 16 or a multiple of 16');
	end
	l = length(h) / 8;
	d = uint32([1:l]);
	for ix = 1:16:length(h)
		if littleEndian()
			d(2 * ix - 1: 2 * ix) = uint32( [hex2dec(h(ix + 8: ix + 15)), hex2dec(h(ix + 0: ix + 7))]);
		else
			d(2 * ix - 1: 2 * ix) = uint32( [hex2dec(h(ix + 0: ix + 7)), hex2dec(h(ix + 8: ix + 15))]);
		end
	end
%endfunction
	
