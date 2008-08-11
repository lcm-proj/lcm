import lcm

from example_t import example_t

def my_handler(channel, data):
    msg = example_t.decode(data)
    print("Received message on channel \"%s\"" % channel)
    print("   timestamp   = %s" % msg.timestamp)
    print("   position    = %s" % msg.position)
    print("   orientation = %s" % msg.orientation)
    print("   ranges: %s" % msg.ranges)
    print("")

lc = lcm.LCM()
subscription = lc.subscribe("EXAMPLE", my_handler)

try:
    while True:
        lc.handle()
except KeyboardInterrupt:
    pass

lc.unsubscribe(subscription)
