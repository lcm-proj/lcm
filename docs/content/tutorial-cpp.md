# C++ Tutorial

Sending and receiving LCM messages with C++

## Introduction

This tutorial will walk you through the main tasks for exchanging LCM messages
using the C++ API.  The C++ API is a header-only wrapper around the C API, and
thus has all the same compilation and linking requirements.  The topics covered
in this tutorial are:

- Initialize LCM in your application.
- Publish a message.
- Subscribe to and receive a message.

This tutorial uses the `example_t` message type defined in the
[type definition tutorial](tutorial-lcmgen.md), and assumes that you have
generated the C++ bindings for the example type by running
``` 
lcm-gen -x example_t.lcm
``` 

After running this command, you should have one file,
<tt>exlcm/example_t.hpp</tt>.  This file is the C++ binding for the example
message type.  Notice that there is no <tt>.cpp</tt> file generated: the
binding is a header only.  If you have the time, take a moment to open up the
file and inspect the generated code.

## Initializing LCM

The first task for any application that uses LCM is to initialize the library.
Here's an example:

``` C++
#include <lcm/lcm-cpp.hpp>

int main(int argc, char ** argv)
{
    lcm::LCM lcm;

    if(!lcm.good())
        return 1;

    /* Your application goes here */

    return 0;
}
``` 

LCM uses the namespace `lcm`, and the primary communications functionality is
contained in the <a href="../doxygen_output/c_cpp/html/classlcm_1_1LCM.html">lcm::LCM</a>  class.  The constructor initializes communications,
and has a single optional argument.  If no argument is given, as above, then
the LCM instance is initialized to reasonable defaults, which are suitable for
communicating with other LCM applications on the local computer.  The argument
can also be a string specifying the underlying communications mechanisms.  For
communication across computers, or other usages such as reading data from
an LCM logfile (e.g., to post-process or analyze previously collected data),
see the <a href="../doxygen_output/c_cpp/html/classlcm_1_1LCM.html#a18f8e2693cdafb36c4481789aa8ec24e">constructor documentation</a>.

Once constructed, the <a href="../doxygen_output/c_cpp/html/classlcm_1_1LCM.html#a532ecef61ae5651931df3ae7d4cf5a32">lcm::LCM:good()</a> method can be used to check if
there were any errors initializing the LCM instance for communications.

The class destructor takes care of releasing resources and cleaning up, so it
is sufficient to explicitly delete heap-allocated instances or let
stack-allocated instances go out of scope.

## Publishing a message

When you create an LCM data type and generate C++ code with <tt>lcm-gen</tt>,
that data type will then be available as a C++ class with the same name.  For
<tt>example_t</tt>, the C++ class that gets generated looks like this:
    
``` C++
namespace exlcm 
{

class example_t
{
    public:
        int64_t    timestamp;
        double     position[3];
        double     orientation[4];
        int32_t    num_ranges;
        std::vector< int16_t > ranges;
        std::string name;
        int8_t     enabled;
``` 

Notice here that fixed-length arrays in LCM appear as fixed-length C++ arrays,
and that variable length arrays appear as STL vectors.  More on that below.
    
We can instantiate and then publish some sample data as follows:
    
``` C++
#include <lcm/lcm-cpp.hpp>
#include "exlcm/example_t.hpp"

int main(int argc, char ** argv)
{
    lcm::LCM lcm;
    if(!lcm.good())
        return 1;

    exlcm::example_t my_data;
    my_data.timestamp = 0;

    my_data.position[0] = 1;
    my_data.position[1] = 2;
    my_data.position[2] = 3;

    my_data.orientation[0] = 1;
    my_data.orientation[1] = 0;
    my_data.orientation[2] = 0;
    my_data.orientation[3] = 0;

    my_data.num_ranges = 15;
    my_data.ranges.resize(my_data.num_ranges);
    for(int i = 0; i < my_data.num_ranges; i++)
        my_data.ranges[i] = i;

    my_data.name = "example string";
    my_data.enabled = true;

    lcm.publish("EXAMPLE", &my_data);

    return 0;
}
``` 

For the most part, this example should be pretty straightforward.
Note that `my_data.ranges` refers to a variable length array defined by the
<tt>example_t</tt> LCM type, and is represented by a STL vector in the
generated C++ class.  It is up to the programmer to resize it appropriately and
set `my_data.num_ranges` to a value smaller or equal to the
number of elements in that vector.  When the message is encoded,
`my_data.num_ranges` determines how many elements will actually be read and
transmitted from `my_data.ranges`.  If `my_data.num_ranges` is set to 0, the
contents of `my_data.ranges` is ignored.

