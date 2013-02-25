
#include "lualcm_pack.h"
#include "errno.h"
#include "stdint.h"
#include "stdlib.h"
#include "string.h"
#include "lualcm_hash.h"
#include "lua_ver_helper.h"

#ifndef __cplusplus
#include "stdbool.h"
#endif

/*
 * WARNING! This code assumes you have a normal lua build, where lua_Number
 * is a double. Packing and unpacking will be very awkward if lua_Number isn't
 * a floating point type. If lua_Number is float, that's ok, but it won't be
 * able to represent the full range of a 32 bit int.
 *
 * TODO Add warning for non floating point lua_Number.
 *
 * TODO Add warning for large ints that don't fit in a double.
 *
 * TODO Add warning for large ints that don't fit in a float (if lua_Number is
 * defined as float).
 */

/* functions */
static int impl_unpack(lua_State *);
static int impl_pack(lua_State *);
static int impl_prepare_string(lua_State *);
static int impl_trim_to_null(lua_State *);
static int impl_utf8_check(lua_State *);

/* supporting types */

typedef enum impl_datatype {
	DATATYPE_UNKNOWN,
	DATATYPE_INT8,
	DATATYPE_INT16,
	DATATYPE_INT32,
	DATATYPE_UINT32,
	DATATYPE_INT64,
	DATATYPE_FLOAT,
	DATATYPE_DOUBLE,
	DATATYPE_STRING,
	DATATYPE_BOOLEAN,
	DATATYPE_BYTE,
	DATATYPE_HASH,
} impl_datatype_t;

typedef struct impl_pack_op {
	impl_datatype_t datatype;
	size_t repeat; /* for strings, repeat is used as the string length */
} impl_pack_op_t;

typedef struct impl_pack_op_list {
	size_t num_ops;
	impl_pack_op_t * ops;
	bool big_endian;
} impl_pack_op_list_t;

/* supporting functions */
static bool impl_format_to_ops(impl_pack_op_t *, size_t, size_t *, bool *, const char *, const char **);
static size_t impl_get_required_buffer_size(impl_pack_op_t *, size_t);
static int impl_get_required_stack_size(impl_pack_op_t *, size_t);
static bool impl_unpack_buffer(lua_State *, impl_pack_op_t *, size_t, bool, const uint8_t *, size_t, const char **);
static bool impl_pack_buffer(lua_State *, impl_pack_op_t *, size_t, bool, uint8_t *, size_t, size_t *, const char **);
static bool impl_is_machine_little_endian(void);
static void impl_swap_bytes(uint8_t *, size_t);

/* unpack helper functions */
static void impl_unpack_int8_t(lua_State *, const uint8_t *, size_t *, size_t, bool);
static void impl_unpack_int16_t(lua_State *, const uint8_t *, size_t *, size_t, bool);
static void impl_unpack_int32_t(lua_State *, const uint8_t *, size_t *, size_t, bool);
static void impl_unpack_uint32_t(lua_State *, const uint8_t *, size_t *, size_t, bool);
static void impl_unpack_int64_t(lua_State *, const uint8_t *, size_t *, size_t, bool);
static void impl_unpack_float(lua_State *, const uint8_t *, size_t *, size_t, bool);
static void impl_unpack_double(lua_State *, const uint8_t *, size_t *, size_t, bool);
static void impl_unpack_string(lua_State *, const uint8_t *, size_t *, size_t, bool);
static void impl_unpack_boolean(lua_State *, const uint8_t *, size_t *, size_t, bool);
static void impl_unpack_byte(lua_State *, const uint8_t *, size_t *, size_t, bool);
static void impl_unpack_hash(lua_State *, const uint8_t *, size_t *, size_t, bool);

