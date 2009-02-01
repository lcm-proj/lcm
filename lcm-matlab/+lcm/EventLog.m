classdef EventLog
    properties
        fid = -1;
        mode = ' ';
    end

    methods
        function obj=EventLog(fname, mode)
        % obj = eventlog(fname, mode)
        %
        % Open a LCM log file
        %
        % fname is the file to open
        % mode is 'r' or 'w', for read or write access.
            if nargin == 0
                error('Not enough arguments');
            elseif nargin == 1
                mode = 'r';
            elseif nargin > 2
                error('Too many arguments');
            end

            if(mode ~= 'r') 
                error('Only read mode is supported');
            end

            obj.fid = fopen(fname, mode, 'ieee-be', 'UTF-8');
            obj.mode = mode;
        end

        function delete(obj)
            if (obj.mode == 'r') || (obj.mode == 'w')
                obj.close();
            end
        end

        function close(obj)
        % eventlog.close()
        %
        % Closes a LCM log file.  After invoking this method, the log file
        % cannot be used for anything.
            if (obj.mode ~= 'r') && (obj.mode ~= 'w')
                error('Invalid log object');
            end
            fclose(obj.fid);
            obj.fid = -1;
            obj.mode = ' ';
        end

        function event=read_next_event(obj)
        % event = eventlog.read_next_event()
        %
        % Read the next event from the LCM log file

            % search for magic
            magic = fread(obj.fid, 1, 'int32');
            while magic ~= 3986807297
                b = fread(obj.fid, 1, 'uint8');
                magic = bitor(bitshift(magic, 8, 32), uint32(b));
            end

            % read in event fields
            eventnum = fread(obj.fid, 1, 'int64=>int64');
            timestamp = fread(obj.fid, 1, 'int64=>int64');

            clen = fread(obj.fid, 1, 'int32');
            dlen = fread(obj.fid, 1, 'int32');

            event = struct('eventnum', eventnum, ...
                           'timestamp', timestamp, ...
                           'channel', fread(obj.fid, clen, 'char=>char')', ...
                           'data', fread(obj.fid, dlen, 'uint8=>uint8'));
        end
    end
end
