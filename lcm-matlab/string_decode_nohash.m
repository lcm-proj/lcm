%author Georg Bremer <georg.bremer@autonomos-systems.de>
%#codegen
function [pos, S] = string_decode_nohash(buf, pos, maxlen, S, sz)
	for sx = 1:sz
		if pos < 1
			return
		end
		found = false;
		for endpos = pos:maxlen
			if uint8(0) == buf(endpos)
				S(sx, 1:(endpos - pos + 1)) = char(buf(pos:endpos));
				pos = endpos + 1;
				found = true;
				break
			end
		end
		if ~found
			pos = -1;
		end
	end