/* pack helper functions */
static void impl_pack_int8_t(lua_State *, uint8_t *, size_t *, int *, size_t, bool);
static void impl_pack_int16_t(lua_State *, uint8_t *, size_t *, int *, size_t, bool);
static void impl_pack_int32_t(lua_State *, uint8_t *, size_t *, int *, size_t, bool);
static void impl_pack_uint32_t(lua_State *, uint8_t *, size_t *, int *, size_t, bool);
static void impl_pack_int64_t(lua_State *, uint8_t *, size_t *, int *, size_t, bool);
static void impl_pack_float(lua_State *, uint8_t *, size_t *, int *, size_t, bool);
static void impl_pack_double(lua_State *, uint8_t *, size_t *, int *, size_t, bool);
static void impl_pack_string(lua_State *, uint8_t *, size_t *, int *, size_t, bool);
static void impl_pack_boolean(lua_State *, uint8_t *, size_t *, int *, size_t, bool);
static void impl_pack_byte(lua_State *, uint8_t *, size_t *, int *, size_t, bool);
static void impl_pack_hash(lua_State *, uint8_t *, size_t *, int *, size_t, bool);

/* forward declaration, from utf8_check.c */
int utf8_check(const char * s, size_t length);

void ll_pack_register(lua_State * L){

	const struct luaL_Reg functions[] = {
		{"pack", impl_pack},
		{"unpack", impl_unpack},
		{"prepare_string", impl_prepare_string},
		{"_trim_to_null", impl_trim_to_null},
		{"_utf8_check", impl_utf8_check},
		{NULL, NULL},
	};

	luaX_registerglobal(L, "lcm._pack", functions);
}

int impl_unpack(lua_State * L){

	/* get the format */
	const char * format = luaL_checkstring(L, 1);

	/* some variables used for reading the format */
	bool is_little_endian;
	size_t num_ops;
	impl_pack_op_t ops[20];

	/* variables used to check results */
	bool success;
	const char * error_message;

	/* turn format into ops */
	success = impl_format_to_ops(ops, 20, &num_ops,
			&is_little_endian, format, &error_message);

	/* check the result */
	if(!success){
		luaL_error(L, "error reading format: %s", error_message);
	}

	/* get number of values */
	const int num_values = impl_get_required_stack_size(ops, num_ops);

	/* TODO offer packing/unpacking into lua tables? */

	/* check the stack size, since some users may try to unpack many values */
	luaL_checkstack(L, num_values, "Does it look like I have infinite stack?!");

	/* get the buffer */
	size_t buf_size;
	const uint8_t * buf = (const uint8_t *) luaL_checklstring(L, 2, &buf_size);

	/* use ops to unpack */
	success = impl_unpack_buffer(L, ops, num_ops,
			is_little_endian, buf, buf_size, &error_message);

	/* check the result */
	if(!success){
		luaL_error(L, "error unpacking buffer: %s", error_message);
	}

	return num_values;
}

int impl_pack(lua_State * L){

	/* get the format */
	const char * format = luaL_checkstring(L, 1);

	/* some variables used for reading the format */
	bool is_little_endian;
	size_t num_ops;
	impl_pack_op_t ops[20];

	/* variables used to check results */
	bool success;
	const char * error_message;

	/* turn format into ops */
	success = impl_format_to_ops(ops, 20, &num_ops,
			&is_little_endian, format, &error_message);

	/* check the result */
	if(!success){
		luaL_error(L, "error reading format: %s", error_message);
	}

	/* make buffer */
	const size_t buf_size = impl_get_required_buffer_size(ops, num_ops);
	uint8_t * buf = (uint8_t *) malloc(buf_size);

	/* remove format string from the stack, pack requires this */
	lua_remove(L, 1);

	/* use ops to pack */
	size_t actual_buf_size;
	success = impl_pack_buffer(L, ops, num_ops, is_little_endian, buf, buf_size, &actual_buf_size, &error_message);

	/* check the result */
	if(!success){
		free(buf);
		luaL_error(L, "error packing buffer: %s", error_message);
	}

	/* at this point, buf_size and actual_buf_size should/will be the same */
	/* we only need to use actual_buf_size if we didn't know how big the buffer was going to be */

	/* printf("buf_size: %d, actual_buf_size: %d\n", buf_size, actual_buf_size); */

	/* push the buffer */
	lua_pushlstring(L, (const char *) buf, actual_buf_size);
	free(buf);

	return 1;
}

int impl_prepare_string(lua_State * L){

	/* get the string */
	const char * string = luaL_checkstring(L, 1);

	/* get the length (to first null) */
	size_t length = strlen(string);

	/* check UTF-8 */
	if(!utf8_check(string, length)){
		luaL_error(L, "string is not UTF-8");
	}

	/* push the string (null terminated) */
	lua_pushstring(L, string);

	return 1;
}

