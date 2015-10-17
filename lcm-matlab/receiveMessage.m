%This function receives messages into an internal persistent buffer and tries to reassemble them.
%However, due to constraints in the Simulink receive block this is not very reliable. We do not know who the send
%therefore if we receive split messages from more than one sender, we probably mess things up.
%
%author Georg Bremer <georg.bremer@autonomos-systems.de>
%
%#codegen
function [buf, pos, missing, sequence] = receiveMessage(packet, channel, max_packet_size, max_packet_count)

	fragment_header_magic = uint32(hex2dec('4c433033'));
	short_header_magic = uint32(hex2dec('4c433032'));
    buf = zeros(max_packet_size * max_packet_count, 1, 'uint8');

	persistent packet_buf;
    if isempty(packet_buf)
		packet_buf = zeros(max_packet_size * max_packet_count, 1, 'uint8');
    end
  
	persistent current_sequence_number;
	if isempty(current_sequence_number)
		current_sequence_number = int32(-1);
	end
	persistent missing_fragments;
	if isempty(missing_fragments)
		missing_fragments = int16(0);
	end

	packet_size = length(packet);
	assert(packet_size <= max_packet_size);

	missing = int16(-1);
    sequence = int32(-1);
    
	pos = 1;
	[pos, header_magic] = int32_decode_nohash(packet, pos, packet_size, int32(0), 1);
	[pos, sequence_number] = int32_decode_nohash(packet, pos, packet_size, int32(0), 1);
	if header_magic(1) == short_header_magic
		channel_name = char(zeros(1, packet_size - pos + 1, 'uint8'));
		[pos, channel_name] = string_decode_nohash(packet, pos, packet_size, channel_name, 1);
		if ~strcmp(channel_name(channel_name ~= 0), char(channel))
			pos = 0;
			return
		end

		if pos < 1
			return
		end

		packet_buf(1: packet_size - pos + 1) = packet(pos:packet_size);
		missing_fragments = int16(0);
        sequence = sequence_number(1);
	elseif header_magic(1) == fragment_header_magic
		[pos, payload_size] = int32_decode_nohash(packet, pos, packet_size, int32(0), 1);
		[pos, fragment_offset] = int32_decode_nohash(packet, pos, packet_size, int32(0), 1);
		[pos, fragment_number] = int16_decode_nohash(packet, pos, packet_size, int16(0), 1);
		[pos, n_fragments] = int16_decode_nohash(packet, pos, packet_size, int16(0), 1);

		if ~fragment_number(1)
			channel_name = char(zeros(1, packet_size - pos + 1, 'uint8'));
			[pos, channel_name] = string_decode_nohash(packet, pos, packet_size, channel_name, 1);
			if ~strcmp(channel_name(channel_name ~= 0), char(channel))
				pos = 0;
				return
			end
		end
		
		if pos < 1
			return
        end

        if payload_size(1) > max_packet_size * max_packet_count
            pos = 0;
            return
        end

		if current_sequence_number ~= sequence_number(1)
			packet_buf = zeros(max_packet_size * max_packet_count, 1, 'uint8');
			current_sequence_number = sequence_number(1);
			missing_fragments = n_fragments(1);
		end

		packet_buf((fragment_offset(1) + 1): fragment_offset(1) + packet_size - pos + 1) = packet(pos:packet_size);
		missing_fragments = missing_fragments - 1;

		if missing_fragments
			pos = 0;
		else
            sequence = sequence_number(1);
		end
	else
		pos = -1;
	end
	missing = missing_fragments;
    buf(1:max_packet_size * max_packet_count) = packet_buf;

end
