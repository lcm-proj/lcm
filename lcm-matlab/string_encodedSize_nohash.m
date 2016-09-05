%For strings we manually have to count the size. The lcm standard states, that strings are null terminated.
%
%author Georg Bremer <georg.bremer@autonomos-systems.de>
%
%#codegen
function bytes = string_encodedSize_nohash(S)
	bytes = 1;
	for bytes = 1:length(S)
		if char(0) == S(bytes)
			return
		end
	end
	bytes = bytes + 1;