int impl_trim_to_null(lua_State * L){

	/* get the string */
	const char * string = luaL_checkstring(L, 1);

	/* push the string (null terminated) */
	lua_pushstring(L, string);

	return 1;
}

int impl_utf8_check(lua_State * L){

	/* get the string */
	size_t length;
	const char * string = luaL_checklstring(L, 1, &length);

	/* check UTF-8 */
	if(!utf8_check(string, length)){
		luaL_error(L, "string is not UTF-8");
	}

	return 1;
}

static bool impl_format_to_ops(impl_pack_op_t * ops, size_t max_num_ops, size_t * num_ops, bool * is_little_endian, const char * format, const char ** error_message){

	*num_ops = 0;
	bool repeat_set = false;

	char c = *format;

	if(c == '@' || c == '=' || c == '<' || c == '>' || c == '!'){
		switch(c){
		case '@': return false; /* not supported */ break;
		case '=': *is_little_endian = impl_is_machine_little_endian(); break;
		case '<': *is_little_endian = true; break;
		case '>': *is_little_endian = false; break;
		case '!': *is_little_endian = false; break;
		}
		c = *(++format);
	}else{
		/* default to native endianness */
		*is_little_endian = impl_is_machine_little_endian();
	}

	while(c != '\0'){

		if(*num_ops >= max_num_ops){
			/* too many ops error */
			*error_message = "too many operators in format string";
			return false;
		}

		if(c == 'b' || c == 'h' || c == 'i' || c == 'l' || c == 'I' || c == 'L'
				|| c == 'q' || c == 'f' || c == 'd' || c == 's' || c == '?' || c == 'B' || c == 'X'){

			switch(c){
			case 'b': ops[*num_ops].datatype = DATATYPE_INT8; break;
			case 'h': ops[*num_ops].datatype = DATATYPE_INT16; break;
			case 'i': case 'l': ops[*num_ops].datatype = DATATYPE_INT32; break;
			case 'I': case 'L': ops[*num_ops].datatype = DATATYPE_UINT32; break;
			case 'q': ops[*num_ops].datatype = DATATYPE_INT64; break;
			case 'f': ops[*num_ops].datatype = DATATYPE_FLOAT; break;
			case 'd': ops[*num_ops].datatype = DATATYPE_DOUBLE; break;
			case 's': ops[*num_ops].datatype = DATATYPE_STRING; break;
			case '?': ops[*num_ops].datatype = DATATYPE_BOOLEAN; break;
			case 'B': ops[*num_ops].datatype = DATATYPE_BYTE; break;
			case 'X': ops[*num_ops].datatype = DATATYPE_HASH; break;
			}

			if(!repeat_set){
				ops[*num_ops].repeat = 1;
			}

			(*num_ops)++;
			repeat_set = false;

			c = *(++format);

		}else if(c >= '0' && c <= '9'){

			errno = 0;

			/* format pointer set to point to the character following the last character interpreted */
			char * next_char;
			ops[*num_ops].repeat = strtol(format, &next_char, 10);
			repeat_set = true;

			if(errno != 0){
				/* conversion error */
				*error_message = "cannot parse number in format string";
				return false;
			}

			c = *(format = next_char);

		}else{
			/* format error */
			*error_message = "unexpected character in format string";
			return false;
		}
	}

	return true;
}

static size_t impl_get_required_buffer_size(impl_pack_op_t * ops, size_t num_ops){

	size_t buf_size = 0;

	int i;
	for(i = 0; i < num_ops; i++){
		switch(ops[i].datatype){
		case DATATYPE_INT8: buf_size += ops[i].repeat * sizeof(int8_t); break;
		case DATATYPE_INT16: buf_size += ops[i].repeat * sizeof(int16_t); break;
		case DATATYPE_INT32: buf_size += ops[i].repeat * sizeof(int32_t); break;
		case DATATYPE_UINT32: buf_size += ops[i].repeat * sizeof(uint32_t); break;
		case DATATYPE_INT64: buf_size += ops[i].repeat * sizeof(int64_t); break;
		case DATATYPE_FLOAT: buf_size += ops[i].repeat * sizeof(float); break;
		case DATATYPE_DOUBLE: buf_size += ops[i].repeat * sizeof(double); break;
		case DATATYPE_STRING: buf_size += ops[i].repeat * sizeof(char); break;
		case DATATYPE_BOOLEAN: buf_size += ops[i].repeat * sizeof(uint8_t); break;
		case DATATYPE_BYTE: buf_size += ops[i].repeat * sizeof(uint8_t); break;
		case DATATYPE_HASH: buf_size += ops[i].repeat * sizeof(uint64_t); break;
		}
	}

	return buf_size;
}

