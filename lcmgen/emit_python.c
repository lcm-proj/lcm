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

#define INDENT(n) (4*(n))

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

void setup_python_options(getopt_t *gopt)
{
    getopt_add_string(gopt, 0,   "ppath",     "",         
            "Python destination directory");
}

static int
is_same_type (const lcm_typename_t *tn1, const lcm_typename_t *tn2) {
    return ! strcmp (tn1->lctypename, tn2->lctypename);
}

static int
is_same_package (const lcm_typename_t *tn1, const lcm_typename_t *tn2) {
    return ! strcmp (tn1->package, tn2->package);
}

static const char *
nil_initializer_string(const lcm_typename_t *type)
{
    if (!strcmp(type->lctypename, "byte")) return "0";
    if (!strcmp(type->lctypename, "boolean")) return "False";
    if (!strcmp(type->lctypename, "int8_t")) return "0";
    if (!strcmp(type->lctypename, "int16_t")) return "0";
    if (!strcmp(type->lctypename, "int32_t")) return "0";
    if (!strcmp(type->lctypename, "int64_t")) return "0";
    if (!strcmp(type->lctypename, "float")) return "0.0";
    if (!strcmp(type->lctypename, "double")) return "0.0";
    if (!strcmp(type->lctypename, "string")) return "\"\"";
    else return "None";
}

