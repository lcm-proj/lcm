

#include "lualcm_hash.h"
#include "stdio.h"
#include "lua_ver_helper.h"

/** @file */

/* hash userdata */
typedef struct impl_hash_userdata {
	uint64_t hash; /**< The actual 64-bit integer used as the hash. */
} impl_hash_userdata_t;

/* methods */
static int impl_hash_new(lua_State *);
static int impl_hash_tobytes(lua_State *);
static int impl_hash_rotate(lua_State *);

/* metamethods */
static int impl_hash_add(lua_State *);
static int impl_hash_tostring(lua_State *);

/* supporting functions */
static impl_hash_userdata_t * impl_hash_newuserdata(lua_State *);
static impl_hash_userdata_t * impl_hash_checkudata(lua_State *, int);

/**
 * Makes the hash userdata's metatable. The metatable is named "lcm._hash".
 *
 * @post A metatable exists named "lcm._hash" and contains
 *     all of the hash userdata's member functions.
 *
 * @param L The Lua state.
 */
void ll_hash_makemetatable(lua_State * L){

	/* create empty meta table */
	if(!luaL_newmetatable(L, "lcm._hash")){
		lua_pushstring(L, "cannot create metatable");
		lua_error(L);
	}

	const struct luaL_Reg metas[] = {
		{"__add", impl_hash_add},
		{"__tostring", impl_hash_tostring},
		{NULL, NULL},
	};

	/* register to meta */
	luaX_registertable(L, metas);

	const struct luaL_Reg methods[] = {
		{"tobytes", impl_hash_tobytes},
		{"rotate", impl_hash_rotate},
		{NULL, NULL},
	};

	/* register methods to new table, set __index */
	lua_pushstring(L, "__index");
	lua_newtable(L);
	luaX_registertable(L, methods);
	lua_rawset(L, -3);

	/* TODO hide metatable */
	/*lua_pushstring(L, "__metatable");
	lua_pushnil(L);
	lua_rawset(L, -3);*/

	/* pop the metatable */
	lua_pop(L, 1);
}

/**
 * Registers the hash userdata's "new" function.
 *
 * @post All LCM functions have been added to the LCM module.
 *     The modules table is on the top of the stack.
 *
 * @param L The Lua state.
 */
void ll_hash_register_new(lua_State * L){

	const struct luaL_Reg new_function[] = {
		{"new", impl_hash_new},
		{NULL, NULL},
	};

	luaX_registerglobal(L, "lcm._hash", new_function);
}

/**
 *
 */
void ll_hash_fromvalue(lua_State * L, uint64_t hash){
	impl_hash_userdata_t * hashu = impl_hash_newuserdata(L);
	hashu->hash = hash;
}

/**
 *
 */
uint64_t ll_hash_tovalue(lua_State * L, int index){
	impl_hash_userdata_t * hashu = impl_hash_checkudata(L, index);
	return hashu->hash;
}

/**
 * Creates and initializes an hash userdata.
 *
 * Lua does not support 64-bit integers, so the hash is initialized using a
 * string representing 64-bit hex number using hexdecimal,
 * i.e. "0x0123456701234567". The number is parsed by sscanf.
 *
 * @pre The Lua arguments on the stack:
 *     A string representing a 64-bit number.
 *
 * @post The Lua return values on the stack:
 *     The new hash userdata.
 *
 * @param L The Lua state.
 * @return The number of return values on the Lua stack.
 *
 * @throws Lua error if hash userdata cannot be created.
 */
static int impl_hash_new(lua_State * L){

	/* we expect 1 argument */
	lua_settop(L, 1);

	/* first arg is a string containing the provider */
	const char * hash_str = luaL_checkstring(L, 1);

	/* use sscanf to parse the string */
	/* should use uint64_t here, but it causes a warning  with sscanf */
	unsigned long long hash = 0;
	int matches = sscanf(hash_str, "%llx", &hash);
	if(matches != 1){
		lua_pushstring(L, "error creating hash");
		lua_error(L);
	}

	/* create hash userdata */
	impl_hash_userdata_t * hashu = impl_hash_newuserdata(L);
	hashu->hash = hash;

	/* return hash userdata, which is on top of the stack */

	return 1;
}

/**
 * Convert the hash userdata to bytes.
 *
 * TODO Endianness?
 *
 * @pre The Lua arguments on the stack:
 *     A hash userdata (self).
 *
 * @post The Lua return values on the stack:
 *     Eight integers representing the bytes of the hash.
 *
 * @param L The Lua state.
 * @return The number of return values on the Lua stack.
 */
static int impl_hash_tobytes(lua_State * L){

	/* we expect 1 argument */
	lua_settop(L, 1);

	/* get the hash userdata */
	impl_hash_userdata_t * hashu = impl_hash_checkudata(L, 1);

	uint8_t * bytes = (uint8_t *) &hashu->hash;

	int i;
	for(i = 0; i < sizeof(hashu->hash); i++){
		lua_pushinteger(L, bytes[i]);
	}

	return i;
}

