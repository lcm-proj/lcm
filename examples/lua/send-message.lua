local lcm = require('lcm')

-- this might be necessary depending on platform and LUA_PATH
package.path = './?/init.lua;' .. package.path

local exlcm = require('exlcm')

local lc = lcm.lcm.new()

local msg = exlcm.example_t:new()

msg.timestamp = os.time() -- time in seconds
msg.position = {1, 2, 3}
msg.orientation = {1, 0, 0, 0}
for i = 1, 15 do
  table.insert(msg.ranges, i)
end
msg.num_ranges = #msg.ranges
msg.name = "example string"
msg.enabled = true

lc:publish("EXAMPLE", msg:encode())
