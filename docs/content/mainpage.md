LCM documentation {#mainpage}
====

# Tutorials and Instructions {#main_tutorials}

 - \ref build_instructions
 - \ref tutorial_general

# API Reference {#main_api_ref}

 - [C](\ref LcmC)
 - [C++](\ref LcmCpp)
 - [C# / .NET](lcm-dotnet/index.html)
 - [Java](javadocs/index.html)
 - [Lua](\ref lua_api)
 - [Python](python/index.html)

# Specifications

 - \ref type_specification
 - \ref udp_multicast_protocol
 - \ref log_file_format

# Publications

 - [LCM Overview](http://people.csail.mit.edu/albert/pubs/2010-huang-olson-moore-lcm-iros.pdf) - Describes the design principles of LCM, the best place to start for a quick understanding of LCM.  6 pages.
   - _IROS, Taipei, Taiwan, Oct 2010._
 - [Technical Report](http://dspace.mit.edu/bitstream/handle/1721.1/46708/MIT-CSAIL-TR-2009-041.pdf) - An expanded version of the overview, provides a more comprehensive description.  17 pages.
    - _Technical Report MIT-CSAIL-TR-2009-041, Massachusetts Institute of Technology, 2009_

# Application notes {#main_other}

 - \ref multicast_setup
 - \ref java_notes

# Examples

Simple examples are provided with the source distribution of LCM.

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