static char
_struct_format (lcm_member_t *lm) 
{
    const char *tn = lm->type->lctypename;
    if (!strcmp ("byte", tn)) return 'B';
    if (!strcmp ("boolean", tn)) return 'b';
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

static void
_emit_decode_one (const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls, 
        lcm_member_t *lm, const char *accessor, int indent, const char *sfx)
{
    const char *tn = lm->type->lctypename;
    const char *mn = lm->membername;
    const char *sn = lm->type->shortname;
    if (!strcmp ("string", tn)) {
        emit (indent, "__%s_len = struct.unpack('>I', buf.read(4))[0]", mn);
        emit (indent, "%sbuf.read(__%s_len)[:-1].decode('utf-8', 'replace')%s",
                accessor, mn, sfx);
    } else if (!strcmp ("byte", tn)) {
        emit (indent, "%sstruct.unpack('B', buf.read(1))[0]%s", accessor, sfx);
    } else if (!strcmp ("int8_t", tn) || !(strcmp ("boolean", tn))) {
        emit (indent, "%sstruct.unpack('b', buf.read(1))[0]%s", accessor, sfx);
    } else if (!strcmp ("int16_t", tn)) {
        emit (indent, "%sstruct.unpack('>h', buf.read(2))[0]%s", accessor, sfx);
    } else if (!strcmp ("int32_t", tn)) {
        emit (indent, "%sstruct.unpack('>i', buf.read(4))[0]%s", accessor, sfx);
    } else if (!strcmp ("int64_t", tn)) {
        emit (indent, "%sstruct.unpack('>q', buf.read(8))[0]%s", accessor, sfx);
    } else if (!strcmp ("float", tn)) {
        emit (indent, "%sstruct.unpack('>f', buf.read(4))[0]%s", accessor, sfx);
    } else if (!strcmp ("double", tn)) {
        emit (indent, "%sstruct.unpack('>d', buf.read(8))[0]%s", accessor, sfx);
    } else {
        if (is_same_type (lm->type, ls->structname)) {
            emit (indent, "%s%s._decode_one(buf)%s", accessor, sn, sfx);
        } else if (is_same_package (lm->type, ls->structname)) {
            emit (indent, "%s%s.%s._decode_one(buf)%s", accessor, sn, sn, sfx);
        } else {
            emit (indent, "%s%s._decode_one(buf)%s", accessor, tn, sfx);
        }
    }
}

static void
_emit_decode_list(const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls,
        lcm_member_t *lm, const char *accessor, int indent, int is_first,
        const char *len, int fixed_len)
{
    const char *tn = lm->type->lctypename;
    const char *suffix = "";
    if(!is_first) {
        suffix = ")";
    }
    if (!strcmp ("byte", tn)) {
        emit (indent, "%sbuf.read(%s%s)%s", 
                accessor, fixed_len ? "":"self.", len, suffix);
    } else if (!strcmp ("int8_t", tn) || 
               !strcmp ("boolean", tn) ||
               !strcmp ("int16_t", tn) ||
               !strcmp ("int32_t", tn) ||
               !strcmp ("int64_t", tn) ||
               !strcmp ("float", tn) ||
               !strcmp ("double", tn)) {
        if(fixed_len) {
            emit (indent, "%sstruct.unpack('>%s%c', buf.read(%d))%s", 
                    accessor, len, _struct_format(lm), 
                    atoi(len) * _primitive_type_size(tn), 
                    suffix);
        } else {
            if(_primitive_type_size(tn) > 1) {
                emit (indent, 
                        "%sstruct.unpack('>%%d%c' %% self.%s, buf.read(self.%s * %d))%s", 
                        accessor, _struct_format(lm), len, len, 
                        _primitive_type_size(tn), suffix);
            } else {
            emit (indent, 
                    "%sstruct.unpack('>%%d%c' %% self.%s, buf.read(self.%s))%s", 
                    accessor, _struct_format(lm), len, len, suffix);
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

    fprintf (f, "        ");
    int fmtsize = 0;
    while (! g_queue_is_empty (members)) {
        lcm_member_t *lm = (lcm_member_t*) g_queue_pop_head (members);
        emit_continue ("self.%s", lm->membername);
        if (! g_queue_is_empty (members)) {
            emit_continue (", ");
        }
        fmtsize += _primitive_type_size (lm->type->lctypename);
    }
    emit_continue (" = struct.unpack(\">");
    while (! g_queue_is_empty (formats)) {
        emit_continue ("%c", GPOINTER_TO_INT (g_queue_pop_head (formats)));
    }
    emit_end ("\", buf.read(%d))%s", fmtsize, nfmts == 1 ? "[0]" : "");
}

static void
emit_python_decode_one (const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls)
{
    emit(1, "def _decode_one(buf):");
    emit (2, "self = %s()", ls->structname->shortname);

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
                char *accessor = g_strdup_printf("self.%s = ", lm->membername);
                _emit_decode_one (lcm, f, ls, lm, accessor, 2, "");
                g_free(accessor);
            }
        } else {
            _flush_read_struct_fmt (lcm, f, struct_fmt, struct_members);

            GString *accessor = g_string_new ("");
            g_string_append_printf (accessor, "self.%s", lm->membername);

            // iterate through the dimensions of the member, building up
            // an accessor string, and emitting for loops
            unsigned int n;
            for (n=0; n<lm->dimensions->len-1; n++) {
                lcm_dimension_t *dim = (lcm_dimension_t *) g_ptr_array_index (lm->dimensions, n);

                if(n == 0) {
                    emit (2, "%s = []", accessor->str);
                } else {
                    emit (2+n, "%s.append([])", accessor->str);
                }

                if (dim->mode == LCM_CONST) {
                    emit (2+n, "for i%d in range(%s):", n, dim->size);
                } else {
                    emit (2+n, "for i%d in range(self.%s):", n, dim->size);
                }

                if(n > 0 && n < lm->dimensions->len-1) {
                    g_string_append_printf(accessor, "[i%d]", n-1);
                }
            }

            // last dimension.
            lcm_dimension_t *last_dim = (lcm_dimension_t *) g_ptr_array_index(lm->dimensions,
                    lm->dimensions->len - 1);
            int last_dim_fixed_len = last_dim->mode == LCM_CONST;

            if(lcm_is_primitive_type(lm->type->lctypename) && 
               0 != strcmp(lm->type->lctypename, "string")) {
                // member is a primitive non-string type.  Emit code to 
                // decode a full array in one call to struct.unpack
                if(n == 0) {
                    g_string_append_printf (accessor, " = ");
                } else {
                    g_string_append_printf (accessor, ".append(");
                }

                _emit_decode_list(lcm, f, ls, lm,
                        accessor->str, 2+n, n==0, 
                        last_dim->size, last_dim_fixed_len);
            } else {
                // member is either a string type or an inner LCM type.  Each
                // array element must be decoded individually
                if(n == 0) {
                    emit (2, "%s = []", accessor->str);
                } else {
                    emit (2+n, "%s.append ([])", accessor->str);
                    g_string_append_printf (accessor, "[i%d]", n-1);
                }
                if (last_dim_fixed_len) {
                    emit (2+n, "for i%d in range(%s):", n, last_dim->size);
                } else {
                    emit (2+n, "for i%d in range(self.%s):", n, last_dim->size);
                }
                g_string_append_printf (accessor, ".append(");
                _emit_decode_one (lcm, f, ls, lm, accessor->str, n+3, ")");
            }
            g_string_free (accessor, TRUE);
        }
    }
    _flush_read_struct_fmt (lcm, f, struct_fmt, struct_members);
    emit (2, "return self");

    g_queue_free (struct_fmt);
    g_queue_free (struct_members);
    emit (1, "_decode_one = staticmethod(_decode_one)");
    fprintf (f, "\n");
}

static void
emit_python_decode (const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls)
{
    emit (1, "def decode(data):");
    emit (2, "if hasattr(data, 'read'):");
    emit (3,     "buf = data");
    emit (2, "else:");
    emit (3,     "buf = StringIO.StringIO(data)");
    emit (2, "if buf.read(8) != %s._get_packed_fingerprint():", 
            ls->structname->shortname);
    emit (3,     "raise ValueError(\"Decode error\")");
    emit (2, "return %s._decode_one(buf)", ls->structname->shortname);
    emit (1, "decode = staticmethod(decode)");
    fprintf (f, "\n");
}

static void
_emit_encode_one (const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls, 
        lcm_member_t *lm, const char *accessor, int indent)
{
    const char *tn = lm->type->lctypename;
    const char *mn = lm->membername;
    if (!strcmp ("string", tn)) {
        emit (indent, "__%s_encoded = %s.encode('utf-8')", mn, accessor);
        emit (indent, "buf.write(struct.pack('>I', len(__%s_encoded)+1))", mn);
        emit (indent, "buf.write(__%s_encoded)", mn);
        emit (indent, "buf.write(\"\\0\")");
    } else if (!strcmp ("byte", tn)) {
        emit (indent, "buf.write(struct.pack('B', %s))", accessor);
    } else if (!strcmp ("int8_t", tn) || !strcmp ("boolean", tn)) {
        emit (indent, "buf.write(struct.pack('b', %s))", accessor);
    } else if (!strcmp ("int16_t", tn)) {
        emit (indent, "buf.write(struct.pack('>h', %s))", accessor);
    } else if (!strcmp ("int32_t", tn)) {
        emit (indent, "buf.write(struct.pack('>i', %s))", accessor);
    } else if (!strcmp ("int64_t", tn)) {
        emit (indent, "buf.write(struct.pack('>q', %s))", accessor);
    } else if (!strcmp ("float", tn)) {
        emit (indent, "buf.write(struct.pack('>f', %s))", accessor);
    } else if (!strcmp ("double", tn)) {
        emit (indent, "buf.write(struct.pack('>d', %s))", accessor);
    } else {
        if(is_same_type(lm->type, ls->structname)) {
            emit(indent, "assert %s._get_packed_fingerprint() == %s._get_packed_fingerprint()", accessor,
                    lm->type->shortname);
        } else if(is_same_package(ls->structname, lm->type)) {
            emit(indent, "assert %s._get_packed_fingerprint() == %s.%s._get_packed_fingerprint()",
                    accessor, lm->type->shortname, lm->type->shortname);
        } else {
            emit(indent, "assert %s._get_packed_fingerprint() == %s.%s._get_packed_fingerprint()",
                    accessor, lm->type->lctypename, lm->type->shortname);
        }
        emit (indent, "%s._encode_one(buf)", accessor);
    }
}

static void
_emit_encode_list(const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls,
        lcm_member_t *lm, const char *accessor, int indent, 
        const char *len, int fixed_len)
{
    const char *tn = lm->type->lctypename;
    if (!strcmp ("byte", tn)) {
        emit (indent, "buf.write(%s[:%s%s])", 
                accessor, (fixed_len?"":"self."), len);
        return;
    } else if (!strcmp ("boolean", tn) ||
               !strcmp ("int8_t", tn) ||
               !strcmp ("int16_t", tn) ||
               !strcmp ("int32_t", tn) ||
               !strcmp ("int64_t", tn) ||
               !strcmp ("float", tn) ||
               !strcmp ("double", tn)) {
        if(fixed_len) {
            emit(indent, "buf.write(struct.pack('>%s%c', *%s[:%s]))", 
                    len, _struct_format(lm), accessor, len);
        } else {
            emit(indent, 
                    "buf.write(struct.pack('>%%d%c' %% self.%s, *%s[:self.%s]))", 
                    _struct_format(lm), len, accessor, len);
        }
    } else {
        assert(0);
    }

}

static void
_flush_write_struct_fmt (FILE *f, GQueue *formats, GQueue *members)
{
    assert (g_queue_get_length (formats) == g_queue_get_length (members));
    if (g_queue_is_empty (formats)) return;
    emit_start (2, "buf.write(struct.pack(\">");
    while (! g_queue_is_empty (formats)) {
        emit_continue ("%c", GPOINTER_TO_INT (g_queue_pop_head (formats)));
    }
    emit_continue ("\", ");
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
emit_python_encode_one (const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls)
{
    emit(1, "def _encode_one(self, buf):");
    if(!g_ptr_array_size(ls->members))
        emit(2, "pass");

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
                _flush_write_struct_fmt (f, struct_fmt, struct_members);
                char *accessor = g_strdup_printf("self.%s", lm->membername);
                _emit_encode_one (lcm, f, ls, lm, accessor, 2);
                g_free(accessor);
            }
        } else {
            _flush_write_struct_fmt (f, struct_fmt, struct_members);
            GString *accessor = g_string_new ("");
            g_string_append_printf (accessor, "self.%s", lm->membername);

            unsigned int n;
            for (n=0; n<lm->dimensions->len - 1; n++) {
                lcm_dimension_t *dim = 
                    (lcm_dimension_t*) g_ptr_array_index (lm->dimensions, n);

                g_string_append_printf (accessor, "[i%d]", n);
                if (dim->mode == LCM_CONST) {
                    emit (2+n, "for i%d in range(%s):", n, dim->size);
                } else {
                    emit (2+n, "for i%d in range(self.%s):", n, dim->size);
                }
            }

            // last dimension.
            lcm_dimension_t *last_dim = (lcm_dimension_t *) g_ptr_array_index(lm->dimensions,
                    lm->dimensions->len - 1);
            int last_dim_fixed_len = last_dim->mode == LCM_CONST;

            if(lcm_is_primitive_type(lm->type->lctypename) && 
               0 != strcmp(lm->type->lctypename, "string")) {

                _emit_encode_list(lcm, f, ls, lm,
                        accessor->str, 2+n, last_dim->size, last_dim_fixed_len);
            } else {
                if (last_dim_fixed_len) {
                    emit (2+n, "for i%d in range(%s):", n, last_dim->size);
                } else {
                    emit (2+n, "for i%d in range(self.%s):", n, last_dim->size);
                }
                g_string_append_printf (accessor, "[i%d]", n);
                _emit_encode_one (lcm, f, ls, lm, accessor->str, n+3);
            }
            
            g_string_free (accessor, TRUE);
        }
    }
    _flush_write_struct_fmt (f, struct_fmt, struct_members);

    g_queue_free (struct_fmt);
    g_queue_free (struct_members);
    fprintf (f, "\n");
}

