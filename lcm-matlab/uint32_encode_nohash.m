%author Cedric De Brito <cdb@autonomos-systems.de>
%#codegen
function [buf, pos] = uint32_encode_nohash(buf, pos, maxlen, S, sz)
	[buf, pos] = int32_encode_nohash(buf, pos, maxlen, typecast(S, 'int32'), sz);

