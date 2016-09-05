%This function takes an encoded message and splits it up into multiple udp packets with lcm headers.
%
%author: karlsson
%
%#codegen
function [packets, packet_sizes, packet_count_out] = prepareMessage(payload, sequence_number, channel_name, payload_size, max_packet_size, max_packet_count)

%coder.extrinsic('num2str');

fragmented_header_size = uint32(20);
small_header_size = uint32(8);
channel_name_size = string_encodedSize_nohash(channel_name);
small_message_size = payload_size+small_header_size+channel_name_size;

if small_message_size <= max_packet_size
    packet_count = uint32(1);
    packet_size = uint32(small_message_size);
else
    packet_count = uint32(ceil(double(payload_size+channel_name_size) / double(max_packet_size-fragmented_header_size)));
    packet_size = uint32(max_packet_size);
end

assert(packet_count <= max_packet_count, 'packet_count exceeds max_packet_count');

fragment_header_magic = uint32(hex2dec('4c433033'));
short_header_magic = uint32(hex2dec('4c433032'));

packets = zeros(max_packet_size, max_packet_count, 'uint8');
packet_sizes = zeros(1, max_packet_count, 'uint32');

%assert(false, [char(channel_name) ' Payload: ' num2str(payload_size) ' Packet count: ' num2str(packet_count) ' Max packet size: ' num2str(max_packet_size) ' Max packet count: ' num2str(max_packet_count)]);

if packet_count > 1

    fragment_offset = uint32(0);

    for fx = 1:packet_count
        pos = 1;

        [packets(:,fx), pos] = int32_encode_nohash(packets(:,fx), pos, 4, fragment_header_magic, 1);
        [packets(:,fx), pos] = int32_encode_nohash(packets(:,fx), pos, 8, uint32(sequence_number), 1);
        [packets(:,fx), pos] = int32_encode_nohash(packets(:,fx), pos, 12, payload_size, 1);
        [packets(:,fx), pos] = int32_encode_nohash(packets(:,fx), pos, 16, fragment_offset, 1);
        [packets(:,fx), pos] = int16_encode_nohash(packets(:,fx), pos, 18, int16(fx - 1), 1);
        [packets(:,fx), pos] = int16_encode_nohash(packets(:,fx), pos, 20, packet_count, 1);

        if fx == 1
            [packets(:,fx), pos] = string_encode_nohash(packets(:,fx), pos, 20 + channel_name_size, channel_name, 1);
        end

        payload_rest = payload_size - fragment_offset - 1;
        packet_rest = packet_size - (pos - 1);
        fragment_payload_size = min(packet_rest, payload_rest);
        fragment_end = fragment_offset + fragment_payload_size;
        endpos = pos - 1 + fragment_payload_size;

        %         assert(endpos <= length(packets), 'endpos exceeds length of packets');
        %         assert(fx <= max_packet_count, 'fx exceeds max_packet_count');
        %         assert(fragment_end <= length(payload),'fragment_end exceeds payload length');

        packets(pos:endpos, fx) = payload((fragment_offset + 1):fragment_end);
        packet_sizes(1, fx) = endpos;
        fragment_offset = fragment_end;
    end
else
    pos = 1;
    [packets, pos] = int32_encode_nohash(packets, pos, 4, short_header_magic, 1);
    [packets, pos] = int32_encode_nohash(packets, pos, 8, uint32(sequence_number), 1);
    [packets, pos] = string_encode_nohash(packets, pos, 8 + channel_name_size, channel_name, 1);

    endpos = pos + payload_size - 1;

    %     assert(endpos<=length(packets),'endpos exceeds packet buffer in small message');
    %     assert(payload_size<=length(payload),'payload_size exceeds payload vector in small message');

    packets(pos:endpos, 1) = payload(1:payload_size);
    packet_sizes(1, 1) = endpos;
end

packet_count_out=uint16(packet_count);

