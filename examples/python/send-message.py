import lcm

from example_t import example_t

lc = lcm.LCM ("udpm://?transmit_only=true")

msg = example_t ()
msg.timestamp = 0
msg.position = (1, 2, 3)
msg.orientation = (1, 0, 0, 0)
msg.ranges = range (15)
msg.num_ranges = len (msg.ranges)

lc.publish ("EXAMPLE", msg.encode())
