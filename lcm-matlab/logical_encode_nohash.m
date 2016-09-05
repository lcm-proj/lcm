%author Georg Bremer <georg.bremer@autonomos-systems.de>
%#codegen
function [buf, pos] = logical_encode_nohash(buf, pos, maxlen, S, sz)
	[buf, pos] = int8_encode_nohash(buf, pos, maxlen, int8(S), sz);

