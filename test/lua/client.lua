
local lcm = require('lcm')

-- this line required to make `require` correctly search the working directory
package.path = './?/init.lua;' .. package.path

local lcmtest = require('lcmtest')

local function info(text)
	print("client: " .. text)
end

local function check_field(value, expectedvalue, name)

	if value ~= expectedvalue then
		error(string.format("%s does not equal %s for field %s!", tostring(value), tostring(expectedvalue), name))
	end
end

local MultidimTest = {}
MultidimTest.__index = MultidimTest

function MultidimTest:new()

	local obj = {}
	obj.name = "multidim array test"
	obj.msgtype = lcmtest.multidim_array_t
	obj.num_iterations = 5
	setmetatable(obj, MultidimTest)

	return obj
end

function MultidimTest:make_message(iteration)

	local msg = self.msgtype:new()

	msg.size_a = iteration
	msg.size_b = iteration
	msg.size_c = iteration

	local n = 0
	for i = 1, msg.size_a do
		msg.data[i] = {}
		for j = 1, msg.size_b do
			msg.data[i][j] = {}
			for k = 1, msg.size_c do
				msg.data[i][j][k] = n
				n = n + 1
			end
		end
	end

	n = 0
	for i = 1, 2 do
		msg.strarray[i] = {}
		for j = 1, msg.size_c do
			msg.strarray[i][j] = tostring(n)
			n = n + 1
		end
	end

	return msg
end

function MultidimTest:check_reply(msg, iteration)

	check_field(msg.size_a, iteration + 1, "size_a")
	check_field(msg.size_b, iteration + 1, "size_b")
	check_field(msg.size_c, iteration + 1, "size_c")

	local n = 0
	for i = 1, msg.size_a do
		for j = 1, msg.size_b do
			for k = 1, msg.size_c do
				check_field(msg.data[i][j][k], n, string.format("data[%d][%d][%d]", i, j, k))
				n = n + 1
			end
		end
	end

	n = 0
	for i = 1, 2 do
		for j = 1, msg.size_c do
			check_field(msg.strarray[i][j], tostring(n), string.format("strarray[%d][%d]", i, j))
			n = n + 1
		end
	end
end

local NodeTest = {}
NodeTest.__index = NodeTest

function NodeTest:new()

	local obj = {}
	obj.name = "node test"
	obj.msgtype = lcmtest.node_t
	obj.num_iterations = 7
	setmetatable(obj, NodeTest)

	return obj
end

function NodeTest:make_message(iteration)

	local msg = self.msgtype:new()

	msg.num_children = iteration

	msg.children = {}
	for i = 1, msg.num_children do
		msg.children[i] = self:make_message(msg.num_children - 1)
	end

	return msg
end

function NodeTest:check_reply(msg, iteration, decendant)

	if not decendant then
		-- this is only used for the root of the message
		check_field(msg.num_children, iteration + 1, "num_children")
	else
		check_field(msg.num_children, iteration, "num_children")
	end

	for _, childmsg in pairs(msg.children) do
		self:check_reply(childmsg, msg.num_children - 1, true)
	end
end

local PrimitivesListTest = {}
PrimitivesListTest.__index = PrimitivesListTest

function PrimitivesListTest:new()

	local obj = {}
	obj.name = "primitives list test"
	obj.msgtype = lcmtest.primitives_list_t
	obj.num_iterations = 100
	setmetatable(obj, PrimitivesListTest)

	return obj
end

function PrimitivesListTest:make_message(iteration)

	local msg = self.msgtype:new()

	msg.num_items = iteration

	msg.items = {}
	for ix = 1, msg.num_items do

		local submsg = lcmtest.primitives_t:new()

		-- make i start at zero
		local i = ix - 1

		submsg.i8 = -(i % 100)
		submsg.i16 = -i * 10
		submsg.i64 = -i * 10000
		submsg.position[1] = -i
		submsg.position[2] = -i
		submsg.position[3] = -i
		submsg.orientation[1] = -i
		submsg.orientation[2] = -i
		submsg.orientation[3] = -i
		submsg.orientation[4] = -i
		submsg.num_ranges = i

		submsg.ranges = {}
		for j = 1, submsg.num_ranges do
				submsg.ranges[j] = -(j - 1)
		end

		-- +0 is to avoid issues with tostring(-0)
		submsg.name = tostring(-i + 0)

		-- because Lua evaluates 0 as true
		submsg.enabled = (((i + 1) % 2) ~= 0)

		msg.items[ix] = submsg
	end

	return msg
