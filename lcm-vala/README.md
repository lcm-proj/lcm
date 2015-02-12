LCM VALA bindings
=================

All what you need is content of vapi dir and `lcm_message.vala`.

I don't know well autotools, and my attempt fail.
Proper way is make `lcm-vala` library with message encoding routines
from `lcm_message.vala`. But my attempt end with vapi without `Lcm.CoreTypes`.

I think easiest way is to create library with messages and include `lcm_messages.vala`
here. Although `vapi` provide direct bindings to C library.

Some examples can be found in `test/vala`.
Current binding not complete, and don't have much tests,
but it already can send ad receive messages.

-- Vladimri Ermakov <vooon341@gmail.com>
