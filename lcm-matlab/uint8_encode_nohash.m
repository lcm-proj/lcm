%author Georg Bremer <georg.bremer@autonomos-systems.de>
%#codegen
function [buf, pos] = uint8_encode_nohash(buf, pos, maxlen, S, sz)
	[buf, pos] = int8_encode_nohash(buf, pos, maxlen, typecast(S, 'int8'), sz);