static int impl_get_required_stack_size(impl_pack_op_t * ops, size_t num_ops){

	int num_values = 0;

	int i;
	for(i = 0; i < num_ops; i++){
		switch(ops[i].datatype){
		case DATATYPE_INT8: num_values += ops[i].repeat; break;
		case DATATYPE_INT16: num_values += ops[i].repeat; break;
		case DATATYPE_INT32: num_values += ops[i].repeat; break;
		case DATATYPE_UINT32: num_values += ops[i].repeat; break;
		case DATATYPE_INT64: num_values += ops[i].repeat; break;
		case DATATYPE_FLOAT: num_values += ops[i].repeat; break;
		case DATATYPE_DOUBLE: num_values += ops[i].repeat; break;
		case DATATYPE_STRING: num_values += 1; break;
		case DATATYPE_BOOLEAN: num_values += ops[i].repeat; break;
		case DATATYPE_BYTE: num_values += ops[i].repeat; break;
		case DATATYPE_HASH: num_values += ops[i].repeat; break;
		}
	}

	return num_values;
}

static bool impl_unpack_buffer(lua_State * L, impl_pack_op_t * ops, size_t num_ops, bool is_little_endian, const uint8_t * buf, size_t buf_size, const char ** error_message){

	const size_t req_buf_size = impl_get_required_buffer_size(ops, num_ops);

	/* make sure we won't run out of buffer */
	if(req_buf_size > buf_size){
		*error_message = "buffer is too small";
		return false;
	}

	bool swap = false;
	if(is_little_endian != impl_is_machine_little_endian()){
		swap = true;
	}

	size_t offset = 0;

	int i;
	for(i = 0; i < num_ops; i++){
		switch(ops[i].datatype){
		case DATATYPE_INT8: impl_unpack_int8_t(L, buf, &offset, ops[i].repeat, swap); break;
		case DATATYPE_INT16: impl_unpack_int16_t(L, buf, &offset, ops[i].repeat, swap); break;
		case DATATYPE_INT32: impl_unpack_int32_t(L, buf, &offset, ops[i].repeat, swap); break;
		case DATATYPE_UINT32: impl_unpack_uint32_t(L, buf, &offset, ops[i].repeat, swap); break;
		case DATATYPE_INT64: impl_unpack_int64_t(L, buf, &offset, ops[i].repeat, swap); break;
		case DATATYPE_FLOAT: impl_unpack_float(L, buf, &offset, ops[i].repeat, swap); break;
		case DATATYPE_DOUBLE: impl_unpack_double(L, buf, &offset, ops[i].repeat, swap); break;
		case DATATYPE_STRING: impl_unpack_string(L, buf, &offset, ops[i].repeat, swap); break;
		case DATATYPE_BOOLEAN: impl_unpack_boolean(L, buf, &offset, ops[i].repeat, swap); break;
		case DATATYPE_BYTE: impl_unpack_byte(L, buf, &offset, ops[i].repeat, swap); break;
		case DATATYPE_HASH: impl_unpack_hash(L, buf, &offset, ops[i].repeat, swap); break;
		}
	}

	return true;
}