static void
emit_python_encode (const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls)
{
    emit(1, "def encode(self):");
    emit(2, "buf = StringIO.StringIO()");
    emit(2, "buf.write(%s._get_packed_fingerprint())", 
            ls->structname->shortname);
    emit(2, "self._encode_one(buf)");
    emit(2, "return buf.getvalue()");
    fprintf (f, "\n");
}

static void
emit_member_initializer(const lcmgen_t* lcm, FILE *f, lcm_member_t* lm, 
        int dim_num)
{
    if(dim_num == lm->dimensions->len) {
        fprintf(f, "%s", nil_initializer_string(lm->type));
        return;
    }
    lcm_dimension_t *dim = 
        (lcm_dimension_t *) g_ptr_array_index (lm->dimensions, dim_num);
    if(dim->mode == LCM_VAR) {
        fprintf(f, "[]");
    } else {
        fprintf(f, "[ ");
        emit_member_initializer(lcm, f, lm, dim_num+1);
        fprintf(f, " for dim%d in range(%s) ]", dim_num, dim->size);
    }
}

static void
emit_python_init (const lcmgen_t *lcm, FILE *f, lcm_struct_t *lr)
{
    fprintf(f, "    def __init__(self):\n");
    unsigned int member;
    for (member = 0; member < lr->members->len; member++) {
        lcm_member_t *lm = (lcm_member_t *) g_ptr_array_index(lr->members, member);
        fprintf(f, "        self.%s = ", lm->membername);

        emit_member_initializer(lcm, f, lm, 0);
        fprintf(f, "\n");
    }
    if (0 == member) { fprintf(f, "        pass\n"); }
    fprintf(f, "\n");
}

