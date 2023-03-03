# Lua Tutorial

Sending and receiving LCM messages with Lua

## Introduction

This tutorial will walk you through the main tasks for exchanging LCM messages
using the Lua API.  The topics covered
in this tutorial are:

- Initialize LCM in your application.
- Publish a message.
- Subscribe to and receive a message.

This tutorial uses the `example_t` message type defined in the
[type definition tutorial](./tutorial-lcmgen.md#lcm-gen-tutorial), and assumes that you have
generated the Lua bindings for the example type by running
``` 
lcm-gen -l example_t.lcm
``` 

After running this command, you should have the following files:
``` 
exlcm/example_t.lua
exlcm/init.lua
``` 

The first file contains the Lua bindings for the `example_t` message type,
and the `init.lua` file sets up a Lua package.  If you have the time,
take a moment to open up the files and inspect the generated
code.  Note that if `exlcm/init.lua` already existed, then it will be
regenerated as necessary.

## Initializing LCM

The first task for any application that uses LCM is to initialize the library.
In Lua, this is very straightforward:

``` 
local lcm = require('lcm')

local lc = lcm.lcm.new()
``` 

The primary communications functionality is contained in the [LCM userdata](./lua-api.md#lcm-userdata).
The constructor initializes communications resources, and has a single optional
argument.
If no argument is given, as above, then the LCM instance is initialized to
reasonable defaults, which are suitable for communicating with other LCM
applications on the local computer.  The argument can also be a string
specifying the underlying communications mechanisms.  The LCM Lua class
itself is a wrapper around the C LCM library, so for information on setting the
class up for communication across computers, or other usages such as reading
data from an LCM logfile (e.g., to post-process or analyze previously collected
data), see the documentation for lcm_create().

If an error occurs initializing LCM, then the initializer will throw a Lua error.

## Publishing a message

When you create an LCM data type and generate Lua code with <tt>lcm-gen</tt>,
that data type will then be available as a Lua class with the same name.  For
<tt>example_t</tt>, the Lua class that gets generated looks like this:

``` lua
local example_t = {}
example_t.__index = example_t

example_t.name = 'exlcm.example_t'
example_t.packagename = 'exlcm'
example_t.shortname = 'example_t'

function example_t:new()

  local obj = {}

  obj.timestamp = 0
  obj.position = {}
  for d0 = 1, 3 do
    obj.position[d0] = 0.0
  end
  obj.orientation = {}
  for d0 = 1, 4 do
    obj.orientation[d0] = 0.0
  end
  obj.num_ranges = 0
  obj.ranges = {}
  obj.name = ''
  obj.enabled = false

  setmetatable(obj, self)

  return obj
end
``` 

Notice here that fixed-length arrays in LCM appear as Lua table arrays initialized
to the appropriate length, and variable length arrays start off as empty lists.
All other fields are initialized to some reasonable defaults.

We can instantiate and then publish some sample data as follows:

``` lua
local lcm = require('lcm')

-- this might be necessary depending on platform and LUA_PATH
package.path = './?/init.lua;' .. package.path

local exlcm = require('exlcm')

local lc = lcm.lcm.new()

local msg = exlcm.example_t:new()

msg.timestamp = 0
msg.position = {1, 2, 3}
msg.orientation = {1, 0, 0, 0}
for i = 1, 15 do
  table.insert(msg.ranges, i)
end
msg.num_ranges = #msg.ranges
msg.name = "example string"
msg.enabled = true

lc:publish("EXAMPLE", msg:encode())
``` 

The full example is available in runnable form as
<tt>examples/lua/send-message.lua</tt> in the LCM source distribution.

For the most part, this example should be pretty straightforward.  The
application creates a message, fills in the message data fields, then
initializes LCM and publishes the message.

The call to [](lua-api.md#publish) serializes the data into a byte stream and
transmits the packet to any interested receivers.  The string
<tt>"EXAMPLE"</tt> is the <em>channel</em> name, which is a string
transmitted with each packet that identifies the contents to receivers.
Receivers subscribe to different channels using this identiffier, allowing
uninteresting data to be discarded quickly and efficiently.

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

``` lua
local lcm = require('lcm')

-- this might be necessary depending on platform and LUA_PATH
package.path = './?/init.lua;' .. package.path

local exlcm = require('exlcm')

function array_to_str(array)
  str = '{'
  for i = 1, #array - 1 do
    str = str .. array[i] .. ', '
  end
  return str .. array[#array] .. '}'
end

function my_handler(channel, data)

  local msg = exlcm.example_t.decode(data)

  print(string.format("Received message on channel \"%s\"", channel))
  print(string.format("   timestamp   = %d", msg.timestamp))
  print(string.format("   position    = %s", array_to_str(msg.position)))
  print(string.format("   orientation = %s", array_to_str(msg.orientation)))
  print(string.format("   ranges: %s", array_to_str(msg.ranges)))
  print(string.format("   name        = '%s'", msg.name))
  print(string.format("   enabled     = %s", tostring(msg.enabled)))
  print("")
end

lc = lcm.lcm.new()
sub = lc:subscribe("EXAMPLE", my_handler)

while true do
  lc:handle()
end

-- all remaining subscriptions are unsubed at garbage collection
``` 

The full example is available in runnable form as
<tt>examples/lua/listener.lua</tt> in the LCM source distribution.

After initializing the LCM object, the application subscribes to a channel by
passing a callback function to the [](lua-api.md#subscribe)
method.

The example application then repeatedly calls
[](lua-api.md#handle),
which simply waits for a message of interest to arrive and then invokes the
appropriate callback functions.
Callbacks are invoked one at a time, so there is no need for thread
synchronization.

If your application has other work to do while waiting for messages (e.g.,
print out a message every few seconds or check for input somewhere else), you
can use the [](lua-api.md#handle_timeout) method. This method will block for
up to the specified number of milliseconds, and then return a boolean: true if
a message was received and handled, and false otherwise.