/**
 * Rotates a hash userdata to the left.
 *
 * The number of bits to rotate is given as an argument. Bits are shifted left,
 * bits shifted past the MSB wrap around to the LSB.
 *
 * The number of bits to rotate should be between 0 and 63, but other integers
 * are accepted, including negative values. The number of bits to rotate is
 * always adjusted to the range of 0 to 63.
 *
 * The rotation is actually done by:
 * @c hash = hash << rotation | hash >> 64 - rotation;
 *
 * @pre The Lua arguments on the stack:
 *     A hash userdata (self), an integer containing the number of bits to
 *     rotate.
 *
 * @post The Lua return values on the stack:
 *     The hash userdata.
 *
 * @param L The Lua state.
 * @return The number of return values on the Lua stack.
 */
static int impl_hash_rotate(lua_State * L){

	/* we expect 2 arguments */
	lua_settop(L, 2);

	/* get the hash userdata */
	impl_hash_userdata_t * hashu = impl_hash_checkudata(L, 1);

	/* get the rotation */
	lua_Integer rotation = luaL_checkinteger(L, 2);

	/* do a fancy modulo, because 64 is 2^8, result is always positive*/
	rotation &= 63;

	hashu->hash = hashu->hash << rotation | hashu->hash >> 64 - rotation;

	/* pop the rotation int from the stack, return the hash */
	lua_pop(L, 1);

	return 1;
}

/**
 * Adds two hash userdatas. This is the __add metamethod of the hash
 * userdata.
 *
 * @pre The Lua arguments on the stack:
 *     Two hash userdatas (one of them being self).
 *
 * @post The Lua return values on the stack:
 *     A sumed hash userdata.
 *
 * @param L The Lua state.
 * @return The number of return values on the Lua stack.
 */
static int impl_hash_add(lua_State * L){

	/* we expect 2 arguments */
	lua_settop(L, 2);

	/* get the hash userdatas */
	impl_hash_userdata_t * left_hashu = impl_hash_checkudata(L, 1);
	impl_hash_userdata_t * right_hashu = impl_hash_checkudata(L, 2);

	/* make result */
	impl_hash_userdata_t * new_hashu = impl_hash_newuserdata(L);
	new_hashu->hash = left_hashu->hash + right_hashu->hash;

	return 1;
}

/**
 * Creates a string from a hash userdata. This is the __tostring metamethod
 * of the hash userdata.
 *
 * @pre The Lua arguments on the stack:
 *     A hash userdata (self).
 *
 * @post The Lua return values on the stack:
 *     A string representing the hash userdata.
 *
 * @param L The Lua state.
 * @return The number of return values on the Lua stack.
 */
static int impl_hash_tostring(lua_State * L){

	/* we expect 1 argument */
	lua_settop(L, 1);

	/* get the hash userdata */
	impl_hash_userdata_t * hashu = impl_hash_checkudata(L, 1);

	/* convert uint64_t to string */
	/* use snprintf because lua_pushfstring can't handle %llx */
	/* have to cast uint64_t to avoid warning with snprintf */
	char hash_str[20];
#ifndef WIN32
	snprintf(hash_str, 20, "0x%llx", (unsigned long long) hashu->hash);
#else
	_snprintf(hash_str, 20, "0x%llx", (unsigned long long) hashu->hash);
#endif
	/* make the string */
	lua_pushfstring(L, "lcm._hash = %s (@ %p)", hash_str, hashu);

	return 1;
}

/**
 * Creates a new hash userdata. The userdata is defined using the
 * "lcm._hash" metatable.
 *
 * @pre The Lua arguments on the stack:
 *     Nothing.
 *
 * @pre The metatable "lcm._hash" is defined.
 *
 * @post The Lua return values on the stack:
 *     The new hash userdata.
 *
 * @param L The Lua state.
 * @return A pointer to the new hash userdata.
 */
static impl_hash_userdata_t * impl_hash_newuserdata(lua_State * L){

	/* make new user data */
	impl_hash_userdata_t * hashu =
			(impl_hash_userdata_t *)
			lua_newuserdata(L, sizeof(impl_hash_userdata_t));

	/* initialize struct */
	hashu->hash = 0;

	/* set the metatable */
	luaL_getmetatable(L, "lcm._hash");
	if(lua_isnil(L, -1)){
		lua_pushstring(L, "cannot find metatable");
		lua_error(L);
	}
	lua_setmetatable(L, -2);

	return hashu;
}

/**
 * Checks for a hash userdata at the given index. Checks the userdata type
 * using the "lcm._hash" metatable.
 *
 * @pre The Lua arguments on the stack:
 *     A hash userdata at the given index.
 *
 * @pre The metatable "lcm._hash" is defined.
 *
 * @post The Lua return values on the stack:
 *     Nothing.
 *
 * @param L The Lua state.
 * @param index The index of the hash userdata.
 * @return A pointer to the hash userdata.
 */
static impl_hash_userdata_t * impl_hash_checkudata(lua_State * L, int index){
	return (impl_hash_userdata_t *)
		  luaL_checkudata(L, index, "lcm._hash");
}

