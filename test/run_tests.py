#!/usr/bin/env python

import sys
import subprocess
import getopt
import time

client = "c"

tests = { \
        "c" : "c/client",
        "python" : "python python/client.py",
        "cpp" : "cpp/client",
        }

def dotest(client_name):
    global tests
    server_args = "c/server"
    client_args = tests[client_name]
    if client_args is None:
        print("client test not defined for %s" % client_name)
        sys.exit(2)
    server_proc = subprocess.Popen(server_args, shell=True)
    time.sleep(0.1)
    client_proc = subprocess.Popen(client_args, shell=True)

    client_status = client_proc.wait()
    server_proc.terminate()
    server_status = server_proc.wait()
    print("server returned: %d" % (server_status))
    print("client %s returned: %d" % (client_name, client_status))
    print

def usage():
    print("Usage:  %s [options] [client]" % sys.argv[0])
    print("")
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

dotest("c")
dotest("python")
dotest("cpp")

print("Running C++ tests")
subprocess.check_call("cpp/memq_test", shell=True)

# Call the lcm_file_test independantly
print("Running Python tests.")
subprocess.check_call("python/bool_test.py", shell=True)
subprocess.check_call("python/byte_array_test.py", shell=True)
subprocess.check_call("python/lcm_file_test.py", shell=True)
subprocess.check_call("python/lcm_memq_test.py", shell=True)
subprocess.check_call("python/lcm_thread_test.py", shell=True)
