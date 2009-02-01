% lcm_publish(lcm, channel, data)
%
% publishes a message over LCM.
%
% lcm is an LCM handle as returned by lcm_create
%
% channel is a string specifying desired the LCM channel
%
% data is the byte buffer to publish.  It must be of type string, int8, or
% uint8

function lcm_publish(lcm, channel, data)
lcm_mex('publish', lcm, channel, data);
