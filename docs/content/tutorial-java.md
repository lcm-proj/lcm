Java Tutorial {#tut_java}
====

\brief Sending and receiving LCM messages with Java

# Introduction {#tut_java_intro}

This tutorial will show you how to use all of the core functionality of LCM,
and is intended for those who have a working knowledge of Java. For detailed
information about LCM, please see the [Java API reference](javadocs/index.html).

The topics covered in this tutorial are:

\li Initialize LCM in your application.
\li Publish a message.
\li Subscribe to and receive a message.

This tutorial uses the \p example_t message type defined in the
\ref tut_lcmgen "type definition tutorial", and assumes that you have
generated the Java bindings for the example type by running
\code
lcm-gen -j example_t.lcm
\endcode

After running this command, you should have one file,
<tt>exlcm/example_t.java</tt>.  This file is the Java binding for the example
message type.  If you have the time, take a moment to open up the
file and inspect the generated code.

You can then compile this into a .class file, and then create a .jar
archive.  Assuming that \c lcm.jar is in the current directory (see the \ref java_notes "Java notes" page), you
could then run (also from a command shell):

\code
javac -cp lcm.jar exlcm/*.java
jar cf my_types.jar exlcm/*.class
\endcode

You should then have a file \c my_types.jar, which is a Java archive containing
the Java bindings for the example message.  In order to use LCM types, you must
include the jar file on your classpath.
  
# Initializing LCM {#tut_java_initialize}
  
You will need to make sure that <em>lcm.jar</em> is in your classpath, and
your Java classes will need to include "import lcm.lcm.*".
  
To initialize LCM, with default options, simply call:
  
\code
LCM myLCM = LCM.getSingleton();
\endcode

The default options are suitable for communicating with other LCM applications
on the local computer.
For communication across computers, or other usages such as reading data from
an LCM logfile (e.g., to post-process or analyze previously collected data),
see the [Java API reference](javadocs/index.html).

# Publishing a message {#tut_java_publish}
  
We can instantiate and then publish some sample data as follows:
    
\code
import java.io.*;
import lcm.lcm.*;

public class SendMessage
{
    public static void main(String args[])
    {
        try {
            LCM lcm = LCM.getSingleton();

            exlcm.example_t msg = new exlcm.example_t();
            msg.timestamp = System.nanoTime();
            msg.position = new double[] { 1, 2, 3 };
            msg.orientation = new double[] { 1, 0, 0, 0 };
            msg.ranges = new short[] {
                0, 1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14
            };
            msg.num_ranges = msg.ranges.length;
            msg.name = "example string";
            msg.enabled = true;

            lcm.publish("EXAMPLE", msg);
        } catch (IOException ex) {
            System.out.println("Exception: " + ex);
        }
    }
}
\endcode
  
After initializing LCM, this application creates an instance of the example
message, fills in some message fields, and then publishes the message.

The call to \c lcm.publish() serializes the data into a byte stream and
transmits the packet using LCM to any interested receivers.  The string
<tt>"EXAMPLE"</tt> is the <em>channel</em> name, which is a string
transmitted with each packet that identifies the contents to receivers.
Receivers subscribe to different channels using this identifier, allowing
uninteresting data to be discarded quickly and efficiently.

 
# Subscribing to messages {#tut_java_subscribe}
  
In order to receive messages, you must implement an [LCMSubscriber](javadocs/lcm/lcm/LCMSubscriber.html) and pass it
to
[LCM.subscribe()](javadocs/lcm/lcm/LCM.html#subscribe%28java.lang.String, lcm.lcm.LCMSubscriber%29). The subscriber will be provided
with a LCMDataInputStream that can be read for the message contents. All LCM
data types include a constructor that takes a DataInput (including instances
of LCMDataInputStream) as an argument. First, let's look at the
subscriber:
  
\code
public class MySubscriber implements LCMSubscriber
{
    public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins)
    {
        System.out.println("Received message on channel " + channel);
        example_t msg;
        try {
            msg = new example_t(ins);
        } catch (IOException ex) {
            System.out.println("Error decoding message: " + ex);
            return;
        }

        System.out.println("  timestamp    = " + msg.timestamp);
        // Could do something else with the message here.
    }
}
\endcode

Next, we can subscribe to the message with:
  
\code
lcm.subscriber("EXAMPLE", new MySubscriber());
\endcode

The LCM instance has a background thread that constantly listens for messages.
When a message on channel <tt>"EXAMPLE"</tt> arrives, the LCM thread will
invoke the [messageReceived()](javadocs/lcm/lcm/LCMSubscriber.html#messageReceived%28lcm.lcm.LCM, java.lang.String, lcm.lcm.LCMDataInputStream%29) method.  

## Putting it all together {#tut_java_together}

Here's an example of a complete subscriber application:

\code
import java.io.*;

import lcm.lcm.*;
import exlcm.*;

public class MySubscriber implements LCMSubscriber
{
    LCM lcm;

    public MySubscriber()
        throws IOException
    {
        this.lcm = new LCM();
        this.lcm.subscribe("EXAMPLE", this);
    }

    public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins)
    {
        System.out.println("Received message on channel " + channel);

        try {
            if (channel.equals("EXAMPLE")) {
                example_t msg = new example_t(ins);

                System.out.println("  timestamp    = " + msg.timestamp);
                System.out.println("  position     = [ " + msg.position[0] +
                                   ", " + msg.position[1] + ", " + msg.position[2] + " ]");
                System.out.println("  orientation  = [ " + msg.orientation[0] +
                                   ", " + msg.orientation[1] +
                                   ", " + msg.orientation[2] +
                                   ", " + msg.orientation[3] + " ]");

                System.out.print("  ranges       = [ ");
                for (int i=0; i<msg.num_ranges; i++) {
                    System.out.print("" + msg.ranges[i]);
                    if (i < msg.num_ranges-1)
                        System.out.print (", ");
                }
                System.out.println (" ]");
                System.out.println("  name         = '" + msg.name + "'");
                System.out.println("  enabled      = '" + msg.enabled + "'");
            }

        } catch (IOException ex) {
            System.out.println("Exception: " + ex);
        }
    }

    public static void main(String args[])
    {
        try {
            MySubscriber m = new MySubscriber();
            while(true) {
                Thread.sleep(1000);
            }
        } catch (IOException ex) {
            System.out.println("Exception: " + ex);
        } catch (InterruptedException ex) { }
    }
}
\endcode

The full example is included in the LCM source distribution, in the
<tt>examples/java</tt> directory.

# Compiling and running {#tut_java_compiling_running}

To compile and run the examples, let's assume that the \c lcm.jar file is in the
current directory, along with \c SendMessage.java,  
\c MySubscriber.java, and \c example_t.lcm. We can run our programs by
executing the commands:
  
\verbatim
# 1. Create the Java implementation of temperature_t.lcm
lcm-gen -j example_t.lcm

# 2. Compile the demo applications and the LCM type created above.
javac -cp .:lcm.jar *.java exlcm/*.java

# 3. Run MySubscriber (in one terminal)
java -cp .:lcm.jar MySubscriber 

# 4. Run SendMessage (in another terminal)
java -cp .:lcm.jar SendMessage 
\endverbatim

# Additional notes {#tut_java_notes}

See the \ref java_notes "Java notes" page for some additional information
related to LCM development with Java.
