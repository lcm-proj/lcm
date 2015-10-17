%author Georg Bremer <georg.bremer@autonomos-systems.de>
%#codegen
function little = littleEndian()
	tmp = typecast(int32(1), 'uint8');
	little = 1 == tmp(1);

