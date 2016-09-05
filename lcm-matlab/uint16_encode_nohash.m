%author Cedric De Brito <cdb@autonomos-systems.de>
%#codegen
function [buf, pos] = uint16_encode_nohash(buf, pos, maxlen, S, sz)
	[buf, pos] = int16_encode_nohash(buf, pos, maxlen, typecast(S, 'int16'), sz);

