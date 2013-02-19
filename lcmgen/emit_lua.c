#include <stdio.h>
#include <stdint.h>
#include <assert.h>
#include <string.h>
#include <stdlib.h>
#include <ctype.h>
#include <sys/stat.h>
#include <sys/types.h>

#ifdef WIN32
#define __STDC_FORMAT_MACROS
#include <inttypes.h>
#include <lcm/windows/WinPorting.h>
#include <windows.h>
#else
#include <inttypes.h>
#include <unistd.h>
#include <fcntl.h>
#endif

#include <glib.h>

#include "lcmgen.h"

// lua uses just 2 spaces per indent
#define INDENT(n) (2*(n))

#define emit_start(n, ...) do { fprintf(f, "%*s", INDENT(n), ""); fprintf(f, __VA_ARGS__); } while (0)
#define emit_continue(...) do { fprintf(f, __VA_ARGS__); } while (0)
#define emit_end(...) do { fprintf(f, __VA_ARGS__); fprintf(f, "\n"); } while (0)
#define emit(n, ...) do { fprintf(f, "%*s", INDENT(n), ""); fprintf(f, __VA_ARGS__); fprintf(f, "\n"); } while (0)

#define err(...) fprintf (stderr, __VA_ARGS__)

static void 
mkdir_with_parents (const char *path, mode_t mode)
{
#ifdef WIN32
    g_mkdir_with_parents(path, 0755);
#else
    int len = strlen(path);
    for (int i = 0; i < len; i++) {
        if (path[i]=='/') {
            char *dirpath = (char *) malloc(i+1);
            strncpy(dirpath, path, i);
            dirpath[i]=0;

            mkdir(dirpath, mode);
            free(dirpath);

            i++; // skip the '/'
        }
    }
#endif
}

static char * 
build_filenamev (char **parts)
{
    char **p = parts;
    int total_len = 0;
    for (p = parts; *p; p++) {
        total_len += strlen (*p) + strlen(G_DIR_SEPARATOR_S);
    }
    total_len ++;
    char *result = (char *) malloc(total_len);
    memset(result, 0, total_len);
    for (p = parts; *p; p++) {
        if (! strlen(*p)) continue;
        strncat(result, *p, total_len);
        if (*(p+1)) 
            strncat(result, G_DIR_SEPARATOR_S, total_len);
    }
    return result;
}

static void
get_all_vals_helper (gpointer key, gpointer value, gpointer user_data)
{
    GPtrArray *vals = (GPtrArray*) user_data;

    g_ptr_array_add(vals, value);    
}

static GPtrArray * 
_hash_table_get_vals (GHashTable *hash_table)
{
    GPtrArray *vals = g_ptr_array_sized_new(g_hash_table_size(hash_table));
    g_hash_table_foreach (hash_table, get_all_vals_helper, vals);
    return vals;
}

void setup_lua_options(getopt_t *gopt)
{
    getopt_add_string(gopt, 0,   "lpath",     "",
            "Lua destination directory");
}

static int
is_same_type (const lcm_typename_t *tn1, const lcm_typename_t *tn2) {
    return ! strcmp (tn1->lctypename, tn2->lctypename);
}

static const char *
nil_initializer_string(const lcm_typename_t *type)
{
    if (!strcmp(type->lctypename, "byte")) return "0";
    if (!strcmp(type->lctypename, "boolean")) return "false";
    if (!strcmp(type->lctypename, "int8_t")) return "0";
    if (!strcmp(type->lctypename, "int16_t")) return "0";
    if (!strcmp(type->lctypename, "int32_t")) return "0";
    if (!strcmp(type->lctypename, "int64_t")) return "0";
    if (!strcmp(type->lctypename, "float")) return "0.0";
    if (!strcmp(type->lctypename, "double")) return "0.0";
    if (!strcmp(type->lctypename, "string")) return "''";
    else return "nil";
}

static char
_struct_format (lcm_member_t *lm) 
{
    const char *tn = lm->type->lctypename;
    if (!strcmp ("byte", tn)) return 'B';
    if (!strcmp ("boolean", tn)) return '?';
    if (!strcmp ("int8_t", tn)) return 'b';
    if (!strcmp ("int16_t", tn)) return 'h';
    if (!strcmp ("int32_t", tn)) return 'i';
    if (!strcmp ("int64_t", tn)) return 'q';
    if (!strcmp ("float", tn)) return 'f';
    if (!strcmp ("double", tn)) return 'd';
    return 0;
}

static int
_primitive_type_size (const char *tn)
{
    if (!strcmp ("byte", tn)) return 1;
    if (!strcmp ("boolean", tn)) return 1;
    if (!strcmp ("int8_t", tn)) return 1;
    if (!strcmp ("int16_t", tn)) return 2;
    if (!strcmp ("int32_t", tn)) return 4;
    if (!strcmp ("int64_t", tn)) return 8;
    if (!strcmp ("float", tn)) return 4;
    if (!strcmp ("double", tn)) return 8;
    assert (0);
    return 0;
}

static char *
escape_typename_to_variablename(const char * tn){

	char const * varname = g_strdup(tn);
	char * nameptr = (char *) varname;

	while(*nameptr != '\0'){
		if(*nameptr == '.'){
			*nameptr = '_';
		}
		++nameptr;
	}

	return (char *) varname;
}

