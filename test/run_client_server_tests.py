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
        "C" : os.path.join("c", "client"),
        "Python" : "python " + os.path.join("python", "client.py"),
        "C++" : os.path.join("cpp", "client"),
        "Lua" : "cd lua; lua client.lua",
        "Java" : "cd java; java -cp lcmtest.jar LcmTestClient"
    }

    test_passed = {}

    # Start the test server
    serverName = os.path.join("c", "server")
    if os.name == "nt":
        serverName += ".exe"

    if not os.path.exists(serverName):
        print("Can't find test server %s" % serverName)
        print("Try running 'make' first")
        return 1

    print("Starting test server")
    server_proc = subprocess.Popen(serverName)

    # Run the client tests while the test server is running
    for name, prog in to_test.items():
        client_status = subprocess.Popen(prog, shell=True).wait()
        if client_status == 0:
            test_passed[name] = "     OK"
        else:
            test_passed[name] = " FAIL  "

    # Stop the test server
    print("Stopping test server")
    server_proc.terminate()
    server_status = server_proc.wait()
    print("Test server stopped")

    # Report
    print("")
    print("Test results:")
    for name, status in test_passed.items():
        print("%s  %s" % (status, name))

    if all(test_passed.values()):
        return 0
    else:
        return 1

if __name__ == "__main__":
    sys.exit(main())
