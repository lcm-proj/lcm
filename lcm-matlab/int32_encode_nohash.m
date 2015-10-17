%author Georg Bremer <georg.bremer@autonomos-systems.de>
%#codegen
function [buf, pos] = int32_encode_nohash(buf, pos, maxlen, S, sz)
	endpos = pos - 1 + sz * 4;
	if pos < 1 || endpos > maxlen
		pos = -1;
		return;
	end
	if littleEndian()
		Sb = typecast(S, 'uint8');
		for ix = 1:sz
			bx = pos + (ix - 1) * 4;
			sx = (ix - 1) * 4 + 1;
			buf(bx:bx+3) = Sb(sx+3:-1:sx);
		end
	else
		buf(pos:endpos) = typecast(S, 'uint8');
	end
	pos = endpos + 1;

