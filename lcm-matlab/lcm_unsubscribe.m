% lcm_unsubscribe(lcm, subs)
%
% Unsubscribes from one or more channels.
%
% lcm must be an LCM handle as returned by lcm_create
%
% subs must be a subscription handle as returned by lcm_subscribe

function lcm_unsubscribe(lcm, subs)
lcm_mex('unsubscribe', lcm, subs);
