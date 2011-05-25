import sys
import lcm

from exlcm import example_t

if len(sys.argv) < 2:
    sys.stderr.write("usage: read-log <logfile>\n")
    sys.exit(1)

log = lcm.EventLog(sys.argv[1], "r")

for event in log:
    if event.channel == "EXAMPLE":
        msg = example_t.decode(event.data)

        print("Message:")
        print("   timestamp   = %s" % str(msg.timestamp))
        print("   position    = %s" % str(msg.position))
        print("   orientation = %s" % str(msg.orientation))
        print("   ranges: %s" % str(msg.ranges))
        print("")