static void
_emit_decode_one (const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls, 
        lcm_member_t *lm, const char *accessor, int indent)
{
	// XXX probably needs some rework
    const char *tn = lm->type->lctypename;
    const char *mn = lm->membername;
    const char *sn = lm->type->shortname;
    if (!strcmp ("string", tn)) {
        emit (indent, "local __%s_tmpstrlen = lcm._pack.unpack('>I', data:read(4))", mn);
        emit (indent, "%s = lcm._pack.prepare_string(data:read(__%s_tmpstrlen))",
                accessor, mn);
    } else if (!strcmp ("byte", tn)) {
        emit (indent, "%s = lcm._pack.unpack('>B', data:read(1))", accessor);
    } else if (!strcmp ("int8_t", tn)) {
        emit (indent, "%s = lcm._pack.unpack('>b', data:read(1))", accessor);
    } else if (!strcmp ("boolean", tn)) {
        emit (indent, "%s = lcm._pack.unpack('>?', data:read(1))", accessor);
    } else if (!strcmp ("int16_t", tn)) {
        emit (indent, "%s = lcm._pack.unpack('>h', data:read(2))", accessor);
    } else if (!strcmp ("int32_t", tn)) {
        emit (indent, "%s = lcm._pack.unpack('>i', data:read(4))", accessor);
    } else if (!strcmp ("int64_t", tn)) {
        emit (indent, "%s = lcm._pack.unpack('>q', data:read(8))", accessor);
    } else if (!strcmp ("float", tn)) {
        emit (indent, "%s = lcm._pack.unpack('>f', data:read(4))", accessor);
    } else if (!strcmp ("double", tn)) {
        emit (indent, "%s = lcm._pack.unpack('>d', data:read(8))", accessor);
    } else {
    	// XXX not really sure about these...
    	// check if same type
        if (is_same_type(lm->type, ls->structname)) {
            emit (indent, "%s = %s._decode_one(data)", accessor, sn);
        } else {
        	char *variablename = escape_typename_to_variablename(tn);
            emit (indent, "%s = %s._decode_one(data)", accessor, variablename);
            g_free(variablename);
        }
    }
}

static void
_emit_decode_list(const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls,
        lcm_member_t *lm, const char *accessor, int indent,
        const char *len, int fixed_len)
{
    const char *tn = lm->type->lctypename;
    if (!strcmp ("byte", tn) ||
		!strcmp ("int8_t", tn) ||
		!strcmp ("boolean", tn) ||
		!strcmp ("int16_t", tn) ||
		!strcmp ("int32_t", tn) ||
		!strcmp ("int64_t", tn) ||
		!strcmp ("float", tn) ||
		!strcmp ("double", tn)) {
        if(fixed_len) {
            emit (indent, "%s = {lcm._pack.unpack('>%s%c', data:read(%d))}",
                    accessor, len, _struct_format(lm), 
                    atoi(len) * _primitive_type_size(tn));
        } else {
            if(_primitive_type_size(tn) > 1) {
                emit (indent, "%s = {lcm._pack.unpack(string.format('>%%d%c', obj.%s), data:read(obj.%s * %d))}",
                	accessor, _struct_format(lm), len, len, _primitive_type_size(tn));
            } else {
            	emit (indent, "%s = {lcm._pack.unpack(string.format('>%%d%c', obj.%s), data:read(obj.%s))}",
                    accessor, _struct_format(lm), len, len);
            }
        }
    } else {
        assert(0);
    }
}

static void
_flush_read_struct_fmt (const lcmgen_t *lcm, FILE *f, 
        GQueue *formats, GQueue *members)
{
    int nfmts = g_queue_get_length(formats);
    assert (nfmts == g_queue_get_length (members));
    if(nfmts == 0) 
        return;

    emit_start(1, ""); // for indent
    int fmtsize = 0;
    while (! g_queue_is_empty (members)) {
        lcm_member_t *lm = (lcm_member_t*) g_queue_pop_head (members);
        emit_continue ("obj.%s", lm->membername);
        if (! g_queue_is_empty (members)) {
            emit_continue (", ");
        }
        fmtsize += _primitive_type_size (lm->type->lctypename);
    }
    emit_continue (" = lcm._pack.unpack('>");
    while (! g_queue_is_empty (formats)) {
        emit_continue ("%c", GPOINTER_TO_INT (g_queue_pop_head (formats)));
    }
    emit_end ("', data:read(%d))", fmtsize);
}

