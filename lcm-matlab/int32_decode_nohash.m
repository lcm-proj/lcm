%author Georg Bremer <georg.bremer@autonomos-systems.de>
%#codegen
function [pos, Si] = int32_decode_nohash(buf, pos, maxlen, S, sz)
endpos = pos - 1 + sz*4;
Si=zeros(size(S),'int32');
if pos < 1 || endpos > maxlen
    pos = -1;
    return;
end
if (littleEndian())
    for sx = 1:sz
        bx = pos + (sx-1) * 4;
        Si(sx) = typecast(buf(bx+3:-1:bx), 'int32');
    end
else
    Si = typecast(buf(pos:endpos)', 'int32');
end

pos = endpos + 1;