static bool impl_pack_buffer(lua_State * L, impl_pack_op_t * ops, size_t num_ops, bool is_little_endian, uint8_t * buf, size_t max_buf_size, size_t * buf_size, const char ** error_message){

	const size_t req_buf_size = impl_get_required_buffer_size(ops, num_ops);
	const int req_stack_size = impl_get_required_stack_size(ops, num_ops);

	/* this check ensures the offset will never exceed max_buf_size */
	if(req_buf_size > max_buf_size){
		*error_message = "buffer is too small";
		return false;
	}

	/* this assumes only arguments are on the stack */
	if(req_stack_size > lua_gettop(L)){
		*error_message = "missing arguments";
		return false;
	}

	bool swap = false;
	if(is_little_endian != impl_is_machine_little_endian()){
		swap = true;
	}

	size_t offset = 0;
	int stack_pos = -req_stack_size; /* remember, the top of the stack is position = -1 */

	int i;
	for(i = 0; i < num_ops; i++){
		switch(ops[i].datatype){
		case DATATYPE_INT8: impl_pack_int8_t(L, buf, &offset, &stack_pos, ops[i].repeat, swap); break;
		case DATATYPE_INT16: impl_pack_int16_t(L, buf, &offset, &stack_pos, ops[i].repeat, swap); break;
		case DATATYPE_INT32: impl_pack_int32_t(L, buf, &offset, &stack_pos, ops[i].repeat, swap); break;
		case DATATYPE_UINT32: impl_pack_uint32_t(L, buf, &offset, &stack_pos, ops[i].repeat, swap); break;
		case DATATYPE_INT64: impl_pack_int64_t(L, buf, &offset, &stack_pos, ops[i].repeat, swap); break;
		case DATATYPE_FLOAT: impl_pack_float(L, buf, &offset, &stack_pos, ops[i].repeat, swap); break;
		case DATATYPE_DOUBLE: impl_pack_double(L, buf, &offset, &stack_pos, ops[i].repeat, swap); break;
		case DATATYPE_STRING: impl_pack_string(L, buf, &offset, &stack_pos, ops[i].repeat, swap); break;
		case DATATYPE_BOOLEAN: impl_pack_boolean(L, buf, &offset, &stack_pos, ops[i].repeat, swap); break;
		case DATATYPE_BYTE: impl_pack_byte(L, buf, &offset, &stack_pos, ops[i].repeat, swap); break;
		case DATATYPE_HASH: impl_pack_hash(L, buf, &offset, &stack_pos, ops[i].repeat, swap); break;
		}
	}

	/* at this point, req_buf_size and offset will be the same */
	/* if offset is greater than req_buf_size, then we should worry about a segfault */

	/* printf("offset: %d, req_sz: %d\n", offset, req_buf_size); */

	*buf_size = offset; /* or req_buf_size */

	return true;
}

static bool impl_is_machine_little_endian(void){

	static union {
		uint32_t test_int;
		uint8_t test_bytes[4];
	} endian_test = {0x00000001};

	return endian_test.test_bytes[0] == 0x01;
}

static void impl_swap_bytes(uint8_t * buffer, size_t num){

	uint8_t tmp;
	uint8_t * front = &buffer[0];
	uint8_t * back = &buffer[num - 1];

	while(front < back){
		tmp = *front;
		*front++ = *back;
		*back-- = tmp;
	}
}

static void impl_unpack_int8_t(lua_State * L, const uint8_t * buf, size_t * offset, size_t repeat, bool swap){
	while(repeat-- > 0){
		int8_t n = *((int8_t *)(buf + *offset));
		//if(swap) swap_bytes((uint8_t *) &n, sizeof(int8_t));
		lua_pushnumber(L, n);
		*offset += sizeof(int8_t);
	}
}

static void impl_unpack_int16_t(lua_State * L, const uint8_t * buf, size_t * offset, size_t repeat, bool swap){
	while(repeat-- > 0){
		int16_t n = *((int16_t *)(buf + *offset));
		if(swap) impl_swap_bytes((uint8_t *) &n, sizeof(int16_t));
		lua_pushnumber(L, n);
		*offset += sizeof(int16_t);
	}
}

static void impl_unpack_int32_t(lua_State * L, const uint8_t * buf, size_t * offset, size_t repeat, bool swap){
	while(repeat-- > 0){
		int32_t n = *((int32_t *)(buf + *offset));
		if(swap) impl_swap_bytes((uint8_t *) &n, sizeof(int32_t));
		lua_pushnumber(L, n);
		*offset += sizeof(int32_t);
	}
}

static void impl_unpack_uint32_t(lua_State * L, const uint8_t * buf, size_t * offset, size_t repeat, bool swap){
	while(repeat-- > 0){
		uint32_t n = *((uint32_t *)(buf + *offset));
		if(swap) impl_swap_bytes((uint8_t *) &n, sizeof(uint32_t));
		lua_pushnumber(L, n);
		*offset += sizeof(uint32_t);
	}
}

