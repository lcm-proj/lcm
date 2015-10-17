%author Georg Bremer <georg.bremer@autonomos-systems.de>
%#codegen
function [pos, S] = logical_decode_nohash(buf, pos, maxlen, S, sz)
	[pos, Si] = int8_decode_nohash(buf, pos, maxlen, int8(S), sz);
	S = logical(Si);

