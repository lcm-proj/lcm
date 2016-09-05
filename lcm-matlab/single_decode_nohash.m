%author Georg Bremer <georg.bremer@autonomos-systems.de>
%#codegen
function [pos, S] = single_decode_nohash(buf, pos, maxlen, S, sz)
	[pos, Si] = int32_decode_nohash(buf, pos, maxlen, typecast(S, 'int32'), sz);
	S = typecast(Si(1,:), 'single');