static void
emit_lua_decode_one (const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls)
{
    emit(0, "function %s._decode_one(data)", ls->structname->shortname);
    emit(0, "");
    emit(0, "  if not data.read then");
    emit(0, "    data = _buffer_helper:new(data)");
    emit(0, "  end");
    emit(0, "");
    emit(0, "  local obj = %s:new()", ls->structname->shortname);
    emit(0, "");

    GQueue *struct_fmt = g_queue_new ();
    GQueue *struct_members = g_queue_new ();

    for (unsigned int m = 0; m < g_ptr_array_size(ls->members); m++) {
        lcm_member_t *lm = (lcm_member_t *) g_ptr_array_index(ls->members, m);
        char fmt = _struct_format (lm);

        if (! lm->dimensions->len) {
            if (fmt) {
                g_queue_push_tail (struct_fmt, GINT_TO_POINTER ((int)fmt));
                g_queue_push_tail (struct_members, lm);
            } else {
                _flush_read_struct_fmt (lcm, f, struct_fmt, struct_members);
                char *accessor = g_strdup_printf("obj.%s", lm->membername);
                _emit_decode_one (lcm, f, ls, lm, accessor, 1);
                g_free(accessor);
            }
        } else {
            _flush_read_struct_fmt (lcm, f, struct_fmt, struct_members);
            GString *accessor = g_string_new ("");
            g_string_append_printf (accessor, "obj.%s", lm->membername);

            // iterate through the dimensions of the member, building up
            // an accessor string, and emitting for loops
            int n;
            for (n=0; n<lm->dimensions->len - 1; n++) {
                lcm_dimension_t *dim =
                	(lcm_dimension_t *) g_ptr_array_index (lm->dimensions, n);

                emit (1+n, "%s = {}", accessor->str);
                if (dim->mode == LCM_CONST) {
                    emit (1+n, "for i%d = 1, %s do", n, dim->size);
                } else {
                    emit (1+n, "for i%d = 1, obj.%s do", n, dim->size);
                }
                g_string_append_printf(accessor, "[i%d]", n);
            }

            // last dimension.
            lcm_dimension_t *last_dim = (lcm_dimension_t *) g_ptr_array_index(lm->dimensions,
                    lm->dimensions->len - 1);
            int last_dim_fixed_len = last_dim->mode == LCM_CONST;

            if(lcm_is_primitive_type(lm->type->lctypename) && 
               0 != strcmp(lm->type->lctypename, "string")) {
                // member is a primitive non-string type.  Emit code to 
                // decode a full array in one call to struct.unpack

            	_emit_decode_list(lcm, f, ls, lm,
                        accessor->str, 1+n, last_dim->size, last_dim_fixed_len);
            } else {
                // member is either a string type or an inner LCM type.  Each
                // array element must be decoded individually
            	emit (1+n, "%s = {}", accessor->str);
            	if (last_dim_fixed_len) {
            		emit (1+n, "for i%d = 1, %s do", n, last_dim->size);
            	} else {
            		emit (1+n, "for i%d = 1, obj.%s do", n, last_dim->size);
            	}
            	g_string_append_printf (accessor, "[i%d]", n);
            	_emit_decode_one (lcm, f, ls, lm, accessor->str, n+2);
            	emit (1+n, "end");
            }

            g_string_free (accessor, TRUE);

            while ( --n >= 0 ) {
            	emit (1+n, "end");
            }
        }
    }
    _flush_read_struct_fmt (lcm, f, struct_fmt, struct_members);

    g_queue_free (struct_fmt);
    g_queue_free (struct_members);

    emit(0, "");
    emit(0, "  return obj");
    emit(0, "end");
    emit(0, "");
}

static void
emit_lua_decode (const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls)
{
	const char *sn = ls->structname->shortname;

	emit(0, "function %s.decode(data)", sn);
    emit(0, "");
    emit(0, "  if data:sub(1, 8) ~= %s._packed_fingerprint then", sn);
    emit(0, "    error('bad fingerprint')");
    emit(0, "  end");
    emit(0, "");
    emit(0, "  return %s._decode_one(data:sub(9))", sn);
    emit(0, "end");
    emit(0, "");
}

static void
_emit_encode_one (const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls, 
        lcm_member_t *lm, const char *accessor, int indent)
{
	// XXX luaified, but might need some work
    const char *tn = lm->type->lctypename;
    const char *mn = lm->membername;
    if (!strcmp ("string", tn)) {
        emit (indent, "local __%s_tmpstr = lcm._pack.prepare_string(%s)", mn, accessor);
        emit (indent, "table.insert(buf_table, lcm._pack.pack('>I', #__%s_tmpstr + 1))", mn);
        emit (indent, "table.insert(buf_table, __%s_tmpstr .. '\\0')", mn);
    } else if (!strcmp ("byte", tn)) {
        emit (indent, "table.insert(buf_table, lcm._pack.pack('>B', %s))", accessor);
    } else if (!strcmp ("int8_t", tn)) {
        emit (indent, "table.insert(buf_table, lcm._pack.pack('>b', %s))", accessor);
    } else if (!strcmp ("boolean", tn)) {
        emit (indent, "table.insert(buf_table, lcm._pack.pack('>?', %s))", accessor);
    } else if (!strcmp ("int16_t", tn)) {
        emit (indent, "table.insert(buf_table, lcm._pack.pack('>h', %s))", accessor);
    } else if (!strcmp ("int32_t", tn)) {
        emit (indent, "table.insert(buf_table, lcm._pack.pack('>l', %s))", accessor);
    } else if (!strcmp ("int64_t", tn)) {
        emit (indent, "table.insert(buf_table, lcm._pack.pack('>q', %s))", accessor);
    } else if (!strcmp ("float", tn)) {
        emit (indent, "table.insert(buf_table, lcm._pack.pack('>f', %s))", accessor);
    } else if (!strcmp ("double", tn)) {
        emit (indent, "table.insert(buf_table, lcm._pack.pack('>d', %s))", accessor);
    } else {
        emit (indent, "table.insert(buf_table, %s:_encode_one())", accessor);
    }
}

