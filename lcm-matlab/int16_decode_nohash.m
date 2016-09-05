%author Georg Bremer <georg.bremer@autonomos-systems.de>
%#codegen
function [pos, Si] = int16_decode_nohash(buf, pos, maxlen, S, sz)
endpos = pos - 1 + sz * 2;
Si=zeros(size(S),'int16');
if pos < 1 || endpos > maxlen
    pos = -1;
    return;
end
if (littleEndian())
    for sx = 1:sz
        bx = pos + (sx - 1) * 2;
        Si(sx) = typecast(buf(bx+1:-1:bx), 'int16');
    end
else
    Si = typecast(buf(pos:endpos, 1)', 'int16');
end

pos = endpos + 1;