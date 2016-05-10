The Lua LCM API {#lua_api}
====

The Lua API wraps the LCM C API, and is meant to mirror its functionality and organization. The bulk of the Lua API is represented by the LCM userdata, which basically wraps \ref LcmC_lcm_t "lcm_t and related functions".

# LCM Userdata {#lcm_userdata}

The LCM userdata manages an internal \ref LcmC_lcm_t "lcm_t" and any number of subscriptions.

\em Initializer

\li \ref lcm_userdata_new "new"

\em Methods

\li \ref lcm_userdata_publish "publish"
\li \ref lcm_userdata_subscribe "subscribe"
\li \ref lcm_userdata_unsubscribe "unsubscribe"
\li \ref lcm_userdata_handle "handle"
\li \ref lcm_userdata_handle_timeout "handle_timeout"
\li \ref lcm_userdata_timedhandle "timedhandle (Deprecated)"

<hr>

## new {#lcm_userdata_new}

\em Parameters

\li \c provider Optional. The LCM provider.

<em>Return Values</em>

\li The new LCM userdata.

\em Description

This is the userdata initializer; it creates a new userdata.

<em>Example Code</em>

\verbatim
local lcm = require('lcm')

local lc = lcm.lcm.new()
local lc2 = lcm.lcm.new('udpm://239.255.76.67:7667?ttl=1')
\endverbatim

<hr>

## publish {#lcm_userdata_publish}

\em Parameters

\li \c channel The channel to publish to.
\li \c message The encoded message to send.

<em>Return Values</em>

\li (None.)

\em Description

This method publishes a message to a channel. If the message cannot be published, an error is raised.

<em>Example Code</em>

\verbatim
local lcm = require('lcm')
local msg_t = require('msg_t') -- or any other message type

local lc = lcm.lcm.new()

local msg = msg_t:new()
local encoded_msg = msg:encode()

lc:publish('somechannel', encoded_msg)
\endverbatim

<hr>

## subscribe {#lcm_userdata_subscribe}

\em Parameters

\li \c channel The channel to subscribe to.
\li \c handler The callback.

<em>Return Values</em>

\li The subscription reference.

\em Description

This method creates a subscription to a channel. There may be multiple subscriptions per channel. Creating a subscription involves registering a callback, which is invoked once per received message on the specified channel. The callback is invoked during calls to \ref lcm_userdata_handle "handle" or \ref lcm_userdata_timedhandle "timedhandle".

Notice that this function does not return an actual subscription, but a reference to one. This function returns an integer which is used to index an internal table of subscriptions. The lifetime of the internal subscription is not dependent on the reference, so subscriptions cannot be garbage collected. Subscriptions can only be removed by being \ref lcm_userdata_unsubscribe "unsubscribed".

<em>Example Code</em>

\verbatim
local lcm = require('lcm')
local msg_t = require('msg_t') -- or any other message type

local lc = lcm.lcm.new()

local function handler(channel, encoded_msg)
  local msg = msg_t.decode(encoded_msg)
  -- ...
end

local sub = lc:subscribe('somechannel', handler)
\endverbatim

<hr>

## unsubscribe {#lcm_userdata_unsubscribe}

\em Parameters

\li \c sub The subscription reference to unsubscribe.

<em>Return Values</em>

\li (None.)

\em Description

The method removes a subscription created by \ref lcm_userdata_subscribe "subscribe". Also note that all subscriptions are automatically unsubscribed when the LCM userdata is garbage collected.

<em>Example Code</em>

\verbatim
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
\endverbatim

<hr>

## handle {#lcm_userdata_handle}

\em Parameters

\li (None.)

<em>Return Values</em>

\li (None.)

\em Description

Waits for an incomming message, and dispatches handler callbacks as necessary. This method will block indefinitely until a message is received. When a message is received, all of the handler callbacks for the message's channel are invoked, in the same order they were \ref lcm_userdata_subscribe "subscribed".

<em>Example Code</em>

\verbatim
local lcm = require('lcm')

local lc = lcm.lcm.new()

lc:handle()
\endverbatim

<hr>

## handle_timeout {#lcm_userdata_handle_timeout}

\em Parameters

\li \c time The time to block, in milliseconds.

<em>Return Values</em>

\li A boolean: \c true if a message was received and handled, \c false otherwise.

\em Description

This method is like the normal \ref lcm_userdata_handle "handle" except it only blocks for a specified amount of time.

<em>Example Code</em>

\verbatim
local lcm = require('lcm')

local lc = lcm.lcm.new()

local ok = lc:handle_timeout(500)
if not ok then
  print('timed out!')
 end
\endverbatim

<hr>

## timedhandle (Deprecated) {#lcm_userdata_timedhandle}

This function is deprecated! Please use \ref lcm_userdata_handle_timeout "handle_timeout" instead!

\em Parameters

\li \c time The time to block, in seconds.

<em>Return Values</em>

\li A boolean: \c true if a message was received and handled, \c false otherwise.

\em Description

This method is like the normal \ref lcm_userdata_handle "handle" except it only blocks for a specified amount of time.

<em>Example Code</em>

\verbatim
local lcm = require('lcm')

local lc = lcm.lcm.new()

local ok = lc:timedhandle(0.5)
if not ok then
  print('timed out!')
 end
\endverbatim