static void impl_unpack_int64_t(lua_State * L, const uint8_t * buf, size_t * offset, size_t repeat, bool swap){

	static const int64_t DOUBLE_MAX_INT = 9007199254740992; /* 2^53 */
	static bool warned = false;

	while(repeat-- > 0){
		int64_t n = *((int64_t *)(buf + *offset));
		if(swap) impl_swap_bytes((uint8_t *) &n, sizeof(int64_t));
		if(!warned){
			if(n >= DOUBLE_MAX_INT || n <= -DOUBLE_MAX_INT){
				fprintf(stderr, "WARNING! Unpacking really large integers may result in loss of precision!\n");
			}
		}
		lua_pushnumber(L, n);
		*offset += sizeof(int64_t);
	}
}

static void impl_unpack_float(lua_State * L, const uint8_t * buf, size_t * offset, size_t repeat, bool swap){
	while(repeat-- > 0){
		float n = *((float *)(buf + *offset));
		if(swap) impl_swap_bytes((uint8_t *) &n, sizeof(float));
		lua_pushnumber(L, n);
		*offset += sizeof(float);
	}
}

static void impl_unpack_double(lua_State * L, const uint8_t * buf, size_t * offset, size_t repeat, bool swap){
	while(repeat-- > 0){
		double n = *((double *)(buf + *offset));
		if(swap) impl_swap_bytes((uint8_t *) &n, sizeof(double));
		lua_pushnumber(L, n);
		*offset += sizeof(double);
	}
}

static void impl_unpack_string(lua_State * L, const uint8_t * buf, size_t * offset, size_t str_size, bool swap){
	const char * str = (const char *)(buf + *offset);
	lua_pushlstring(L, str, str_size);
	*offset += str_size * sizeof(char);
}

static void impl_unpack_boolean(lua_State * L, const uint8_t * buf, size_t * offset, size_t repeat, bool swap){
	while(repeat-- > 0){
		uint8_t n = *((uint8_t *)(buf + *offset));
		//if(swap) swap_bytes((uint8_t *) &n, sizeof(uint8_t));
		lua_pushboolean(L, (int)n);
		*offset += sizeof(uint8_t);
	}
}

static void impl_unpack_byte(lua_State * L, const uint8_t * buf, size_t * offset, size_t repeat, bool swap){
	while(repeat-- > 0){
		uint8_t n = *((uint8_t *)(buf + *offset));
		//if(swap) swap_bytes((uint8_t *) &n, sizeof(uint8_t));
		lua_pushnumber(L, n);
		*offset += sizeof(uint8_t);
	}
}

static void impl_unpack_hash(lua_State * L, const uint8_t * buf, size_t * offset, size_t repeat, bool swap){
	while(repeat-- > 0){
		uint64_t hash = *((uint64_t *)(buf + *offset));
		if(swap) impl_swap_bytes((uint8_t *) &hash, sizeof(uint64_t));
		ll_hash_fromvalue(L, hash);
		*offset += sizeof(uint64_t);
	}
}

static void impl_pack_int8_t(lua_State * L, uint8_t * buf, size_t * offset, int * stack_pos, size_t repeat, bool swap){
	while(repeat-- > 0){
		int8_t n = (int8_t) luaL_checknumber(L, *stack_pos);
		*((int8_t *)(buf + *offset)) = n;
		//if(swap) swap_bytes(buf + *offset, sizeof(int8_t));
		*offset += sizeof(int8_t);
		*stack_pos += 1;
	}
}

static void impl_pack_int16_t(lua_State * L, uint8_t * buf, size_t * offset, int * stack_pos, size_t repeat, bool swap){
	while(repeat-- > 0){
		int16_t n = (int16_t) luaL_checknumber(L, *stack_pos);
		*((int16_t *)(buf + *offset)) = n;
		if(swap) impl_swap_bytes(buf + *offset, sizeof(int16_t));
		*offset += sizeof(int16_t);
		*stack_pos += 1;
	}
}

