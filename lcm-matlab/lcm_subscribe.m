% subs = lcm_subscribe(lcm, channel)
%
% subs = lcm_subscribe(lcm, channel, userdata)
%
% Subscribes to an LCM channel.
%
% lcm is an LCM handle as returned by lcm_create
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
%  lcm_subscribe(lcm, "POSE");
%
%     subscribes to the "POSE" channel
%
%  lcm_subscribe(lcm, "RADAR.*");
%
%     subscribes to all channels that start with "RADAR"
%
%  lcm_subscribe(lcm, "ABC|DEF|GHI");
%
%     subscribes to the channels ABC, DEF, and GHI

function subs=lcm_subscribe(varargin)

if nargin < 2
    error('Not enough arguments');
elseif nargin == 2
    lcm = varargin{1};
    channel = varargin{2};
    userdata = '';
    subs = lcm_mex('subscribe', lcm, channel, userdata);
elseif nargin == 3
    lcm = varargin{1};
    channel = varargin{2};
    userdata = varargin{3};
    subs = lcm_mex('subscribe', lcm, channel, userdata);
else
    error('Too many arguments');
end
