% fd = lcm_get_fileno(lcm)
%
% Returns the file descriptor for the underlying LCM instance.  This can 
% be used for socket-based polling mechanisms.
function fd=lcm_get_fileno(lcm)
fd=lcm_mex('get_fileno', lcm);
