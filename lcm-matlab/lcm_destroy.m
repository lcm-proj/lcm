% lcm_destroy(lcm)
%
% Releases all memory and resources consumed by an LCM instance.  Call this
% function when the LCM instance is no longer needed.

function lcm_destroy(lcm)
lcm_mex('destroy', lcm)