static void
_emit_encode_list(const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls,
        lcm_member_t *lm, const char *accessor, int indent, 
        const char *len, int fixed_len)
{
    const char *tn = lm->type->lctypename;
    if (!strcmp ("byte", tn) ||
    	!strcmp ("boolean", tn) ||
        !strcmp ("int8_t", tn) ||
        !strcmp ("int16_t", tn) ||
        !strcmp ("int32_t", tn) ||
        !strcmp ("int64_t", tn) ||
        !strcmp ("float", tn) ||
        !strcmp ("double", tn)) {
        if(fixed_len) {
            emit(indent, "table.insert(buf_table, lcm._pack.pack('>%s%c', unpack(%s)))",
            	len, _struct_format(lm), accessor);
        } else {
            emit(indent, "table.insert(buf_table, lcm._pack.pack(string.format('>%%d%c', self.%s), unpack(%s)))",
            	_struct_format(lm), len, accessor);
        }
    } else {
        assert(0);
    }

}

static void
_flush_write_struct_fmt (FILE *f, GQueue *formats, GQueue *members)
{
	// XXX encode primitive members in one line
    assert (g_queue_get_length (formats) == g_queue_get_length (members));
    if (g_queue_is_empty (formats)) return;
    emit_start (1, "table.insert(buf_table, lcm._pack.pack('>");
    while (! g_queue_is_empty (formats)) {
        emit_continue ("%c", GPOINTER_TO_INT (g_queue_pop_head (formats)));
    }
    emit_continue ("', ");
    while (! g_queue_is_empty (members)) {
        lcm_member_t *lm = (lcm_member_t*) g_queue_pop_head (members);
        emit_continue ("self.%s", lm->membername);
        if (! g_queue_is_empty (members)) {
            emit_continue (", ");
        }
    }
    emit_end ("))");
}

static void
emit_lua_encode_one (const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls)
{
    emit(0, "function %s:_encode_one()", ls->structname->shortname);

    // check for no members
    if(!g_ptr_array_size(ls->members)){
    	emit(0, "");
    	emit(0, "  -- nothing to do");
    	emit(0, "end");
    	return;
    }

    emit(0, "");
    emit(0, "  local buf_table = {}");
    emit(0, "");

    GQueue *struct_fmt = g_queue_new ();
    GQueue *struct_members = g_queue_new ();

    for (unsigned int m = 0; m < g_ptr_array_size(ls->members); m++) {
        lcm_member_t *lm = (lcm_member_t *) g_ptr_array_index(ls->members, m);
        char fmt = _struct_format (lm);

        if (! lm->dimensions->len) {
        	// XXX not an array
            if (fmt) {
            	// XXX is a primitive
                g_queue_push_tail (struct_fmt, GINT_TO_POINTER ((int)fmt));
                g_queue_push_tail (struct_members, lm);
            } else {
            	// XXX not a primitive
                _flush_write_struct_fmt (f, struct_fmt, struct_members);
                char *accessor = g_strdup_printf("self.%s", lm->membername);
                _emit_encode_one (lcm, f, ls, lm, accessor, 1);
                g_free(accessor);
            }
        } else {
        	// XXX this is an array
            _flush_write_struct_fmt (f, struct_fmt, struct_members);
            GString *accessor = g_string_new ("");
            g_string_append_printf (accessor, "self.%s", lm->membername);

            int n;
            for (n=0; n<lm->dimensions->len - 1; n++) {
                lcm_dimension_t *dim = 
                    (lcm_dimension_t*) g_ptr_array_index (lm->dimensions, n);

                g_string_append_printf (accessor, "[i%d]", n);
                if (dim->mode == LCM_CONST) {
                    emit (1+n, "for i%d = 1, %s do", n, dim->size);
                } else {
                    emit (1+n, "for i%d = 1, self.%s do", n, dim->size);
                }
            }

            // last dimension.
            lcm_dimension_t *last_dim = (lcm_dimension_t *) g_ptr_array_index(lm->dimensions,
                    lm->dimensions->len - 1);
            int last_dim_fixed_len = last_dim->mode == LCM_CONST;

            if(lcm_is_primitive_type(lm->type->lctypename) && 
               0 != strcmp(lm->type->lctypename, "string")) {

                _emit_encode_list(lcm, f, ls, lm,
                        accessor->str, 1+n, last_dim->size, last_dim_fixed_len);
            } else {
                if (last_dim_fixed_len) {
                    emit (1+n, "for i%d = 1, %s do", n, last_dim->size);
                } else {
                    emit (1+n, "for i%d = 1, self.%s do", n, last_dim->size);
                }
                g_string_append_printf (accessor, "[i%d]", n);
                _emit_encode_one (lcm, f, ls, lm, accessor->str, n+2);
                emit (1+n, "end");
            }

            g_string_free (accessor, TRUE);

            while ( --n >= 0 ) {
            	emit (1+n, "end");
            }
        }
    }
    _flush_write_struct_fmt (f, struct_fmt, struct_members);

    g_queue_free (struct_fmt);
    g_queue_free (struct_members);

    emit(0, "");
    emit(0, "  return table.concat(buf_table)");
    emit(0, "end");
    emit(0, "");
}

static void
emit_lua_encode (const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls)
{
	const char *sn = ls->structname->shortname;

	emit(0, "function %s:encode()", sn);
    emit(0, "");
    emit(0, "  return %s._packed_fingerprint .. self:_encode_one()", sn);
    emit(0, "end");
    emit(0, "");
}

