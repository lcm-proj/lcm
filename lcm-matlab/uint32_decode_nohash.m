%author Cedric De Brito <cdb@autonomos-systems.de>
%#codegen
function [pos, S] = uint32_decode_nohash(buf, pos, maxlen, S, sz)
	[pos, Si] = int32_decode_nohash(buf, pos, maxlen, S, sz);
	S = typecast(Si, 'uint32');

