%author Georg Bremer <georg.bremer@autonomos-systems.de>
%#codegen
function [buf, pos] = int8_encode_nohash(buf, pos, maxlen, S, sz)
	endpos = pos - 1 + sz;
	if pos < 1 || endpos > maxlen
		pos = -1;
		return;
	end
	buf(pos:endpos) = typecast(S, 'uint8');
	pos = endpos + 1;