static void
emit_member_initializer(const lcmgen_t* lcm, FILE *f, lcm_member_t* lm,
        int dim_num)
{
	if(dim_num == lm->dimensions->len) {
		emit_end("%s", nil_initializer_string(lm->type));
		return;
	}

	lcm_dimension_t *dim =
		(lcm_dimension_t *) g_ptr_array_index (lm->dimensions, dim_num);

	if(dim->mode == LCM_VAR) {
		emit_end("{}");
	} else {
		emit_end("{}");
		emit(dim_num + 1, "for d%d = 1, %s do", dim_num, dim->size);
		emit_start(dim_num + 2, "obj.%s", lm->membername);
		for(int i = 0; i < dim_num + 1; i++){
			emit_continue("[d%d]", i);
		}
		emit_continue(" = ");
		emit_member_initializer(lcm, f, lm, dim_num + 1);
		emit(dim_num + 1, "end");
	}
}

static void
emit_lua_new (const lcmgen_t *lcm, FILE *f, lcm_struct_t *lr)
{
    emit(0, "function %s:new()", lr->structname->shortname);
    emit(0, "");
    emit(0, "  local obj = {}");
    emit(0, "");

    unsigned int member;
    for (member = 0; member < lr->members->len; member++) {
        lcm_member_t *lm = (lcm_member_t *) g_ptr_array_index(lr->members, member);
        fprintf(f, "  obj.%s = ", lm->membername);
        // XXX this might need alot of work because lua doesn't have list comprehension
        emit_member_initializer(lcm, f, lm, 0);
    }

    if (0 != member) emit(0, "");

    emit(0, "  setmetatable(obj, self)");
    emit(0, "");
    emit(0, "  return obj");
    emit(0, "end");
    emit(0, "");
}

static void
emit_lua_fingerprint (const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls)
{
    const char *sn = ls->structname->shortname;

    emit(0, "function %s._get_hash_recursive(parents)", sn);
    emit(0, "");
    emit(0, "  local newparents = {}");
    emit(0, "");
    emit(0, "  for _, v in ipairs(parents) do");
    emit(0, "    if v == %s then return lcm._hash.new('0x0') end", sn);
    emit(0, "    table.insert(newparents, v)");
    emit(0, "  end");
    emit(0, "");
    emit(0, "  table.insert(newparents, %s)", sn);
    emit(0, "");
    emit(0, "  local hash = lcm._hash.new('0x%"PRIx64"')", ls->hash);

    // add all substruct hashes
    for (unsigned int m = 0; m < ls->members->len; m++) {
    	lcm_member_t *lm = (lcm_member_t *) g_ptr_array_index(ls->members, m);
    	const char *msn = lm->type->shortname;
    	if (! lcm_is_primitive_type(lm->type->lctypename)) {
    		const char *ghr = "_get_hash_recursive(newparents)";
    		// XXX this might need a touch up, not sure about intra-module names
    		if (is_same_type(lm->type, ls->structname)) {
    			emit(0, "    + %s.%s", msn, ghr);
    		} else {
    			char *variablename = escape_typename_to_variablename(lm->type->lctypename);
    			emit(0, "    + %s.%s", variablename, ghr);
    			g_free(variablename);
    		}
    	}
    }

    emit(0, "  hash:rotate(1)");
    emit(0, "");
    emit(0, "  return hash");
    emit(0, "end");
    emit(0, "");
    emit(0, "%s._packed_fingerprint = lcm._pack.pack('>X', %s._get_hash_recursive({}))", sn, sn);
    emit(0, "");
}

static void
emit_lua_dependencies (const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls)
{
    GHashTable *dependencies = g_hash_table_new (g_str_hash, g_str_equal);
    for (unsigned int m=0; m<ls->members->len; m++) {
        lcm_member_t *lm = (lcm_member_t *) g_ptr_array_index (ls->members, m);
        if (! lcm_is_primitive_type (lm->type->lctypename)) {
        	if (!g_hash_table_lookup (dependencies, lm->type->lctypename)
        			&& strcmp(lm->type->lctypename, ls->structname->lctypename)) {
        		g_hash_table_insert (dependencies, lm->type->lctypename,
        				lm->type->lctypename);
        	}
        }
    }
    GPtrArray *deps = _hash_table_get_vals (dependencies);
    for (int i=0; i<deps->len; i++) {
        const char *package = (char *) g_ptr_array_index (deps, i);
        char *variablename = escape_typename_to_variablename(package);
        emit (0, "local %s = require('%s')", variablename, package);
        g_free(variablename);
    }

    if(deps->len) emit (0, "");

    g_ptr_array_free (deps, TRUE);
    g_hash_table_destroy (dependencies);
}

static void
emit_lua_locals (const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls)
{
	emit(0, "local setmetatable = setmetatable");
	emit(0, "local ipairs = ipairs");
	emit(0, "local table = table");
	emit(0, "local string = string");
	emit(0, "local unpack = unpack");
	emit(0, "");
}

static void
emit_lua_buffer_helper (const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls)
{
	emit(0, "-- buffer helper for decoding");
	emit(0, "local _buffer_helper = {}");
	emit(0, "_buffer_helper.__index = _buffer_helper");
	emit(0, "");
	emit(0, "function _buffer_helper:new(data_str)");
	emit(0, "");
	emit(0, "  local obj = {buffer = data_str, index = 1}");
	emit(0, "  setmetatable(obj, self)");
	emit(0, "");
	emit(0, "  return obj");
	emit(0, "end");
	emit(0, "");
	emit(0, "function _buffer_helper:read(n_bytes)");
	emit(0, "");
	emit(0, "  local partial = self.buffer:sub(self.index, self.index + n_bytes - 1)");
	emit(0, "  self.index = self.index + n_bytes");
	emit(0, "");
	emit(0, "  if self.index > #self.buffer + 1 then");
	emit(0, "    error('buffer ran out of bytes')");
	emit(0, "  end");
	emit(0, "");
	emit(0, "  return partial");
	emit(0, "end");
	emit(0, "");
}

