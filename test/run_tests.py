#!/usr/bin/env python

import sys
import subprocess
import getopt
import time

client = "c"
server = "c"

tests = { \
        "c" : ("c/server", "c/client"),
        "python" : (None, "python python/client.py"),
        "cpp" : ("cpp/server", "cpp/client"),
        }

def dotest(client_name, server_name):
    global tests
    server_args = tests[server_name][0]
    client_args = tests[client_name][1]
    if server_args is None:
        print("server test not defined for %s" % server_name)
        sys.exit(2)
    if client_args is None:
        print("client test not defined for %s" % client_name)
        sys.exit(2)
    server_proc = subprocess.Popen(server_args, shell=True)
    time.sleep(0.1)
    client_proc = subprocess.Popen(client_args, shell=True)

    server_status = server_proc.wait()
    client_status = client_proc.wait()
    print("server %s returned: %d" % (server_name, server_status))
    print("client %s returned: %d" % (client_name, client_status))
    print
    
def usage():
    print("Usage:  %s [options] [client] [server]" % sys.argv[0])
    print("")
#    print("  client and server can be one of:")
#    print("     c")
#    print("")
    print("Options:")
    print("  -h, --help    Show this help text")
    print("")

try:
    opts, args = getopt.getopt(sys.argv[1:], 'h', ['help'])
except getopt.GetoptError:
    usage()
    sys.exit(2)

for o, a in opts:
    if o in ["-h", "--help"]:
        usage()

#server = "c"
#client = "c"
#
#if len(args) > 0):
#
#print "remaining args: %s" % args

dotest("c", "c")
dotest("python", "c")
dotest("cpp", "c")