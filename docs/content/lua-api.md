# The Lua LCM API

The Lua API wraps the LCM C API, and is meant to mirror its functionality and organization. The bulk of the Lua API is represented by the LCM userdata, which basically wraps lcm_t and related functions.

## LCM Userdata

The LCM userdata manages an internal <a href="../doxygen_output/c_cpp/html/group__LcmC__lcm__t.html">lcm_t</a> and any number of subscriptions.

*Initializer*

- [new](#new)

*Methods*

- [publish](#publish)
- [subscribe](#subscribe)
- [unsubscribe](#unsubscribe)
- [handle](#handle)
- [handle_timeout](#handle_timeout)
- [timedhandle](#handle_timeout (Deprecated))

<hr>

### new

*Parameters*

- `provider` Optional. The LCM provider.

<em>Return Values</em>

- The new LCM userdata.

*Description*

This is the userdata initializer; it creates a new userdata.

<em>Example Code</em>

``` 
local lcm = require('lcm')

local lc = lcm.lcm.new()
local lc2 = lcm.lcm.new('udpm://239.255.76.67:7667?ttl=1')
``` 

<hr>

### publish

*Parameters*

- `channel` The channel to publish to.
- `message` The encoded message to send.

<em>Return Values</em>

- (None.)

*Description*

This method publishes a message to a channel. If the message cannot be published, an error is raised.

<em>Example Code</em>

``` 
local lcm = require('lcm')
local msg_t = require('msg_t') -- or any other message type

local lc = lcm.lcm.new()

local msg = msg_t:new()
local encoded_msg = msg:encode()

lc:publish('somechannel', encoded_msg)
``` 

<hr>

### subscribe

*Parameters*

- `channel` The channel to subscribe to.
- `handler` The callback.

<em>Return Values</em>

- The subscription reference.

*Description*

This method creates a subscription to a channel. There may be multiple subscriptions per channel. Creating a subscription involves registering a callback, which is invoked once per received message on the specified channel. The callback is invoked during calls to [handle](#handle) or [timedhandle](#handle_timeout).

Notice that this function does not return an actual subscription, but a reference to one. This function returns an integer which is used to index an internal table of subscriptions. The lifetime of the internal subscription is not dependent on the reference, so subscriptions cannot be garbage collected. Subscriptions can only be removed by being [unsubscribed](#unsubscribe).

<em>Example Code</em>

``` 
local lcm = require('lcm')
local msg_t = require('msg_t') -- or any other message type

local lc = lcm.lcm.new()

local function handler(channel, encoded_msg)
  local msg = msg_t.decode(encoded_msg)
  -- ...
end

local sub = lc:subscribe('somechannel', handler)
``` 

<hr>

### unsubscribe

*Parameters*

- `sub` The subscription reference to unsubscribe.

<em>Return Values</em>

- (None.)

*Description*

The method removes a subscription created by [subscribe](#subscribe). Also note that all subscriptions are automatically unsubscribed when the LCM userdata is garbage collected.

<em>Example Code</em>

``` 
local lcm = require('lcm')
local msg_t = require('msg_t') -- or any other message type

local lc = lcm.lcm.new()

local function handler(channel, encoded_msg)
  local msg = msg_t.decode(encoded_msg)
  -- ...
end

local sub = lc:subscribe('somechannel', handler)

--- ...

lc:unsubscribe(sub)
``` 

<hr>

### handle

*Parameters*

- (None.)

<em>Return Values</em>

- (None.)

*Description*

Waits for an incomming message, and dispatches handler callbacks as necessary. This method will block indefinitely until a message is received. When a message is received, all of the handler callbacks for the message's channel are invoked, in the same order they were [subscribed](#subscribe).

<em>Example Code</em>

``` 
local lcm = require('lcm')

local lc = lcm.lcm.new()

lc:handle()
``` 

<hr>

### handle_timeout

*Parameters*

- `time` The time to block, in milliseconds.

<em>Return Values</em>

- A boolean: `true` if a message was received and handled, `false` otherwise.

*Description*

This method is like the normal [handle](#handle) except it only blocks for a specified amount of time.

<em>Example Code</em>

``` 
local lcm = require('lcm')

local lc = lcm.lcm.new()

local ok = lc:handle_timeout(500)
if not ok then
  print('timed out!')
 end
``` 

<hr>

### timedhandle (Deprecated)

This function is deprecated! Please use [handle_timeout](#handle_timeout) instead!

*Parameters*

- `time` The time to block, in seconds.

<em>Return Values</em>

- A boolean: `true` if a message was received and handled, `false` otherwise.

*Description*

This method is like the normal [handle](#handle) except it only blocks for a specified amount of time.

<em>Example Code</em>

``` 
local lcm = require('lcm')

local lc = lcm.lcm.new()

local ok = lc:timedhandle(0.5)
if not ok then
  print('timed out!')
 end
``` 