static void
emit_python_fingerprint (const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls)
{
    const char *sn = ls->structname->shortname;
    emit (1, "_hash = None");

    emit (1, "def _get_hash_recursive(parents):");
    emit (2,     "if %s in parents: return 0", sn);
    for (unsigned int m = 0; m < ls->members->len; m++) {
        lcm_member_t *lm = (lcm_member_t *) g_ptr_array_index(ls->members, m);
        if (! lcm_is_primitive_type (lm->type->lctypename)) {
            emit (2,     "newparents = parents + [%s]", sn);
            break;
        }
    }
    emit_start (2, "tmphash = (0x%"PRIx64, ls->hash);
    for (unsigned int m = 0; m < ls->members->len; m++) {
        lcm_member_t *lm = (lcm_member_t *) g_ptr_array_index(ls->members, m);
        const char *msn = lm->type->shortname;
        if (! lcm_is_primitive_type (lm->type->lctypename)) {
            const char *ghr = "_get_hash_recursive(newparents)";
            if (is_same_type (lm->type, ls->structname)) {
                emit_continue ("+ %s.%s", msn, ghr);
            } else if (is_same_package (lm->type, ls->structname)) {
                emit_continue ("+ %s.%s.%s", msn, msn, ghr);
            } else {
                emit_continue ("+ %s.%s", lm->type->lctypename, ghr);
            }
        }
    }
    emit_end (") & 0xffffffffffffffff");
    emit (2, "tmphash  = (((tmphash<<1)&0xffffffffffffffff)  + "
            "(tmphash>>63)) & 0xffffffffffffffff");
    emit (2,     "return tmphash");
    emit (1, "_get_hash_recursive = staticmethod(_get_hash_recursive)");

    emit (1, "_packed_fingerprint = None");
    emit (0, "");
    emit (1, "def _get_packed_fingerprint():");
    emit (2,     "if %s._packed_fingerprint is None:", sn);
    emit (3,         "%s._packed_fingerprint = struct.pack(\">Q\", "
            "%s._get_hash_recursive([]))", sn, sn);
    emit (2,     "return %s._packed_fingerprint", sn);
    emit (1, "_get_packed_fingerprint = staticmethod(_get_packed_fingerprint)");
    fprintf (f, "\n");

}

