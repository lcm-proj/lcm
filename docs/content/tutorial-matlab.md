# MATLAB Tutorial

Sending and receiving LCM messages with MATLAB

## Introduction

This tutorial will walk you through the main tasks for exchanging LCM messages
using the MATLAB API.  The topics covered
in this tutorial are:

- Setting up MATLAB to use LCM
- Initialize LCM in your application.
- Publish a message.
- Subscribe to and receive a message.

Using LCM with MATLAB is almost identical to using LCM through Java, since the
MATLAB API relies on the LCM Java bindings.  However, there are some
differences, which mainly consist of how incoming messages are handled.

## Setting up MATLAB to use LCM

This tutorial uses the `example_t` message type defined in the
[type definition tutorial](tutorial-lcmgen.md), and assumes that you have
compiled `lcm.jar` (see the [Java notes](java-notes.md) page), and 
generated the Java bindings for the example type by running the following 
(from a command shell, not the MATLAB prompt):
``` 
lcm-gen -j example_t.lcm
``` 

After running this command, you should have a file named
`exlcm/example_t.java`.
You can then compile this into a .class file, and then create a .jar
archive.  Assuming that `lcm.jar` is in the current directory, you
could then run (also from a command shell):

``` 
javac -cp lcm.jar exlcm/*.java
jar cf my_types.jar exlcm/*.class
``` 

You should then have a file `my_types.jar`, which is a Java archive containing
the Java bindings for the example message.

```{note}
To use `my_types.jar` in MATLAB, you must make sure the Java version used by
MATLAB is equal or greater than the version used to build `my_types.jar`.
To check your system Java version, type `java -version` on the command line.
To check the version used by MATLAB, type `version -java` in a MATLAB terminal. 
If the MATLAB version is *older*, you will either need to rebuild the JAR with an
older version of Java, or configure MATLAB to use a newer version.

By default, MATLAB is bundled with Java 8. If you have Java 9 or later, you can
target Java 8 by compiling `example_t.java` via `javac -cp lcm.jar exlcm/*.java --release 8`.
```

```{note}
On Windows, use `\` instead of `/` for directory separators.
```

The next task is to tell MATLAB how to find LCM and the message bindings.  You
will first need to add `lcm.jar` and `my_types.jar` into the MATLAB
classpath.  You can do this from the MATLAB prompt or a MATLAB script by
running:

``` 
javaaddpath lcm.jar
javaaddpath my_types.jar
``` 

Once this is all setup, you can start using LCM from your MATLAB scripts.

## Initializing LCM

You can initialize LCM from MATLAB as follows:

``` 
lc = lcm.lcm.LCM.getSingleton();
``` 

The `lc` object now contains the communications context and interface for LCM.
Since we're simply calling into the Java API here, see the <a href="../javadocs/index.html">Java API reference</a> for other ways on setting up LCM.

## Publishing a message

We can instantiate and publish some sample data as follows:
    
``` 
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
``` 

For the most part, this example should be pretty straightforward.  The
application initializes LCM, creates a message, fills in the message data
fields, then publishes the message using `lc.publish()`.

The call to `lc.publish` serializes the data into a byte stream and
transmits the packet to any interested receivers.  The string
<tt>'EXAMPLE'</tt> is the <em>channel</em> name, which is a string
transmitted with each packet that identifies the contents to receivers.
Receivers subscribe to different channels using this identifier, allowing
uninteresting data to be discarded quickly and efficiently.

The full example is available in runnable form in the 
<tt>examples/matlab</tt> directory in the LCM source distribution.

## Receiving LCM Messages

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

``` 
lc = lcm.lcm.LCM.getSingleton();
aggregator = lcm.lcm.MessageAggregator();

lc.subscribe('EXAMPLE', aggregator);

while true
    disp waiting
    millis_to_wait = 1000;
    msg = aggregator.getNextMessage(millis_to_wait);
    if ~isempty(msg)
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
disp([ 'name:        ' sprintf('%s ', m.name ])
disp([ 'enabled:     ' sprintf('%d ', m.enabled) ])
``` 

After initializing the LCM object, the application creates a <a href="../javadocs/lcm/lcm/MessageAggregator.html">MessageAggregator</a> and subscribes it to the <tt>'EXAMPLE'</tt> channel.
The sole purpose of the message aggregator is to receive messages
on that channel and queue them up for later handling.  This is different from
how messages would normally be handled in Java, as MATLAB is not well suited to
callback functions or multiple threads.

The example application then repeatedly calls 
<a href="../javadocs/lcm/lcm/MessageAggregator.html#getNextMessage-long-">MessageAggregatorgetNextMessage()</a>
which simply waits for a message of interest to arrive and then returns the raw
data.

Once a message has arrived, the application decodes it by passing `msg.data`
to the constructor for the example message type, and displays some message
fields.

For more information on how to use the
<a href="../javadocs/lcm/lcm/MessageAggregator.html">MessageAggregator</a>,
see the API documentation.  This includes configuring messsaging limits such as
a maximum number of messages to queue up to avoid falling behind real-time.

The full example is available in runnable form in the
<tt>examples/matlab</tt> directory in the LCM source distribution.
