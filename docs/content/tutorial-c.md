C Tutorial {#tut_c}
====
\brief Sending and receiving LCM messages with C

# Introduction {#tutorial_c_intro}

This tutorial will walk you through the main tasks for exchanging messages
using the LCM C API.  It covers the following topics:

\li Initialize LCM in your application.
\li Publish a message.
\li Subscribe to and receive a message.

This tutorial uses the \p example_t message type defined in the
\ref tut_lcmgen "type definition tutorial", and assumes that you have
generated the C bindings for the example type by running
\code
lcm-gen -c example_t.lcm
\endcode

After running this command, you should have two files:
<tt>exlcm_example_t.c</tt> and <tt>exlcm_example_t.h</tt>.  These two files are
the C bindings for the example message type.  Notice that the message type
package name "exlcm" was prepended to the file name.  This is how LCM emulates
namespaces in C, and the package name is also prefixed to the generated C struct name.  
If you have the time, take a moment to open up those two files and inspect the generated code.

# Initializing LCM {#tutorial_c_initialize}

The first task for any application that uses LCM is to
initialize the library.  Here's an example of that (and how to clean
up after itself as well):

\code
#include <lcm/lcm.h>

int main(int argc, char ** argv)
{
    lcm_t * lcm = lcm_create(NULL);

    if(!lcm)
        return 1;

    /* Your application goes here */

    lcm_destroy(lcm);
    return 0;
}
\endcode

The function lcm_create() allocates and initializes an instance of
\ref lcm_t, which represents a connection to an LCM network.  The single
argument can be \c NULL as shown above, to initialize LCM
with default settings.  The defaults initialized are suitable for communicating
with other LCM applications on the local computer.  The argument can also be a
string specifying the underlying communications mechanisms.
For communication across computers, or other usages such as reading data from
an LCM logfile (e.g., to post-process or analyze previously collected data),
see the API reference for lcm_create().

Once you're all done, it's a good idea to call lcm_destroy() to clean
up any resources used by LCM.
    
# Publishing a message {#tutorial_c_publishing}

When you create an LCM data type and generate C code with <tt>lcm-gen</tt>,
that data type will then be available as a C struct with the same name.  For
<tt>example_t</tt>, the C struct that gets generated looks like this:
    
\code
typedef struct _exlcm_example_t exlcm_example_t;
struct _exlcm_example_t
{
    int64_t    timestamp;
    double     position[3];
    double     orientation[4];
    int32_t    num_ranges;
    int16_t    *ranges;
    char*      name;
    int8_t     enabled;
};
\endcode

Notice here that fixed-length arrays in LCM appear as fixed-length C arrays.
Variable length arrays appear as pointers in C.  More on that below.
    
We can instantiate and then publish some sample data as follows:
    
\code
#include <lcm/lcm.h>
#include "exlcm_example_t.h"

int
main(int argc, char ** argv)
{
    lcm_t * lcm = lcm_create(NULL);
    if(!lcm)
        return 1;

    exlcm_example_t my_data = {
        .timestamp = 0,
        .position = { 1, 2, 3 },
        .orientation = { 1, 0, 0, 0 },
    };
    int16_t ranges[15];
    int i;
    for(i = 0; i < 15; i++)
        ranges[i] = i;

    my_data.num_ranges = 15;
    my_data.ranges = ranges;
    my_data.name = "example string";
    my_data.enabled = 1;

    exlcm_example_t_publish(lcm, "EXAMPLE", &my_data);

    lcm_destroy(lcm);
    return 0;
}
\endcode

The full example is available in runnable form as
<tt>examples/c/send_message.c</tt> in the LCM source distribution.

For the most part, this example should be pretty straightforward.
Note that \c my_data.ranges refers to a variable length array defined by the
<tt>example_t</tt> LCM type, and is represented by a pointer in the generated
C struct.  It is up to the programmer to set this pointer to an array of the
proper type, and set \c my_data.num_ranges to a value smaller or equal to the
number of elements in that array.  When the message is encoded, \c
my_data.num_ranges determines how many elements will actually be read and
transmitted from \c my_data.ranges.  If \c my_data.num_ranges is set to 0, the
value of \c my_data.ranges is ignored.

The call to exlcm_example_t_publish() serializes the data into a byte stream and
transmits the packet using LCM to any interested receivers.  The string
<tt>"EXAMPLE"</tt> is the <em>channel</em> name, which is a string
transmitted with each packet that identifies the contents to receivers.
Receivers subscribe to different channels using this identifier, allowing
uninteresting data to be discarded quickly and efficiently.

# Receiving LCM Messages {#tutorial_c_receive}

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
#include <stdio.h>
#include <inttypes.h>
#include <lcm/lcm.h>
#include "exlcm_example_t.h"

static void
my_handler(const lcm_recv_buf_t *rbuf, const char * channel, 
        const exlcm_example_t * msg, void * user)
{
    int i;
    printf("Received message on channel \"%s\":\n", channel);
    printf("  timestamp   = %"PRId64"\n", msg->timestamp);
    printf("  position    = (%f, %f, %f)\n",
            msg->position[0], msg->position[1], msg->position[2]);
    printf("  orientation = (%f, %f, %f, %f)\n",
            msg->orientation[0], msg->orientation[1], msg->orientation[2],
            msg->orientation[3]);
    printf("  ranges:");
    for(i = 0; i < msg->num_ranges; i++)
        printf(" %d", msg->ranges[i]);
    printf("\n");
    printf("  name        = '%s'\n", msg->name);
    printf("  enabled     = %d\n", msg->enabled);
}

int
main(int argc, char ** argv)
{
    lcm_t * lcm = lcm_create(NULL);
    if(!lcm)
        return 1;

    exlcm_example_t_subscribe(lcm, "EXAMPLE", &my_handler, NULL);

    while(1)
        lcm_handle(lcm);

    lcm_destroy(lcm);
    return 0;
}
\endcode

The full example is available in runnable form as
<tt>examples/c/listener.c</tt> in the LCM source distribution.

A key design principal for this subscription code is that it is <em>event
driven</em>.  The application supplies a callback function to example_t_subscribe()
that is called whenever a message is available.  This happens inside a single
thread without need for concurrency, since the callback is dispatched from
within the lcm_handle() function.

It is important to call lcm_handle() whenever work needs to be done by LCM.
If no work is needed, the function will block until there is.  For
applications without another type of main loop, it is suitable to call
lcm_handle() in a loop as seen above.  However, many applications already use
some type of event loop.  In these cases, it is best to monitor the LCM file
descriptor, which can be obtained with lcm_get_fileno().  Whenever this
file descriptor becomes readable, the application should call lcm_handle()
which is guaranteed to not block in such a situation.  Additional examples for
doing this can be found in <tt>examples/c</tt>, in the LCM source distribution.
