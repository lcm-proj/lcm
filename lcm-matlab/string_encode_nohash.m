%author Georg Bremer <georg.bremer@autonomos-systems.de>
%#codegen
function [buf, pos] = string_encode_nohash(buf, pos, maxlen, S, sz)
	for sx = 1:sz
		S_size = string_encodedSize_nohash(S(sx, :));
        endpos = pos - 1 + sz * S_size;
        if pos < 1 || endpos > maxlen
			pos = -1;
			return;
        end
        buf(pos:endpos - 1) = S(sx, 1:S_size - 1);
		buf(endpos) = char(0);
        pos = endpos + 1;
	end