typedef struct {
    char *name;
    GPtrArray *enums;
    GPtrArray *structs;
} _package_contents_t;

static _package_contents_t * _package_contents_new (const char *name)
{
    _package_contents_t *pc = (_package_contents_t *) malloc (sizeof(_package_contents_t));
    pc->enums = g_ptr_array_new ();
    pc->structs = g_ptr_array_new ();
    pc->name = strdup (name);
    return pc;
}

static void _package_contents_free (_package_contents_t *pc)
{
    g_ptr_array_free (pc->enums, TRUE);
    g_ptr_array_free (pc->structs, TRUE);
    free (pc->name);
    free (pc);
}

// XXX step 2, basically the main function
static int
emit_package (lcmgen_t *lcm, _package_contents_t *pc)
{
    // create the package directory, if necessary
    char **dirs = g_strsplit (pc->name, ".", 0);
    char *pdname = build_filenamev (dirs);
    char package_dir[PATH_MAX];
    char package_dir_prefix[PATH_MAX];
    int have_package = dirs[0] != NULL;

    sprintf (package_dir_prefix, "%s%s", getopt_get_string(lcm->gopt, "lpath"),
            strlen(getopt_get_string(lcm->gopt, "lpath")) > 0 ?
            G_DIR_SEPARATOR_S : "");
    sprintf(package_dir, "%s%s%s", package_dir_prefix, pdname,
            have_package ? G_DIR_SEPARATOR_S : "");
    free (pdname);
    if (strlen (package_dir)) {
        if (! g_file_test (package_dir, G_FILE_TEST_EXISTS)) {
//            g_mkdir_with_parents (package_dir, 0755);
            mkdir_with_parents (package_dir, 0755);
        }
        if (!g_file_test (package_dir, G_FILE_TEST_IS_DIR)) {
            err ("Could not create directory %s\n", package_dir);
            return -1;
        }
    }

    // write the package init.lua files, if necessary
    FILE *init_lua_fp = NULL;
    GHashTable * initlua_requires = NULL;
    GHashTable * initlua_requires_subpack = NULL;

    if (have_package) {
        int ndirs = 0;
        for (ndirs=0; dirs[ndirs]; ndirs++);

        for (int i=0 ; i<ndirs; i++) {

        	// make filename
        	char *initlua_fname;

        	{
        		char *initlua_fname_parts[1024];
        		assert(ndirs + 4 < 1024);

        		initlua_fname_parts[0] = package_dir_prefix;
        		for (int j=0; j<=i; j++) {
        			initlua_fname_parts[j+1] = dirs[j];
        		}
        		initlua_fname_parts[i+2] = "init.lua";
        		initlua_fname_parts[i+3] = NULL;

        		initlua_fname = build_filenamev (initlua_fname_parts);
        	}

            // make current package name
        	char * package_name;

        	{
        		char * name_parts[1024];
        		assert(i < 1024);

        		for (int j = 0; j <= i; j++) {
        			name_parts[j] = dirs[j];
        		}
        		name_parts[i + 1] = NULL;

        		package_name = g_strjoinv(".", name_parts);
        	}

            if (initlua_requires) {
            	g_hash_table_destroy(initlua_requires);
            	initlua_requires = NULL;
            }

            if (initlua_requires_subpack) {
            	g_hash_table_destroy(initlua_requires_subpack);
            	initlua_requires_subpack = NULL;
            }

            initlua_requires = g_hash_table_new_full(g_str_hash, g_str_equal, g_free, g_free);
            initlua_requires_subpack = g_hash_table_new_full(g_str_hash, g_str_equal, g_free, g_free);

            // if the file already exists, read the contents
            if (g_file_test (initlua_fname, G_FILE_TEST_EXISTS)) {

            	init_lua_fp = fopen(initlua_fname, "r");

            	if (!init_lua_fp) {
            		perror ("fopen");
            		free (initlua_fname);
            		g_free(package_name);
            		return -1;
            	}

            	while(!feof(init_lua_fp)) {
            		char buf[4096];
            		memset(buf, 0, sizeof(buf));
            		char *result = fgets(buf, sizeof(buf)-1, init_lua_fp);
            		if(!result)
            			break;

            		// XXX get all of the previous types and packages

            		// this regex works because the first part is greedy
            		GRegex * regex = g_regex_new("require\\('([\\w+\\.]*\\.)(\\w+)'\\)( -- subpackage)?",
						(GRegexCompileFlags) 0, (GRegexMatchFlags) 0, NULL);
            		GMatchInfo * matchinfo;

            		if(g_regex_match(regex, buf, (GRegexMatchFlags) 0, &matchinfo)){
            			if(g_match_info_get_match_count(matchinfo) == 3){
            				// not a subpackage
            				gchar * classname = g_match_info_fetch(matchinfo, 2);
            				g_hash_table_insert(initlua_requires, g_strdup(classname), g_strdup(classname));
            			}else if(g_match_info_get_match_count(matchinfo) == 4){
            				// this is a subpackage
            				// XXX fprintf(stderr, "> buff: %s\n", buf);
            				gchar * superpackage = g_match_info_fetch(matchinfo, 1);
            				gchar * subpackage = g_match_info_fetch(matchinfo, 2);
            				// XXX fprintf(stderr, "> super: %s, sub: %s\n", superpackage, subpackage);
            				gchar * fullsubpackage = g_strjoin("", superpackage, subpackage, NULL);
            				// XXX fprintf(stderr, "> [2] inserting: %s\n", fullsubpackage);
            				g_hash_table_insert(initlua_requires_subpack, g_strdup(fullsubpackage), g_strdup(fullsubpackage));
            				g_free(fullsubpackage);
            			}
            		}

            		g_match_info_free(matchinfo);
            		g_regex_unref(regex);
            	}

            	fclose(init_lua_fp);
            	init_lua_fp = NULL;
            }

            init_lua_fp = fopen(initlua_fname, "w");
            // XXX fprintf(stderr, "> opened: %s\n", initlua_fname);

            if (!init_lua_fp) {
            	perror ("fopen");
            	free (initlua_fname);
            	g_free(package_name);
            	return -1;
            }

#ifndef WIN32
            // lock init.lua for exclusive write access
            // TODO do the equivalent in windows
            struct flock lockinfo;
            lockinfo.l_type = F_WRLCK;
            lockinfo.l_start = 0;
            lockinfo.l_whence = SEEK_SET;
            lockinfo.l_len = 0 ;
            lockinfo.l_pid = getpid();
            if(0 != fcntl(fileno(init_lua_fp), F_SETLKW, &lockinfo)) {
                perror("locking init.lua");
                free(initlua_fname);
                g_free(package_name);
                fclose(init_lua_fp);
                return -1;
            }
#endif

            fprintf (init_lua_fp, "--[[\n"
            		"LCM package init.lua file\n"
            		"This file automatically generated by lcm-gen.\n"
            		"DO NOT MODIFY BY HAND!!!!\n"
            		"--]]\n"
            		"\n"
            		"local M = {}\n"
            		"\n");

            // add in all previous types
            GList * package_types = g_hash_table_get_values(initlua_requires);

            for (int j = 0; j < g_list_length(package_types); j++) {
            	char * tn = (char *) g_list_nth_data(package_types, j);
            	char * fn = g_strjoin(".", package_name, tn, NULL);
            	fprintf(init_lua_fp, "M.%s = require('%s')\n", tn, fn);
            	g_free(fn);
            }

            g_list_free(package_types);

            // add in all previous packages
            GList * subpacks = g_hash_table_get_values(initlua_requires_subpack);

            for (int j = 0; j < g_list_length(subpacks); j++) {
            	char * spn = (char *) g_list_nth_data(subpacks, j);
            	// get the base of the package name
            	char ** tmpsplit = g_strsplit(spn, ".", -1);
            	char * sn = tmpsplit[g_strv_length(tmpsplit) - 1];
            	// XXX fprintf(stderr, "[1] sn: %s, spn: %s\n", sn, spn);
            	fprintf(init_lua_fp, "M.%s = require('%s') -- subpackage\n", sn, spn);
            	g_strfreev(tmpsplit);
            }

            g_list_free(subpacks);

            // if the current package has a subpackage (which eventually contains the target package)
            // add a `require` for that subpackage to the current (if it hasn't already)
            if (i + 1 < ndirs) {
            	char *subpack_name = g_strjoin(".", package_name, dirs[i + 1], NULL);

            	// check for the subpackage name
            	if (!g_hash_table_lookup(initlua_requires_subpack, subpack_name)) {

            		// add it if it didn't exist
            		g_hash_table_insert(initlua_requires_subpack, g_strdup(subpack_name), g_strdup(subpack_name));
            		// XXX fprintf(stderr, "[2] sn: %s, spn: %s\n", dirs[i + 1], subpack_name);
            		fprintf(init_lua_fp, "M.%s = require('%s') -- subpackage\n", dirs[i + 1], subpack_name);
            	}

            	g_free(subpack_name);
            }

            // not yet the target?
            if (i + 1 < ndirs) {

            	// close it out
            	fprintf(init_lua_fp, "\nreturn M\n\n");
            	fclose(init_lua_fp);
            	init_lua_fp = NULL;
            }

            free (initlua_fname);
            g_free(package_name);
        }
    }
    g_strfreev (dirs);

    ////////////////////////////////////////////////////////////
    // STRUCTS
    for (int i = 0; i<pc->structs->len; i++) {
        lcm_struct_t *ls = (lcm_struct_t *) g_ptr_array_index(pc->structs, i);

        char path[PATH_MAX];
        sprintf (path, "%s%s.lua", package_dir, ls->structname->shortname);

        if(init_lua_fp){

        	// XXX add the 'require' to the appropriate init.lua
        	if (!g_hash_table_lookup(initlua_requires, ls->structname->shortname)) {
        		fprintf(init_lua_fp, "M.%s = require('%s')\n", ls->structname->shortname, ls->structname->lctypename);
        	}

        	// XXX look for subpackages
        	for (unsigned int m = 0; m < g_ptr_array_size(ls->members); m++) {
        		lcm_member_t *lm = (lcm_member_t *) g_ptr_array_index(ls->members, m);

        		if(g_str_has_prefix(lm->type->package, pc->name)){

        			// make a regex starting with the current package...
        			gchar ** tmpsplit = g_strsplit(pc->name, ".", 0);
        			gchar * regexpackage = g_strjoinv("\\.", tmpsplit);

        			// only look for immediate submodules, not submodules of the submodules
        			gchar * regexstr = g_strjoin("", "^", regexpackage, "\\.(\\w+)", NULL);

        			GRegex * regex = g_regex_new(regexstr, (GRegexCompileFlags) 0, (GRegexMatchFlags) 0, NULL);
        			GMatchInfo * matchinfo;

        			g_strfreev(tmpsplit);
        			g_free(regexpackage);
        			g_free(regexstr);

        			if (g_regex_match(regex, lm->type->package, (GRegexMatchFlags) 0, &matchinfo)) {
        				if (g_match_info_get_match_count(matchinfo) == 2) {
        					gchar * fullsubpackage = g_match_info_fetch(matchinfo, 0);
        					gchar * subpackage = g_match_info_fetch(matchinfo, 1);

        					// was it already in the file?
        					if (!g_hash_table_lookup(initlua_requires_subpack, fullsubpackage)) {
        						// XXX fprintf(stderr, "> [1] inserting: %s\n", fullsubpackage);
        						g_hash_table_insert(initlua_requires_subpack, g_strdup(fullsubpackage), g_strdup(fullsubpackage));
        						fprintf(init_lua_fp, "M.%s = require('%s') -- subpackage\n", subpackage, fullsubpackage);
        					}
        				}
        			}

        			g_match_info_free(matchinfo);
        			g_regex_unref(regex);
        		}
        	}
        }

        if (!lcm_needs_generation(lcm, ls->lcmfile, path))
            continue;

        FILE *f = fopen(path, "w");
        if (f==NULL) return -1;

        fprintf(f, "--[[\n"
        		"LCM type definitions\n"
        		"This file automatically generated by lcm.\n"
        		"DO NOT MODIFY BY HAND!!!!\n"
        		"--]]\n"
        		"\n"
        		"local lcm = require('lcm')\n\n");

        emit_lua_dependencies (lcm, f, ls);

        // XXX added this...
        emit_lua_locals(lcm, f, ls);
        emit_lua_buffer_helper(lcm, f, ls);

        // XXX step 3, start making the object
        emit(0, "local %s = {}", ls->structname->shortname);
        emit(0, "%s.__index = %s", ls->structname->shortname, ls->structname->shortname);
        emit(0, "");

        // CONSTANTS
        for (unsigned int cn = 0; cn < g_ptr_array_size(ls->constants); cn++) {
            lcm_constant_t *lc = (lcm_constant_t *) g_ptr_array_index(ls->constants, cn);
            assert(lcm_is_legal_const_type(lc->lctypename));
            emit(1, "%s.%s = %s", ls->structname->shortname,
            		lc->membername, lc->val_str);
        }
        if (g_ptr_array_size(ls->constants) > 0)
            emit(0, "");

        // NAMES
        emit(0, "%s.name = '%s'", ls->structname->shortname, ls->structname->lctypename);
        emit(0, "%s.packagename = '%s'", ls->structname->shortname, ls->structname->package);
        emit(0, "%s.shortname = '%s'", ls->structname->shortname, ls->structname->shortname);
        emit(0, "");

        emit_lua_new (lcm, f, ls);
        emit_lua_fingerprint (lcm, f, ls);
        emit_lua_encode (lcm, f, ls);
        emit_lua_encode_one (lcm, f, ls);
        emit_lua_decode (lcm, f, ls);
        emit_lua_decode_one (lcm, f, ls);

        emit(0, "return %s", ls->structname->shortname);
        emit(0, "");

        fclose (f);
    }

    if(init_lua_fp){
    	fprintf(init_lua_fp, "\nreturn M\n\n");
        fclose(init_lua_fp);
    }

    g_hash_table_destroy(initlua_requires);
    return 0;
}

