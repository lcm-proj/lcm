% lcm = lcm_create()
%
% lcm = lcm_create(lcmurl)
%
% Initializes an LCM instance and returns a handle to that instance.
%
% When invoked with no arguments, the default LCM URL is used.
%
% When lcmurl is specified, this is passed directly to the C LCM API.

function lcm=lcm_create(varargin)

if length(varargin) == 0
    lcm = lcm_mex('create');
elseif length(varargin) == 1
    lcm = lcm_mex('create', varargin{1});
else 
    error('Too many arguments');
end
