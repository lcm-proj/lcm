# This example demonstrates how to use LCM with the Python select module

import select
import lcm
from exlcm import example_t

def my_handler(channel, data):
    msg = example_t.decode(data)
    print("Received message on channel \"%s\"" % channel)
    print("   timestamp   = %s" % str(msg.timestamp))
    print("   position    = %s" % str(msg.position))
    print("   orientation = %s" % str(msg.orientation))
    print("   ranges: %s" % str(msg.ranges))
    print("   name        = '%s'" % msg.name)
    print("   enabled     = %s" % str(msg.enabled))
    print("")

lc = lcm.LCM()
lc.subscribe("EXAMPLE", my_handler)

try:
    timeout = 1.5  # amount of time to wait, in seconds
    while True:
        rfds, wfds, efds = select.select([lc.fileno()], [], [], timeout)
        if rfds:
            lc.handle()
        else:
            print("Waiting for message...")
except KeyboardInterrupt:
    pass
