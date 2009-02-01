classdef LCM
    properties(Access=private)
        mex = false;
    end

    methods
        function obj=LCM(varargin)
            % lcm = lcm.LCM()
            %
            % lcm = lcm.LCM(lcmurl)
            %
            % Initializes an LCM instance and returns a handle to that instance.
            %
            % When invoked with no arguments, the default LCM URL is used.
            %
            % When lcmurl is specified, this is passed directly to the C LCM API.
            %
            % available methods:
            %     close()
            %     publish(channel, data)
            %     subscribe(channel [, userdata])
            %     get_fileno()
            %     get_messages([timeout_ms])
            %
            if length(varargin) == 0
                obj.mex = lcm_mex('create');
            elseif length(varargin) == 1
                obj.mex = lcm_mex('create', varargin{1});
            else 
                error('Too many arguments');
            end
        end

        function delete(obj)
            if(~islogical(obj.mex))
                obj.close();
            end
        end

        function close(obj)
            % close()
            %
            % Releases all memory and resources consumed by an LCM instance.  Call this
            % function when the LCM instance is no longer needed.
            if(~islogical(obj.mex))
                lcm_mex('destroy', obj.mex)
                obj.mex = false;
            else
                error('LCM instance already closed');
            end
        end

        function fd=get_fileno(obj)
            % fd = get_fileno()
            %
            % Returns the file descriptor for the underlying LCM instance.  This can 
            % be used for socket-based polling mechanisms.
            fd=lcm_mex('get_fileno', obj.mex);
        end

        function publish(obj, channel, data)
            % publish(channel, data)
            %
            % publishes a message over LCM.
            %
            % channel is a string specifying desired the LCM channel
            %
            % data is the byte buffer to publish.  It must be of type string, int8, or
            % uint8
            lcm_mex('publish', obj.mex, channel, data);
        end

        function subs=subscribe(varargin)
            % subs = subscribe(channel)
            %
            % subs = subscribe(channel, userdata)
            %
            % Subscribes to an LCM channel.
            %
            % channel specifies the channel or channels to subscribe to, and 
            % is interpreted as a POSIX extended regular expression.  It is implicitly
            % surrounded by ^ and $.  
            %
            % See the documentation for lcm_get_messages to see how userdata is used 
            % when it is specified.
            %
            % Returns a subscription handle.  Pass this handle to lcm_unsubscribe to 
            % unsubscribe from the specified channels.
            %
            % Examples:
            %
            %  obj.subscribe("POSE");
            %
            %     subscribes to the "POSE" channel
            %
            %  obj.subscribe("RADAR.*");
            %
            %     subscribes to all channels that start with "RADAR"
            %
            %  obj.subscribe("ABC|DEF|GHI");
            %
            %     subscribes to the channels ABC, DEF, and GHI
            if nargin < 2
                error('Not enough arguments');
            elseif nargin == 2
                obj = varargin{1};
                channel = varargin{2};
                userdata = '';
                subs = lcm_mex('subscribe', obj.mex, channel, userdata);
            elseif nargin == 3
                obj = varargin{1};
                channel = varargin{2};
                userdata = varargin{3};
                subs = lcm_mex('subscribe', obj.mex, channel, userdata);
            else
                error('Too many arguments');
            end
        end

        function unsubscribe(obj, subs)
            % unsubscribe(subs)
            %
            % Unsubscribes from one or more channels.
            %
            % subs must be a subscription handle as returned by lcm_subscribe
            lcm_mex('unsubscribe', obj.mex, subs);
        end

        function msgs=get_messages(varargin)
            % messages = get_messages()
            %
            % messages = get_messages(timeout_ms)
            %
            % Waits for and returns a struct matrix of received LCM messages.
            %
            % If timeout_ms is specified, then this function waits at most timeout_ms
            % milliseconds.  Otherwise, it waits indefinitely.
            %
            % messages, on return, is an Nx1 struct matrix, where N is the number of
            % messages received.  The following fields are defined in the struct:
            %
            %   channel : string type indicating the channel the message was received on
            %
            %   data : uint8 array containing the raw data bytes of the message
            %
            %   userdata : the string passed to lcm_subscribe for the subscription that
            %              caused this message to be received.
            if length(varargin) == 1
                obj = varargin{1};
                msgs=lcm_mex('get_messages', obj.mex);
            elseif length(varargin) == 2
                obj = varargin{1};
                msgs=lcm_mex('get_messages', obj.mex, varargin{2});
            else 
                error('Wrong number of input arguments');
            end
        end

    end
end