end

function PrimitivesListTest:check_reply(msg, iteration)

	check_field(msg.num_items, iteration + 1, "num_items")

	for ix = 1, msg.num_items do

		local submsg = msg.items[ix]
		local prefix = string.format("items[%d].", ix)

		-- make i start at zero
		local i = ix - 1

		check_field(submsg.i8, -(i % 100), prefix .. "i8")
		check_field(submsg.i16, -(i * 10), prefix .. "i16")
		check_field(submsg.i64, -(i * 10000), prefix .. "i64")
		check_field(submsg.position[1], -i, prefix .. "position[1]")
		check_field(submsg.position[2], -i, prefix .. "position[2]")
		check_field(submsg.position[3], -i, prefix .. "position[3]")
		check_field(submsg.orientation[1], -i, prefix .. "orientation[1]")
		check_field(submsg.orientation[2], -i, prefix .. "orientation[2]")
		check_field(submsg.orientation[3], -i, prefix .. "orientation[3]")
		check_field(submsg.orientation[4], -i, prefix .. "orientation[4]")
		check_field(submsg.num_ranges, i, prefix .. "num_ranges")

		for j = 1, submsg.num_ranges do
			check_field(submsg.ranges[j], -(j - 1), prefix .. string.format("ranges[%d]", j))
		end

		-- +0 is to avoid issues with tostring(-0)
		check_field(submsg.name, tostring(-i + 0), prefix .. "name")

		-- because Lua evaluates 0 as true
		check_field(submsg.enabled, (((i + 1) % 2) ~= 0), prefix .. "enabled")
	end
end

local PrimitivesTest = {}
PrimitivesTest.__index = PrimitivesTest

function PrimitivesTest:new()

	local obj = {}
	obj.name = "primitives test"
	obj.msgtype = lcmtest.primitives_t
	obj.num_iterations = 1000
	setmetatable(obj, PrimitivesTest)

	return obj
end

function PrimitivesTest:make_message(iteration)

	local msg = self.msgtype:new()
	local n = iteration

	msg.i8 = n % 100
	msg.i16 = n * 10
	msg.i64 = n * 10000
	msg.position[1] = n
	msg.position[2] = n
	msg.position[3] = n
	msg.orientation[1] = n
	msg.orientation[2] = n
	msg.orientation[3] = n
	msg.orientation[4] = n
	msg.num_ranges = n

	msg.ranges = {}
	for i = 1, msg.num_ranges do
			msg.ranges[i] = i - 1
	end
	
	msg.name = tostring(n)

	-- because Lua evaluates 0 as true
	msg.enabled = ((n % 2) ~= 0)

	return msg
end

function PrimitivesTest:check_reply(msg, iteration)

	local n = iteration

	check_field(msg.i8, (n + 1) % 100, "i8")
	check_field(msg.i16, (n + 1) * 10, "i16");
	check_field(msg.i64, (n + 1) * 10000, "i64");
	check_field(msg.position[1], n + 1, "position[1]");
	check_field(msg.position[2], n + 1, "position[2]");
	check_field(msg.position[3], n + 1, "position[3]");
	check_field(msg.orientation[1], n + 1, "orientation[1]");
	check_field(msg.orientation[2], n + 1, "orientation[2]");
	check_field(msg.orientation[3], n + 1, "orientation[3]");
	check_field(msg.orientation[4], n + 1, "orientation[4]");
	check_field(msg.num_ranges, n + 1, "num_ranges");

	for i = 1, n + 1 do
		check_field(msg.ranges[i], i - 1, string.format("ranges[%d]", i))
	end

	check_field(msg.name, tostring(n + 1), "name")

	-- because Lua evaluates 0 as true
	check_field(msg.enabled, (((n + 1) % 2) ~= 0), "enabled")
