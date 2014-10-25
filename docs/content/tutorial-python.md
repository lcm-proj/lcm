Python Tutorial {#tut_python}
====
\brief Sending and receiving LCM messages with Python

# Introduction {#tut_python_intro}

This tutorial will walk you through the main tasks for exchanging LCM messages
using the Python API.  The topics covered
in this tutorial are:

\li Initialize LCM in your application.
\li Publish a message.
\li Subscribe to and receive a message.

This tutorial uses the \p example_t message type defined in the
\ref tut_lcmgen "type definition tutorial", and assumes that you have
generated the Python bindings for the example type by running
\code
lcm-gen -p example_t.lcm
\endcode

After running this command, you should have the following files:
\code
exlcm/example_t.py
exlcm/__init__.py
\endcode

The first file contains the Python bindings for the \c example_t message type,
and the \c __init__.py file sets up a Python package.  If you have the time,
take a moment to open up the files and inspect the generated
code.  Note that if \c exlcm/__init__.py already existed, then it will be
appended to if necessary, and the existing contents left otherwise untouched.

# Initializing LCM {#tut_python_initialize}

The first task for any application that uses LCM is to initialize the library.
In Python, this is very straightforward:

\code
import lcm

lc = lcm.LCM()
\endcode

The primary communications functionality is contained in the [lcm.LCM](python/lcm.LCM-class.html) class.
The constructor initializes communications resources, and has a single optional
argument.
If no argument is given, as above, then the LCM instance is initialized to
reasonable defaults, which are suitable for communicating with other LCM
applications on the local computer.  The argument can also be a string
specifying the underlying communications mechanisms.  The LCM Python class
itself is a wrapper around the C LCM library, so for information on setting the
class up for communication across computers, or other usages such as reading
data from an LCM logfile (e.g., to post-process or analyze previously collected
data), see the documentation for \ref lcm_create().

If an error occurs initializing LCM, then an IOError is raised.

# Publishing a message {#tut_python_publishing}

When you create an LCM data type and generate Python code with <tt>lcm-gen</tt>,
that data type will then be available as a Python class with the same name.  For
<tt>example_t</tt>, the Python class that gets generated looks like this:
    
\code
class example_t(object):
    def __init__(self):
        self.timestamp = 0
        self.position = [ 0.0 for dim0 in range(3) ]
        self.orientation = [ 0.0 for dim0 in range(4) ]
        self.num_ranges = 0
        self.ranges = []
        self.name = ""
        self.enabled = False
\endcode

Notice here that fixed-length arrays in LCM appear as Python lists initialized
to the appropriate length, and variable length arrays start off as empty lists.
All other fields are initialized to some reasonable defaults.
    
We can instantiate and then publish some sample data as follows:
    
\code
import lcm
from exlcm import example_t

msg = example_t()
msg.timestamp = 0
msg.position = (1, 2, 3)
msg.orientation = (1, 0, 0, 0)
msg.ranges = range(15)
msg.num_ranges = len(msg.ranges)
msg.name = "example string"
msg.enabled = True

lc = lcm.LCM()
lc.publish("EXAMPLE", msg.encode())
\endcode

The full example is available in runnable form as
<tt>examples/python/send_message.py</tt> in the LCM source distribution.

For the most part, this example should be pretty straightforward.  The
application creates a message, fills in the message data fields, then
initializes LCM and publishes the message.

The call to [lcm.publish()](python/lcm.LCM-class.html#publish) serializes the data into a byte stream and
transmits the packet to any interested receivers.  The string
<tt>"EXAMPLE"</tt> is the <em>channel</em> name, which is a string
transmitted with each packet that identifies the contents to receivers.
Receivers subscribe to different channels using this identifier, allowing
uninteresting data to be discarded quickly and efficiently.

# Receiving LCM Messages {#tut_python_receive}

As discussed above, each LCM message is transmitted with an attached channel
name.  You can use these channel names to determine which LCM messages your
application receives, by subscribing to the channels of interest.  It is
important for senders and receivers to agree on the channel names which will
be used for each message type.

Here is a sample program that sets up LCM and adds a subscription to the
<tt>"EXAMPLE"</tt> channel.  Whenever a message is received on this
channel, its contents are printed out.  If messages on other channels are
being transmitted over the network, this program will not see them because it
only has a subscription to the <tt>"EXAMPLE"</tt> channel.  A
particular instance of LCM may have an unlimited number of subscriptions.

\code
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
subscription = lc.subscribe("EXAMPLE", my_handler)

try:
    while True:
        lc.handle()
except KeyboardInterrupt:
    pass
\endcode

The full example is available in runnable form as
<tt>examples/python/listener.py</tt> in the LCM source distribution.

After initializing the LCM object, the application subscribes to a channel by
passing a callback function to the [lcm.subscribe](python/lcm.LCM-class.html#subscribe)
method.  

The example application then repeatedly calls 
[lcm.handle](python/lcm.LCM-class.html#handle),
which simply waits for a message of interest to arrive and then invokes the
appropriate callback functions.
Callbacks are invoked one at a time, so there is no need for thread
synchronization.

If your application has other work to do while waiting for messages (e.g.,
print out a message every few seconds or check for input somewhere else), you
can use the [lcm.fileno](python/lcm.LCM-class.html#handle)
method to obtain a file descriptor.  This file descriptor can then be used in
conjunction with the Python select module or some other event loop to check
when LCM messages have arrived.  See
<tt>examples/python/listener_select.py</tt> in the LCM source distribution for
an example.
