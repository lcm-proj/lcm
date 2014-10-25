.NET Tutorial {#tut_dotnet}
====
\brief An example use case in C#.NET

# Introduction {#tut_dotnet_intro}

This tutorial will guide you through the basics of using LCM .NET port. As
the .NET port is basically a transcription of the original Java library, it
tries to be functionally equivalent while maintaining C#.NET naming
conventions and other platform specifics. All sample code is written in C# (as
well as the port itself), but the principles are applicable to any of the
languages supported by the .NET Framework.
    
The tutorial doesn't cover the very basics of the LCM (message transmision
principles, message definition format etc.) - please see the rest of the
documentation before further reading. 
    
\section Generating C#.NET-specific message files
  
To demonstrate basic functionality, this tutorial will use the same message
format and application logic as the \ref tut_java to accent similarities and
differences between Java and .NET ports. Let's have the following type
specification, saved to a file named \p temperature_t.lcm:
  
\code
package exlcm;

struct example_t
{
    int64_t  timestamp;
    double   position[3];
    double   orientation[4]; 
    int32_t  num_ranges;
    int16_t  ranges[num_ranges];
    string   name;
    boolean  enabled;
}
\endcode

In order to obtain C#.NET-specific handler class, we need to call \p lcm-gen with
the \p --csharp flag:
\code
lcm-gen --csharp example_t.lcm
\endcode

Besides, the \p lcm-gen utility accepts the following .NET-specific options:

<table>
    <tr><th>Option</th><th>Default value</th><th>Description</th></tr>
    <tr><td>--csharp-path</td><td></td><td>C#.NET file destination
        directory</td></tr>
    <tr><td>--csharp-mkdir</td><td>1</td><td>Make C#.NET source
        directories automatically</td></tr>
    <tr><td>--csharp-strip-dirs</td><td>0</td><td>Do not generate folders
        for default and root namespace - unlike Java sources, the .NET source files'
        directory structure does not have to be analogic to their namespace structure.
        It is often advantageous to omit the top-most directories common to all
        generated files to simplify the directory layout.</td></tr>
    <tr><td>--csharp-decl</td><td>:&nbsp;LCM.LCM.LCMEncodable</td><td>String
        added to class declarations - similar to Java option \p --jdecl</td></tr>
    <tr><td>--csharp-root-nsp</td><td></td><td>Root C#.NET namespace
        (wrapper) added before LCM package name - this comes handy when you
        want to place generated .NET bindings into a specific part of your .NET
        namespace hierarchy and do not want to affect other languages by embedding
        the wrapper namespace directly into the source \p .lcm files</td></tr>
    <tr><td>--csharp-default-nsp</td><td>LCMTypes</td><td>Default NET
        namespace if the LCM type has no package defined</td></tr>
</table>
  
As with all tutorials, we will publish and subscribe to the "EXAMPLE" channel.
  
# Initializing LCM {#tut_dotnet_initialize}
  
There are at least two ways how to use the .NET port of LCM:
  
\li if you don't want to modify the library and just use it, the simplest way
is to build the library and copy resulting lcm.dll to your application; you
then need to add a reference to it (Project -> Add Reference... -> Browse) and
you are ready to start communicating!
\li if you plan to do some changes to library source code, the recommended way
is to add library Visual Studio project to your solution and reference it
(Project -> Add Reference...  -> Projects)
  
Main classes of the library are put in the LCM.LCM namespace (while helper
code is in LCM.Util). This results in quite funny fully qualified name of the
master class - LCM.LCM.LCM (its constructor is even funnier -
LCM.LCM.LCM.LCM() :-) ).  It's logical to use the \p 'using LCM.LCM' statement to
shorten calls to the library, but the naming scheme (chosen to agree with the
Java variant) makes it a little difficult - you cannot use bare 'LCM' as class
name - the compiler considers it to be the namespace. Instead, you need to
write LCM.LCM to denote the main class.
  
Generated message handlers are placed in the \p LCMTypes namespace by default
(you can change this by specifying lcm-gen option \p --csharp-default-nsp).
  
LCM itself has a mechanism to maintain single instance of the main class -
static property LCM.Singleton:
  
\code
LCM.LCM myLCM = LCM.LCM.Singleton;
\endcode

You can also instantiate the class and take care of the object's singularity
by yourself:
  
\code
LCM.LCM myLCM = new LCM.LCM();
\endcode

In situations where the default connection URL (fetched by LCM from the
environment variable \p LCM_DEFAULT_URL or defined by the constant
\p "udpm://239.255.76.67:7667" when the former is empty) is not suitable,
the constructor can accept variable number of parameters specifying individual
connection strings.

For detailed information on the LCM .NET API please see the
[.NET API reference](lcm-dotnet/index.html).

# Publishing a message {#tut_dotnet_publish}
  