end

local StandardTester = {}
StandardTester.__index = StandardTester

function StandardTester:new(lc, test)

	local obj = {}
	obj.lc = lc
	obj.test = test
	obj.testerr = nil
	obj.response_count = 0
	obj.iteration = 0
	setmetatable(obj, StandardTester)

	return obj
end

function StandardTester:handler(channel, data)

	local function process_msg()
		-- this will error if something goes wrong
		self.test:check_reply(self.test.msgtype.decode(data), self.iteration)
	end

	local function process_err(argerr)
		-- set the objects error, testerr
		self.testerr = argerr
	end

	-- discard status, run() will check testerr
	xpcall(process_msg, process_err)
end

function StandardTester:run()

	self.testerr = nil
	self.response_count = 0
	self.iteration = 0

	local channel = string.format("test_lcmtest_%s", self.test.msgtype.shortname)
	local reply_channel = string.format("test_lcmtest_%s_reply", self.test.msgtype.shortname)

	local handler_closure = function(channel, data) self:handler(channel, data) end
	local sub = self.lc:subscribe(reply_channel, handler_closure)

	for i = 1, self.test.num_iterations do

		-- send a message
		local msg = self.test:make_message(self.iteration)
		self.lc:publish(channel, msg:encode())

		-- get the message, check result
		local ontime = self.lc:timedhandle(500000)
		if not ontime then
			self.testerr = "timed out waiting for response!"
		end

		-- check for an error
		if self.testerr then
			info(string.format("%s failed on iteration %d!", self.test.name, self.iteration))
			info("error: " .. tostring(self.testerr))
			self.lc:unsubscribe(sub)
			return false
		end

		self.iteration = self.iteration + 1
	end

	info(string.format("lcmtest_%-17s : PASSED :-)", self.test.msgtype.shortname))
	self.lc:unsubscribe(sub)
	return true
end

local EchoTester = {}
EchoTester.__index = EchoTester

function EchoTester:new(lc)

	local obj = {}
	obj.lc = lc
	obj.data = nil
	obj.response_count = 0
	obj.num_iterations = 100
	setmetatable(obj, EchoTester)

	return obj
end

function EchoTester:handler(channel, data)

	if data == self.data then
		self.response_count = self.response_count + 1
	end
end

function EchoTester:run()

	local handler_closure = function(channel, data) self:handler(channel, data) end
	local sub = self.lc:subscribe("TEST_ECHO_REPLY", handler_closure)

	for i = 1, self.num_iterations do
		local datalen = math.random(10, 10000)
		local chars = {}
		for j = 1, datalen do
			chars[j] = string.char(math.random(0, 255))
		end
		self.data = table.concat(chars)
		self.lc:publish("TEST_ECHO", self.data)

		if not self.lc:timedhandle(500000) or self.response_count ~= i then
			info(string.format("echo test failed to receive a response on iteration %d!", i))
			self.lc:unsubscribe(sub)
			return false
		end
	end

	info(string.format("%-25s : PASSED :-)", "echo test"))
	self.lc:unsubscribe(sub)
	return true
end

local Tester = {}
Tester.__index = Tester

function Tester:new()

	local obj = {}
	setmetatable(obj, Tester)

	return obj
end

function Tester:run()

	-- make lcm connection
	local lc = lcm.lcm.new()

	-- run all tests
	EchoTester:new(lc):run()
	StandardTester:new(lc, PrimitivesTest:new()):run()
	StandardTester:new(lc, PrimitivesListTest:new()):run()
	StandardTester:new(lc, NodeTest:new()):run()
	StandardTester:new(lc, MultidimTest:new()):run()

	-- attempt to garbage collect the lcm connection
	lc = nil
	for i = 1, 1000 do
		collectgarbage()
	end

	info("All tests passed.")
end

-- now to actually do something
local test = Tester:new()
test:run()