// XXX step 1, but there's not much to see, then go to emit package
int emit_lua(lcmgen_t *lcm)
{
    GHashTable *packages = g_hash_table_new_full (g_str_hash, 
            g_str_equal, NULL, (GDestroyNotify)_package_contents_free);

    // group the enums and structs by package
    for (unsigned int i = 0; i < lcm->enums->len; i++) {
        lcm_enum_t *le = (lcm_enum_t *) g_ptr_array_index(lcm->enums, i);
        _package_contents_t *pc = (_package_contents_t *) g_hash_table_lookup (packages, 
                le->enumname->package);
        if (!pc) {
            pc = _package_contents_new (le->enumname->package);
            g_hash_table_insert (packages, pc->name, pc);
        }
        g_ptr_array_add (pc->enums, le);
    }

    for (unsigned int i = 0; i < lcm->structs->len; i++) {
        lcm_struct_t *ls = (lcm_struct_t *) g_ptr_array_index(lcm->structs, i);
        _package_contents_t *pc = (_package_contents_t *) g_hash_table_lookup (packages, 
                ls->structname->package);
        if (!pc) {
            pc = _package_contents_new (ls->structname->package);
            g_hash_table_insert (packages, pc->name, pc);
        }
        g_ptr_array_add (pc->structs, ls);
    }

    GPtrArray *vals = _hash_table_get_vals (packages);

    for (int i=0; i<vals->len; i++) {
        _package_contents_t *pc = (_package_contents_t *) g_ptr_array_index (vals, i);
        int status = emit_package (lcm, pc); 
        if (0 != status) return status;
    }

    g_ptr_array_free (vals, TRUE);

    g_hash_table_destroy (packages);
    return 0;

}
