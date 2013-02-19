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

-- all subscriptions are unsubed at garbage collection
