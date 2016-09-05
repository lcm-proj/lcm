%author Georg Bremer <georg.bremer@autonomos-systems.de>
%#codegen
function [pos, S] = int64_decode_nohash(buf, pos, maxlen, S, sz)
	endpos = pos - 1 + sz*8;
	if pos < 1 || endpos > maxlen
		pos = -1;
		return;
	end
	if (littleEndian())
		for sx = 1:sz
			bx = pos + (sx-1) * 8;
			S(2*sx - 1: 2*sx) = typecast(buf((bx+7):-1:bx), 'uint32');
		end
	else
		S = typecast(buf(pos:endpos), 'uint32');
		S = S(:);
	end
	pos = endpos + 1;

