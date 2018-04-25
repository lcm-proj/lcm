Tutorial and examples {#tutorial_general}
====

# Introduction {#tutorial_general_intro}

LCM is a package designed to allow multiple processes to exchange messages in
a safe and high-performance way.   
A <em>message</em> is the basic unit of LCM communications: it represents a
self-contained piece of information.  Messages are defined as programming
language-independent data structures; the lcm-gen tool compiles these
definitions into language-specific code.

Each message is sent on a <em>channel</em>, which is identified by a
human-readable name. For example, messages containing information about the
temperature in the hallway might be published on the "HALLWAY_TEMPERATURE"
channel. By convention, all messages on a channel have the same type.

Any application can publish on any channel, although it is common for
a single application serves as the sole source of data on a channel. Any
application can receive data on any channel--- for example, both a thermostat
application and data logger might subscribe to the "HALLWAY_TEMPERATURE"
channel.

This tutorial will walk you through the main tasks for exchange messages
between two applications:

 -# Create a type definition
 -# Initialize LCM in your application
 -# Publish a message
 -# Subscribe to and receive a message

Since the type definitions are language independent, the \ref tut_lcmgen "first step" is the same
for all programming languages.  

 - \ref tut_lcmgen "Step 1: Creating a type definition"

The remaining steps vary across programming
languages, consult the following sections for language-specific tutorials.

 - \ref tut_c "Steps 2-4: C"
 - \ref tut_cpp "Steps 2-4: C++"
 - \ref tut_dotnet "Steps 2-4: C# / .NET"
 - \ref tut_java "Steps 2-4: Java"
 - \ref tut_matlab "Steps 2-4: MATLAB"
 - \ref tut_python "Steps 2-4: Python"
 - \ref tut_lua "Steps 2-4: Lua"
 - \ref tut_go "Steps 2-4: Go"

Note that C and C++ are considered to be separate programming languages.  It is
possible to use the C bindings from C++, but there are also LCM bindings
specific to C++.

Of course, serious projects will need a build system. LCM itself is built with
[CMake](https://cmake.org/), and provides helper functions to simplify the
binding generation process for projects built with CMake.

 - \ref tut_cmake "Steps 5: Generating bindings with CMake"

# Additional examples

Additional examples are provided with the source distribution of LCM.

For each language, at least two examples are provided. One listens for a
message, the other transmits a message. The LCM type used is defined in
[example_t.lcm](https://github.com/lcm-proj/lcm/blob/master/examples/types/example_t.lcm)  (`examples/types/example_t.lcm` in the source distribution).

Additional examples are distributed with the LCM source in the `examples/`
directory.

Language | Listener | Transmitter
-------- | -------- | -----------
C        | [listener.c](https://github.com/lcm-proj/lcm/blob/master/examples/c/listener.c) <br>[listener-async.c](https://github.com/lcm-proj/lcm/blob/master/examples/c/listener-async.c) | [send_message.c](https://github.com/lcm-proj/lcm/blob/master/examples/c/send_message.c)
C++      | [listener.cpp](https://github.com/lcm-proj/lcm/blob/master/examples/cpp/listener.cpp) | [send_message.cpp](https://github.com/lcm-proj/lcm/blob/master/examples/cpp/send_message.cpp)
C#       | [example_t_display.cs](https://github.com/lcm-proj/lcm/blob/master/examples/csharp/example_t_demo/example_t_display.cs) | [example_t_transmit.cs](https://github.com/lcm-proj/lcm/blob/master/examples/csharp/example_t_demo/example_t_transmit.cs)
Java     | [MySubscriber.java](https://github.com/lcm-proj/lcm/blob/master/examples/java/example_t_demo/MySubscriber.java) | [SendMessage.java](https://github.com/lcm-proj/lcm/blob/master/examples/java/example_t_demo/SendMessage.java)
Lua      | [listener.lua](https://github.com/lcm-proj/lcm/blob/master/examples/lua/listener.lua) | [send-message.lua](https://github.com/lcm-proj/lcm/blob/master/examples/lua/send-message.lua)
MATLAB   | [listener.m](https://github.com/lcm-proj/lcm/blob/master/examples/matlab/listener.m) | [sendmessage.m](https://github.com/lcm-proj/lcm/blob/master/examples/matlab/sendmessage.m)
Python   | [listener.py](https://github.com/lcm-proj/lcm/blob/master/examples/python/listener.py) | [send-message.py](https://github.com/lcm-proj/lcm/blob/master/examples/python/send-message.py)
Go       | [listener/main.go](https://github.com/lcm-proj/lcm/blob/master/examples/go/listener/main.go) | [sender/main.go](https://github.com/lcm-proj/lcm/blob/master/examples/go/sender/main.go)