static void
emit_python_dependencies (const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls)
{
    GHashTable *dependencies = g_hash_table_new (g_str_hash, g_str_equal);
    for (unsigned int m=0; m<ls->members->len; m++) {
        lcm_member_t *lm = (lcm_member_t *) g_ptr_array_index (ls->members, m);
        if (! lcm_is_primitive_type (lm->type->lctypename)) {
            if (strlen (lm->type->package) && 
                ! is_same_package (ls->structname, lm->type)) {
                if (! g_hash_table_lookup (dependencies, lm->type->lctypename)) {
                    g_hash_table_insert (dependencies, lm->type->lctypename, 
                            lm->type->lctypename);
                }
            } else if (! g_hash_table_lookup (dependencies, 
                        lm->type->shortname)){
                g_hash_table_insert (dependencies, lm->type->shortname, 
                        lm->type->shortname);
            }
        }
    }
    GPtrArray *deps = _hash_table_get_vals (dependencies);
    for (int i=0; i<deps->len; i++) {
        const char *package = (char *) g_ptr_array_index (deps, i);
        emit (0, "import %s\n", package);
    }
    g_ptr_array_free (deps, TRUE);
    g_hash_table_destroy (dependencies);
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

static int
emit_package (lcmgen_t *lcm, _package_contents_t *pc)
{
    // create the package directory, if necessary
    char **dirs = g_strsplit (pc->name, ".", 0);
    char *pdname = build_filenamev (dirs);
    char package_dir[PATH_MAX];
    char package_dir_prefix[PATH_MAX];
    int have_package = dirs[0] != NULL;

    sprintf (package_dir_prefix, "%s%s", getopt_get_string(lcm->gopt, "ppath"), 
            strlen(getopt_get_string(lcm->gopt, "ppath")) > 0 ? 
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

    // write the package __init__.py files, if necessary
    FILE *init_py_fp = NULL;
    GHashTable * init_py_imports = g_hash_table_new_full(g_str_hash, 
            g_str_equal, free, NULL);
    if (have_package) {
        int ndirs = 0;
        for (ndirs=0; dirs[ndirs]; ndirs++);

        for (int i=0 ; i<ndirs; i++) {
            char *initpy_fname_parts[1024];
            assert(ndirs + 4 < 1024);

            initpy_fname_parts[0] = package_dir_prefix;
            for (int j=0; j<=i; j++) {
                initpy_fname_parts[j+1] = dirs[j];
            }
            initpy_fname_parts[i+2] = "__init__.py";
            initpy_fname_parts[i+3] = NULL;

            char *initpy_fname = build_filenamev (initpy_fname_parts);
            int created_initpy = 0;

            // close init_py_fp if already open
            if(init_py_fp) {
            	fclose(init_py_fp);
            	init_py_fp = NULL;
            }

            if (! g_file_test (initpy_fname, G_FILE_TEST_EXISTS)) {
                // __init__.py does not exist for this package.  Create it.
                created_initpy = 1;
                init_py_fp = fopen(initpy_fname, "w");
            } else {
                // open the existing __init__.py file, and make note of the
                // modules it imports
                created_initpy = 0;
                init_py_fp = fopen(initpy_fname, "r+");
            }

            if (!init_py_fp) {
                perror ("fopen");
                free (initpy_fname);
                return -1;
            }
#ifndef WIN32
            // lock __init__.py for exclusive write access
            // TODO do the equivalent in windows
            struct flock lockinfo;
            lockinfo.l_type = F_WRLCK;
            lockinfo.l_start = 0;
            lockinfo.l_whence = SEEK_SET;
            lockinfo.l_len = 0 ;
            lockinfo.l_pid = getpid();
            if(0 != fcntl(fileno(init_py_fp), F_SETLKW, &lockinfo)) {
                perror("locking __init__.py");
                free(initpy_fname);
                fclose(init_py_fp);
                return -1;
            }
#endif

            if(created_initpy) {
                fprintf (init_py_fp, "\"\"\"LCM package __init__.py file\n"
                        "This file automatically generated by lcm-gen.\n"
                        "DO NOT MODIFY BY HAND!!!!\n"
                        "\"\"\"\n\n");
            } else {
                while(!feof(init_py_fp)) {
                    char buf[4096];
                    memset(buf, 0, sizeof(buf));
                    char *result = fgets(buf, sizeof(buf)-1, init_py_fp);
                    if(!result)
                        break;

                    g_strstrip(buf);
                    char **words = g_strsplit(buf, " ", -1);
                    if(!words[0] || !words[1] || !words[2] || !words[3])
                        continue;
                    if(!strcmp(words[0], "from") && !strcmp(words[2], "import")) {
                        char *module_name = strdup(words[1]);
                        g_hash_table_insert(init_py_imports, module_name, 
                                module_name);
                    }

                    g_strfreev(words);
                }
            }
            free (initpy_fname);
        }
    }
    g_strfreev (dirs);

    ////////////////////////////////////////////////////////////
    // ENUMS
    for (int i=0; i<pc->enums->len; i++) {
        lcm_enum_t *le = (lcm_enum_t *) g_ptr_array_index (pc->enums, i);

        char path[PATH_MAX];
        sprintf (path, "%s%s.py", package_dir, le->enumname->shortname);

        if(init_py_fp && 
           !g_hash_table_lookup(init_py_imports, le->enumname->shortname))
            fprintf(init_py_fp, "from %s import %s\n", 
                    le->enumname->shortname,
                    le->enumname->shortname);

        if (!lcm_needs_generation(lcm, le->lcmfile, path))
            continue;

        FILE *f = fopen(path, "w");
        if (f==NULL) return -1;

        fprintf(f, "\"\"\"LCM type definitions\n"
                "This file automatically generated by lcm.\n"
                "DO NOT MODIFY BY HAND!!!!\n"
                "\"\"\"\n"
                "\n"
                "import cStringIO as StringIO\n"
                "import struct\n");

        // enums always encoded as int32
        emit (0, "class %s(object):", le->enumname->shortname);
        emit (1, "__slots__ = [ \"value\" ]");
        for (unsigned int v = 0; v < le->values->len; v++) {
            lcm_enum_value_t *lev = (lcm_enum_value_t *) g_ptr_array_index(le->values, v);
            emit(1, "%s = %i", lev->valuename, lev->value);
        }

        emit (1, "_packed_fingerprint = struct.pack(\">Q\", 0x%"PRIx64")",
                le->hash);
        fprintf (f, "\n");

        emit (1, "def __init__ (self, value):");
        emit (2,     "self.value = value");
        fprintf (f, "\n");

        emit (1, "def _get_hash_recursive(parents):");
        emit (2,     "return 0x%"PRIx64, le->hash);
        emit (1, "_get_hash_recursive=staticmethod(_get_hash_recursive)");
        emit (1, "def _get_packed_fingerprint():");
        emit (2,     "return %s._packed_fingerprint", le->enumname->shortname);
        emit (1, "_get_packed_fingerprint = staticmethod(_get_packed_fingerprint)");
        fprintf (f, "\n");

        emit (1, "def encode(self):");
        emit (2,     "return struct.pack(\">Qi\", 0x%"PRIx64", self.value)",
                le->hash);

        emit (1, "def _encode_one(self, buf):");
        emit (2,     "buf.write (struct.pack(\">i\", self.value))");
        fprintf (f, "\n");

        emit (1, "def decode(data):");
        emit (2,     "if hasattr (data, 'read'):");
        emit (3,         "buf = data");
        emit (2,     "else:");
        emit (3,         "buf = StringIO.StringIO(data)");
        emit (2,     "if buf.read(8) != %s._packed_fingerprint:", 
                le->enumname->shortname);
        emit (3,         "raise ValueError(\"Decode error\")");
        emit (2,     "return %s(struct.unpack(\">i\", buf.read(4))[0])",
                le->enumname->shortname);
        emit (1, "decode = staticmethod(decode)");

        emit (1, "def _decode_one(buf):");
        emit (2,     "return %s(struct.unpack(\">i\", buf.read(4))[0])",
                le->enumname->shortname);
        emit (1, "_decode_one = staticmethod(_decode_one)");

        fprintf (f, "\n");
        fclose (f);
    }

    ////////////////////////////////////////////////////////////
    // STRUCTS
    for (int i = 0; i<pc->structs->len; i++) {
        lcm_struct_t *ls = (lcm_struct_t *) g_ptr_array_index(pc->structs, i);

        char path[PATH_MAX];
        sprintf (path, "%s%s.py", package_dir, ls->structname->shortname);

        if(init_py_fp && 
           !g_hash_table_lookup(init_py_imports, ls->structname->shortname))
            fprintf(init_py_fp, "from %s import %s\n", 
                    ls->structname->shortname,
                    ls->structname->shortname);

        if (!lcm_needs_generation(lcm, ls->lcmfile, path))
            continue;

        FILE *f = fopen(path, "w");
        if (f==NULL) return -1;

        fprintf(f, "\"\"\"LCM type definitions\n"
                "This file automatically generated by lcm.\n"
                "DO NOT MODIFY BY HAND!!!!\n"
                "\"\"\"\n"
                "\n"
                "import cStringIO as StringIO\n"
                "import struct\n\n");

        emit_python_dependencies (lcm, f, ls);

        fprintf(f, "class %s(object):\n", ls->structname->shortname);
        fprintf (f,"    __slots__ = [");
        for (unsigned int member = 0; member < ls->members->len; member++) {
            lcm_member_t *lm = (lcm_member_t *) g_ptr_array_index (ls->members, member);
            fprintf (f, "\"%s\"%s", lm->membername, 
                    member < ls->members->len-1 ? ", " : "");
        }
        fprintf (f, "]\n\n");

        // CONSTANTS
        for (unsigned int cn = 0; cn < g_ptr_array_size(ls->constants); cn++) {
            lcm_constant_t *lc = (lcm_constant_t *) g_ptr_array_index(ls->constants, cn);
            assert(lcm_is_legal_const_type(lc->lctypename));
            emit(1, "%s = %s", lc->membername, lc->val_str);
        }
        if (g_ptr_array_size(ls->constants) > 0)
            emit(0, "");

        emit_python_init (lcm, f, ls);
        emit_python_encode (lcm, f, ls);
        emit_python_encode_one (lcm, f, ls);
        emit_python_decode (lcm, f, ls);
        emit_python_decode_one (lcm, f, ls);
        emit_python_fingerprint (lcm, f, ls);
        fclose (f);
    }

    if(init_py_fp)
        fclose(init_py_fp);
    g_hash_table_destroy(init_py_imports);
    return 0;
}

int emit_python(lcmgen_t *lcm)
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