static void impl_pack_int32_t(lua_State * L, uint8_t * buf, size_t * offset, int * stack_pos, size_t repeat, bool swap){
	while(repeat-- > 0){
		int32_t n = (int32_t) luaL_checknumber(L, *stack_pos);
		*((int32_t *)(buf + *offset)) = n;
		if(swap) impl_swap_bytes(buf + *offset, sizeof(int32_t));
		*offset += sizeof(int32_t);
		*stack_pos += 1;
	}
}

static void impl_pack_uint32_t(lua_State * L, uint8_t * buf, size_t * offset, int * stack_pos, size_t repeat, bool swap){
	while(repeat-- > 0){
		uint32_t n = (uint32_t) luaL_checknumber(L, *stack_pos);
		*((uint32_t *)(buf + *offset)) = n;
		if(swap) impl_swap_bytes(buf + *offset, sizeof(uint32_t));
		*offset += sizeof(uint32_t);
		*stack_pos += 1;
	}
}

static void impl_pack_int64_t(lua_State * L, uint8_t * buf, size_t * offset, int * stack_pos, size_t repeat, bool swap){
	while(repeat-- > 0){
		int64_t n = (int64_t) luaL_checknumber(L, *stack_pos);
		*((int64_t *)(buf + *offset)) = n;
		if(swap) impl_swap_bytes(buf + *offset, sizeof(int64_t));
		*offset += sizeof(int64_t);
		*stack_pos += 1;
	}
}

static void impl_pack_float(lua_State * L, uint8_t * buf, size_t * offset, int * stack_pos, size_t repeat, bool swap){
	while(repeat-- > 0){
		float n = (float) luaL_checknumber(L, *stack_pos);
		*((float *)(buf + *offset)) = n;
		if(swap) impl_swap_bytes(buf + *offset, sizeof(float));
		*offset += sizeof(float);
		*stack_pos += 1;
	}
}

static void impl_pack_double(lua_State * L, uint8_t * buf, size_t * offset, int * stack_pos, size_t repeat, bool swap){
	while(repeat-- > 0){
		double n = (double) luaL_checknumber(L, *stack_pos);
		*((double *)(buf + *offset)) = n;
		if(swap) impl_swap_bytes(buf + *offset, sizeof(double));
		*offset += sizeof(double);
		*stack_pos += 1;
	}
}

static void impl_pack_string(lua_State * L, uint8_t * buf, size_t * offset, int * stack_pos, size_t str_size, bool swap){
	size_t other_str_size;
	const char * other_str = luaL_checklstring(L, *stack_pos, &other_str_size);
	char * str = (char *)(buf + *offset);

	int i;
	for(i = 0; i < str_size; i++){
		if(i < other_str_size){
			str[i] = other_str[i];
		}else{
			/* pad end of str with null characters */
			str[i] = '\0';
		}
	}

	*offset += str_size * sizeof(char);
	*stack_pos += 1;
}

static void impl_pack_boolean(lua_State * L, uint8_t * buf, size_t * offset, int * stack_pos, size_t repeat, bool swap){
	while(repeat-- > 0){
		luaL_checkany(L, *stack_pos); /* because lua_toboolean also returns 0 on invalid stack index */
		uint8_t n = (uint8_t) lua_toboolean(L, *stack_pos);
		*((uint8_t *)(buf + *offset)) = n;
		//if(swap) swap_bytes(buf + *offset, sizeof(uint8_t));
		*offset += sizeof(uint8_t);
		*stack_pos += 1;
	}
}

static void impl_pack_byte(lua_State * L, uint8_t * buf, size_t * offset, int * stack_pos, size_t repeat, bool swap){
	while(repeat-- > 0){
		uint8_t n = (uint8_t) luaL_checknumber(L, *stack_pos);
		*((uint8_t *)(buf + *offset)) = n;
		//if(swap) swap_bytes(buf + *offset, sizeof(uint8_t));
		*offset += sizeof(uint8_t);
		*stack_pos += 1;
	}
}

static void impl_pack_hash(lua_State * L, uint8_t * buf, size_t * offset, int * stack_pos, size_t repeat, bool swap){
	while(repeat-- > 0){
		*((uint64_t *)(buf + *offset)) = ll_hash_tovalue(L, *stack_pos);
		if(swap) impl_swap_bytes(buf + *offset, sizeof(uint64_t));
		*offset += sizeof(uint64_t);
		*stack_pos += 1;
	}
}
