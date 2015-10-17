%author Georg Bremer <georg.bremer@autonomos-systems.de>
%#codegen
function [buf, pos] = double_encode_nohash(buf, pos, maxlen, S, sz)
	[buf, pos] = int64_encode_nohash(buf, pos, maxlen, typecast(S, 'int32'), sz);
	