In order to use LCM types, you can either build an assembly containing
generated classes (needed when using LCM from other .NET language then C#), or
include the classes directly to application project.  Utilization of the
generated classes is then fairly straightforward:
  
\code
exlcm.example_t msg = new exlcm.example_t();
TimeSpan span = DateTime.Now - new DateTime(1970, 1, 1);
msg.timestamp = span.Ticks * 100;
msg.position = new double[] { 1, 2, 3 };
msg.orientation = new double[] { 1, 0, 0, 0 };
msg.ranges = new short[] { 0, 1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 };
msg.num_ranges = msg.ranges.Length;
msg.name = "example string";
msg.enabled = true;

myLCM.Publish("EXAMPLE", msg);
\endcode

The data are simply assigned to appropriate fields inside the message container.
Passing the message object to the \p Publish method of the LCM object places
it to specified channel of the communication bus (channel "EXAMPLE" here).

# Subscribing to messages {#tut_dotnet_subscribe}
  
In order to receive messages, you have two options:
  
\li write a class implementing the LCMSubscriber interface (just one
    handler-method MessageReceived) to assynchronously handle incoming
messages
\li use standard class MessageAggregator (that internally implements
    LCMSubscriber interface) for synchronous blocking or not blocking
     message delivery
      
This tutorial exploits the former option - the class \p SimpleSubscriber is defined
as internal inside the demo application class:
  
\code
internal class SimpleSubscriber : LCM.LCMSubscriber
{
    public void MessageReceived(LCM.LCM lcm, string channel, LCM.LCMDataInputStream dins)
    {
        Console.WriteLine("RECV: " + channel);

        if (channel == "EXAMPLE")
        {
            exlcm.example_t msg = new exlcm.example_t(dins);

            Console.WriteLine("Received message of the type example_t:");
            Console.WriteLine("  timestamp   = {0:D}", msg.timestamp);
            Console.WriteLine("  position    = ({0:N}, {1:N}, {2:N})",
                    msg.position[0], msg.position[1], msg.position[2]);
            Console.WriteLine("  orientation = ({0:N}, {1:N}, {2:N}, {3:N})",
                    msg.orientation[0], msg.orientation[1], msg.orientation[2],
                    msg.orientation[3]);
            Console.Write("  ranges      = [ ");
            for (int i = 0; i < msg.num_ranges; i++)
            {
                Console.Write(" {0:D}", msg.ranges[i]);
                if (i < msg.num_ranges-1)
                    Console.Write(", ");
            }
            Console.WriteLine(" ]");
            Console.WriteLine("  name         = '" + msg.name + "'");
            Console.WriteLine("  enabled      = '" + msg.enabled + "'");
        }
    }
}
\endcode

The class instance is then passed to LCM method \p SubscribeAll that passes all
received messages to our subscriber class. When selective subscription is needed
(i.e. in almost all real-world cases as we usually don't to listen to all channels),
method \p Subscribe that takes the channel name pattern as an argument is to
be used.
  
\code
myLCM.SubscribeAll(new SimpleSubscriber());
\endcode

# Putting it all together {#tut_dotnet_together}

Distribution of the LCM library includes a directory of examples.  One of them
is a couple of programs implementing all described features. Please go to
\p examples/csharp/ to find Visual Studio solution ready to be built.

The complete example transmitter application:
  
\code
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using LCM;

namespace LCM.Examples
{
    /// <summary>
    /// Demo transmitter, see LCM .NET tutorial for more information
    /// </summary>
    class ExampleTransmit
    {
        public static void Main(string[] args)
        {
            try
            {
                LCM.LCM myLCM = LCM.LCM.Singleton;

                exlcm.example_t msg = new exlcm.example_t();
                TimeSpan span = DateTime.Now - new DateTime(1970, 1, 1);
                msg.timestamp = span.Ticks * 100;
                msg.position = new double[] { 1, 2, 3 };
                msg.orientation = new double[] { 1, 0, 0, 0 };
                msg.ranges = new short[] { 0, 1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 };
                msg.num_ranges = msg.ranges.Length;
                msg.name = "example string";
                msg.enabled = true;

                myLCM.Publish("EXAMPLE", msg);
            }
            catch (Exception ex)
            {
                Console.Error.WriteLine("Ex: " + ex);
            }
        }
    }
}
\endcode

The complete example receiver application:

\code
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using LCM;

namespace LCM.Examples
{
    /// <summary>
    /// Demo listener, demonstrating interoperability with other implementations
    /// Just run this listener and use any of the example_t message senders
    /// </summary>
    class ExampleDisplay
    {
        public static void Main(string[] args)
        {
            LCM.LCM myLCM;

            try
            {
                myLCM = new LCM.LCM();

                myLCM.SubscribeAll(new SimpleSubscriber());

                while (true)
                    System.Threading.Thread.Sleep(1000);
            }
            catch (Exception ex)
            {
                Console.Error.WriteLine("Ex: " + ex);
                Environment.Exit(1);
            }
        }

        internal class SimpleSubscriber : LCM.LCMSubscriber
        {
            public void MessageReceived(LCM.LCM lcm, string channel, LCM.LCMDataInputStream dins)
            {
                Console.WriteLine("RECV: " + channel);

                if (channel == "EXAMPLE")
                {
                    exlcm.example_t msg = new exlcm.example_t(dins);

                    Console.WriteLine("Received message of the type example_t:");
                    Console.WriteLine("  timestamp   = {0:D}", msg.timestamp);
                    Console.WriteLine("  position    = ({0:N}, {1:N}, {2:N})",
                            msg.position[0], msg.position[1], msg.position[2]);
                    Console.WriteLine("  orientation = ({0:N}, {1:N}, {2:N}, {3:N})",
                            msg.orientation[0], msg.orientation[1], msg.orientation[2],
                            msg.orientation[3]);
                    Console.Write("  ranges      = [ ");
                    for (int i = 0; i < msg.num_ranges; i++)
                    {
                        Console.Write(" {0:D}", msg.ranges[i]);
                        if (i < msg.num_ranges-1)
                            Console.Write(", ");
                    }
                    Console.WriteLine(" ]");
                    Console.WriteLine("  name         = '" + msg.name + "'");
                    Console.WriteLine("  enabled      = '" + msg.enabled + "'");
                }
            }
        }
    }
}
\endcode

# Conclusion {#tut_dotnet_conclusion}

The tutorial has provided a basic working demonstration of the LCM library
.NET port. For further information, please see the LCM documentation.
