#!/usr/bin/env python
import os
import sys
import subprocess
import time

def main(server, *client):
    # Start the test server
    print("Starting test server")
    server_proc = subprocess.Popen(server)

    # Kludge. Wait for server to start.
    time.sleep(1)

    # Run the client tests while the test server is running
    print("Starting test client")
    test_result = subprocess.call(client)

    # Stop the test server
    print("Stopping test server")
    server_proc.terminate()
    server_status = server_proc.wait()
    print("Test server stopped")

    # Report
    return test_result

if __name__ == "__main__":
    sys.exit(main(*sys.argv[1:]))
