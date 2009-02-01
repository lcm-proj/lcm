% messages = lcm_get_messages(lcm)
%
% messages = lcm_get_messages(lcm, timeout_ms)
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

function msgs=lcm_get_messages(varargin)
if length(varargin) == 1
    msgs=lcm_mex('get_messages', varargin{1});
elseif length(varargin) == 2
    msgs=lcm_mex('get_messages', varargin{1}, varargin{2});
else 
    error('Wrong number of input arguments');
end

