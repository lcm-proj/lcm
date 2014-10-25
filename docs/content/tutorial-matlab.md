MATLAB Tutorial {#tut_matlab}
====
\brief Sending and receiving LCM messages with MATLAB

# Introduction {#tut_matlab_intro}

This tutorial will walk you through the main tasks for exchanging LCM messages
using the MATLAB API.  The topics covered
in this tutorial are:

\li Setting up MATLAB to use LCM
\li Initialize LCM in your application.
\li Publish a message.
\li Subscribe to and receive a message.

Using LCM with MATLAB is almost identical to using LCM through Java, since the
MATLAB API relies on the LCM Java bindings.  However, there are some
differences, which mainly consist of how incoming messages are handled.

# Setting up MATLAB to use LCM {#tut_matlab_setup}

This tutorial uses the \p example_t message type defined in the
\ref tut_lcmgen "type definition tutorial", and assumes that you have
compiled \c lcm.jar (see the \ref java_notes "Java notes" page), and 
generated the Java bindings for the example type by running the following 
(from a command shell, not the MATLAB prompt):
\code
lcm-gen -j example_t.lcm
\endcode

After running this command, you should have a file named \c
exlcm/example_t.java.
You can then compile this into a .class file, and then create a .jar
archive.  Assuming that \c lcm.jar is in the current directory, you
could then run (also from a command shell):

\code
javac -cp lcm.jar exlcm/*.java
jar cf my_types.jar exlcm/*.class
\endcode

You should then have a file \c my_types.jar, which is a Java archive containing
the Java bindings for the example message.

\note on Windows, use '\' instead of '/' for directory separators.

The next task is to tell MATLAB how to find LCM and the message bindings.  You
will first need to add \c lcm.jar and \c my_types.jar into the MATLAB
classpath.  You can do this from the MATLAB prompt or a MATLAB script by
running:

\code
javaaddpath lcm.jar
javaaddpath my_types.jar
\endcode

Once this is all setup, you can start using LCM from your MATLAB scripts.

# Initializing LCM {#tut_matlab_initialize}

You can initialize LCM from MATLAB as follows:

\code
lc = lcm.lcm.LCM.getSingleton();
\endcode

The \c lc object now contains the communications context and interface for LCM.
Since we're simply calling into the Java API here, see the [Java API documentation](javadocs/index.html) for other ways on setting up LCM.

# Publishing a message {#tut_matlab_publishing}

We can instantiate and publish some sample data as follows:
    
\code
lc = lcm.lcm.LCM.getSingleton();

msg = exlcm.example_t();
msg.timestamp = 0;
msg.position = [1  2  3];
msg.orientation = [1 0 0 0];
msg.ranges = 1:15;
msg.num_ranges = length(msg.ranges);
msg.name = 'example string';
msg.enabled = 1;

lc.publish('EXAMPLE', msg);
\endcode

For the most part, this example should be pretty straightforward.  The
application initializes LCM, creates a message, fills in the message data
fields, then publishes the message using \c lc.publish().

The call to \c lc.publish serializes the data into a byte stream and
transmits the packet to any interested receivers.  The string
<tt>'EXAMPLE'</tt> is the <em>channel</em> name, which is a string
transmitted with each packet that identifies the contents to receivers.
Receivers subscribe to different channels using this identifier, allowing
uninteresting data to be discarded quickly and efficiently.

The full example is available in runnable form in the 
<tt>examples/matlab</tt> directory in the LCM source distribution.

# Receiving LCM Messages {#tut_matlab_receive}

As discussed above, each LCM message is transmitted with an attached channel
name.  You can use these channel names to determine which LCM messages your
application receives, by subscribing to the channels of interest.  It is
important for senders and receivers to agree on the channel names which will
be used for each message type.

Here is a sample program that sets up LCM and adds a subscription to the
<tt>'EXAMPLE'</tt> channel.  Whenever a message is received on this
channel, its contents are printed out.  If messages on other channels are
being transmitted over the network, this program will not see them because it
only has a subscription to the <tt>'EXAMPLE'</tt> channel.  A
particular instance of LCM may have an unlimited number of subscriptions.

\code
lc = lcm.lcm.LCM.getSingleton();
aggregator = lcm.lcm.MessageAggregator();

lc.subscribe('EXAMPLE', aggregator);

while true
    disp waiting
    millis_to_wait = 1000;
    msg = aggregator.getNextMessage(millis_to_wait);
    if length(msg) > 0
        break
    end
end

disp(sprintf('channel of received message: %s', char(msg.channel)))
disp(sprintf('raw bytes of received message:'))
disp(sprintf('%d ', msg.data'))

m = exlcm.example_t(msg.data);

disp(sprintf('decoded message:\n'))
disp([ 'timestamp:   ' sprintf('%d ', m.timestamp) ])
disp([ 'position:    ' sprintf('%f ', m.position) ])
disp([ 'orientation: ' sprintf('%f ', m.orientation) ])
disp([ 'ranges:      ' sprintf('%f ', m.ranges) ])
disp([ 'name:        ' m.name) ])
disp([ 'enabled:     ' sprintf('%d ', m.enabled) ])
\endcode

After initializing the LCM object, the application creates a [MessageAggregator](javadocs/lcm/lcm/MessageAggregator.html) and subscribes it to the <tt>'EXAMPLE'</tt> channel.
The sole purpose of the message aggregator is to receive messages
on that channel and queue them up for later handling.  This is different from
how messages would normally be handled in Java, as MATLAB is not well suited to
callback functions or multiple threads.

The example application then repeatedly calls 
[MessageAggregator.getNextMessage()](javadocs/lcm/lcm/MessageAggregator.html#getNextMessage%28%29)
which simply waits for a message of interest to arrive and then returns the raw
data.

Once a message has arrived, the application decodes it by passing \c msg.data
to the constructor for the example message type, and displays some message
fields.

For more information on how to use the
[MessageAggregator](javadocs/lcm/lcm/MessageAggregator.html),
see the API documentation.  This includes configuring messsaging limits such as
a maximum number of messages to queue up to avoid falling behind real-time.

The full example is available in runnable form in the
<tt>examples/matlab</tt> directory in the LCM source distribution.