The call to <a href="../doxygen_output/c_cpp/html/classlcm_1_1LCM.html#a8596505355407de0f83ea77fbb1deab9">lcm::LCM:publish()</a> serializes the data into a byte stream and
transmits the packet using LCM to any interested receivers.  The string
<tt>"EXAMPLE"</tt> is the <em>channel</em> name, which is a string
transmitted with each packet that identifies the contents to receivers.
Receivers subscribe to different channels using this identifier, allowing
uninteresting data to be discarded quickly and efficiently.

The full example is available in runnable form as
<tt>examples/cpp/send_message.cpp</tt> in the LCM source distribution.

## Receiving LCM Messages

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

``` C++
#include <stdio.h>
#include <lcm/lcm-cpp.hpp>
#include "exlcm/example_t.hpp"

class Handler 
{
    public:
        ~Handler() {}

        void handleMessage(const lcm::ReceiveBuffer* rbuf,
                const std::string& chan, 
                const exlcm::example_t* msg)
        {
            int i;
            printf("Received message on channel \"%s\":\n", chan.c_str());
            printf("  timestamp   = %lld\n", (long long)msg->timestamp);
            printf("  position    = (%f, %f, %f)\n",
                    msg->position[0], msg->position[1], msg->position[2]);
            printf("  orientation = (%f, %f, %f, %f)\n",
                    msg->orientation[0], msg->orientation[1], 
                    msg->orientation[2], msg->orientation[3]);
            printf("  ranges:");
            for(i = 0; i < msg->num_ranges; i++)
                printf(" %d", msg->ranges[i]);
            printf("\n");
            printf("  name        = '%s'\n", msg->name.c_str());
            printf("  enabled     = %d\n", msg->enabled);
        }
};

int main(int argc, char** argv)
{
    lcm::LCM lcm;
    if(!lcm.good())
        return 1;

    Handler handlerObject;
    lcm.subscribe("EXAMPLE", &Handler::handleMessage, &handlerObject);

    while(0 == lcm.handle());

    return 0;
}
``` 

The full example is available in runnable form as
<tt>examples/cpp/listener.cpp</tt> in the LCM source distribution.

After creating a handler object, the application supplies a callback method and
instance variable to <a href="../doxygen_output/c_cpp/html/classlcm_1_1LCM.html#a5df6f3eee54c375d9ab0b7a365d25516">lcm::LCM::subscribe()</a>.  During a call to
<a href="../doxygen_output/c_cpp/html/classlcm_1_1LCM.html#aac221c0fa80ede30a2383fba612f972e">lcm::LCM::handle()</a>, the callback method is invoked on the handler object if the
appropriate message has arrived.  Using templates, the callback method
signature directly specifies the message type, and LCM automatically decodes
the message before passing it to the callback method.
This happens inside a single thread without need for concurrency, since the
callback is dispatched from within the <a href="../doxygen_output/c_cpp/html/classlcm_1_1LCM.html#aac221c0fa80ede30a2383fba612f972e">lcm::LCM::handle()</a> method.

It is possible to use other types of callbacks as well, not just methods bound
to class instances.  See the <a href="../doxygen_output/c_cpp/html/classlcm_1_1LCM.html">lcm::LCM</a> API reference for more information.

It is important to call <a href="../doxygen_output/c_cpp/html/classlcm_1_1LCM.html#aac221c0fa80ede30a2383fba612f972e">lcm::LCM::handle()</a> whenever work needs to be done by LCM.
If no work is needed, the function will block until there is.  For
applications without another type of main loop, it is suitable to call
<a href="../doxygen_output/c_cpp/html/classlcm_1_1LCM.html#aac221c0fa80ede30a2383fba612f972e">lcm::LCM::handle()</a> in a loop as seen above.  However, many applications already use
some type of event loop.  In these cases, it is best to monitor the LCM file
descriptor, which can be obtained with <a href="../doxygen_output/c_cpp/html/classlcm_1_1LCM.html#aacaf64ce2c73a8a7f3fcde386c112055">lcm::LCM::getFileno()</a>.  Whenever this
file descriptor becomes readable, the application should call <a href="../doxygen_output/c_cpp/html/classlcm_1_1LCM.html#aac221c0fa80ede30a2383fba612f972e">lcm::LCM::handle()</a>
which is guaranteed to not block in such a situation.
