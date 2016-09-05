%author Georg Bremer <georg.bremer@autonomos-systems.de>
%#codegen
function [pos, S] = double_decode_nohash(buf, pos, maxlen, S, sz)
 	endpos = pos - 1 + sz*8;
	if pos < 1 || endpos > maxlen
		pos = -1;
		return;
	end
	if (littleEndian())
		for sx = 1:sz
			bx = pos + (sx-1) * 8;
			S(sx) = typecast([buf(bx + 7), buf(bx + 6), buf(bx + 5), buf(bx + 4), buf(bx + 3), buf(bx + 2), buf(bx + 1), buf(bx)], 'double');
		end
    else
		S = typecast(buf(pos:endpos), 'double');
	end
	pos = endpos + 1;
