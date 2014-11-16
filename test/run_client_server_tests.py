#!/usr/bin/env python
import os
import sys
import subprocess

def usage():
    print("Usage:  %s [options] [client]" % sys.argv[0])
    print("")
    print("Options:")
    print("  -h, --help    Show this help text")
    print("")

def main():
    to_test = {
        "C" : "c/client",
        "Python" : "python python/client.py",
        "C++" : "cpp/client",
        "Lua" : "cd lua; lua client.lua",
        "Java" : "cd java; java -cp lcmtest.jar LcmTestClient"
    }

    test_passed = {}

    # Start the test server
    if not os.path.exists("c/server"):
        print("Can't find test server c/server")
        print("Try running 'make' first")
        return 1
    print("Starting test server")
    server_proc = subprocess.Popen("c/server")

    # Run the client tests while the test server is running
    for name, prog in to_test.items():
        client_status = subprocess.Popen(prog, shell=True).wait()
        test_passed[name] = client_status == 0

    # Stop the test server
    print("Stopping test server")
    server_proc.terminate()
    server_status = server_proc.wait()
    print("Test server stopped")

    # Report
    print("")
    print("Test results:")
    for name in sorted(test_passed.keys()):
        if test_passed[name]:
            print("      OK  %s" % name)
        else:
            print(" FAIL     %s" % name)

    if all(test_passed.values()):
        return 0
    else:
        return 1

if __name__ == "__main__":
    sys.exit(main())
