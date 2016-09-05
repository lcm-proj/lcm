%author Georg Bremer <georg.bremer@autonomos-systems.de>
%#codegen
function [pos, S] = int8_decode_nohash(buf, pos, maxlen, S, sz)
	endpos = pos - 1 + sz;
	if pos < 1 || endpos > maxlen
		pos = -1;
		return;
	end
	tmp = buf(pos:endpos, 1)';
	S = typecast(tmp(1, :), 'int8');
	pos = endpos + 1;

