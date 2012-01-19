#!/usr/bin/env python

import sys
import subprocess
import getopt
import time

client = "c"
server = "c"

def dotest(client_dir, server_dir):
    server_args = [ "%s/server" % server_dir ]
    client_args = [ "%s/client" % client_dir ]
    server_proc = subprocess.Popen(server_args)
    time.sleep(0.1)
    client_proc = subprocess.Popen(client_args)

    server_status = server_proc.wait()
    client_status = client_proc.wait()
    print("server: %d" % server_status)
    print("client: %d" % client_status)

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
