#include <assert.h>
#include <ctype.h>
#include <fcntl.h>
#include <glib.h>
#include <inttypes.h>
#include <lcm/lcm_version.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/types.h>
#ifdef _MSC_VER
#include <lcm/windows/WinPorting.h>
#else
#include <unistd.h>
#endif

#include "lcmgen.h"

#define INDENT(n) (4 * (n))

#define emit_start(n, ...)                \
    do {                                  \
        fprintf(f, "%*s", INDENT(n), ""); \
        fprintf(f, __VA_ARGS__);          \
    } while (0)
#define emit_continue(...)       \
    do {                         \
        fprintf(f, __VA_ARGS__); \
    } while (0)
#define emit_end(...)            \
    do {                         \
        fprintf(f, __VA_ARGS__); \
        fprintf(f, "\n");        \
    } while (0)
#define emit(n, ...)                      \
    do {                                  \
        fprintf(f, "%*s", INDENT(n), ""); \
        fprintf(f, __VA_ARGS__);          \
        fprintf(f, "\n");                 \
    } while (0)

#define err(...) fprintf(stderr, __VA_ARGS__)

static void mkdir_with_parents(const char *path, mode_t mode)
{
#ifdef WIN32
    g_mkdir_with_parents(path, 0755);
#else
    int len = strlen(path);
    char *dirpath = malloc(len + 1);
    for (int i = 0; i < len; i++) {
        if (path[i] == '/') {
            strncpy(dirpath, path, i);
            dirpath[i] = '\0';
            mkdir(dirpath, mode);
            i++;  // skip the '/'
        }
    }
    free(dirpath);
#endif
}

static void emit_type_comment(FILE *f, lcm_member_t *structure_member)
{
    /* Might be nicer to construct a string. Eh. */
    // fprintf(f, "%*s", INDENT(2), "");
    fprintf(f, "LCM Type: %s", structure_member->type->lctypename);
    for (guint dim_num = 0; dim_num < structure_member->dimensions->len; dim_num++) {
        lcm_dimension_t *dim =
            (lcm_dimension_t *) g_ptr_array_index(structure_member->dimensions, dim_num);
        fprintf(f, "[%s]", dim->size);
    }
    // fprintf(f, "\n");
}

static void emit_comment(FILE *f, int indent, const char *comment)
{
    if (!comment)
        return;

    gchar **lines = g_strsplit(comment, "\n", 0);
    int num_lines = g_strv_length(lines);

    if (num_lines == 1) {
        // Pad s with spaces in case it starts or ends with a ".
        // Will break if the comment contains """.
        emit(indent, "\"\"\" %s \"\"\"", lines[0]);
    } else {
        emit(indent, "\"\"\"");
        for (int line_ind = 0; lines[line_ind]; line_ind++) {
            if (strlen(lines[line_ind])) {
                emit(indent, "%s", lines[line_ind]);
            } else {
                emit(indent, "");
            }
        }
        emit(indent, "\"\"\"");
    }
    g_strfreev(lines);
}

// comment: May be null
// structure_member: Not null
static void emit_member_comment(FILE *f, int indent, const char *comment,
                                lcm_member_t *structure_member)
{
    /* Numeric and array types are lost in the python type system.
     * Preserve this info after the type author's comments.
     */
    assert(structure_member != NULL);

    gchar **lines = NULL;
    int num_lines = 0;
    if (comment) {
        lines = g_strsplit(comment, "\n", 0);
        num_lines = g_strv_length(lines);
    }

    if (num_lines == 0) {
        fprintf(f, "%*s", INDENT(2), "");
        fprintf(f, "\"\"\" ");
        emit_type_comment(f, structure_member);
        fprintf(f, " \"\"\"");

    } else {
        emit(indent, "\"\"\"");

        for (int line_ind = 0; lines[line_ind]; line_ind++) {
            if (strlen(lines[line_ind])) {
                emit(indent, "%s", lines[line_ind]);
            } else {
                emit(indent, "");
            }
        }

        fprintf(f, "%*s", INDENT(2), "");
        emit_type_comment(f, structure_member);
        fprintf(f, "\n");

        emit(indent, "\"\"\"");
    }
    g_strfreev(lines);
}

static char *build_filenamev(char **parts)
{
    char **p = parts;
    int total_len = 0;
    for (p = parts; *p; p++) {
        total_len += strlen(*p) + strlen(G_DIR_SEPARATOR_S);
    }
    total_len++;
    char *result = (char *) malloc(total_len);
    memset(result, 0, total_len);
    for (p = parts; *p; p++) {
        if (!strlen(*p))
            continue;
        strncat(result, *p, total_len);
        if (*(p + 1))
            strncat(result, G_DIR_SEPARATOR_S, total_len);
    }
    return result;
}

static void get_all_vals_helper(gpointer key, gpointer value, gpointer user_data)
{
    GPtrArray *vals = (GPtrArray *) user_data;

    g_ptr_array_add(vals, value);
}

static GPtrArray *_hash_table_get_vals(GHashTable *hash_table)
{
    GPtrArray *vals = g_ptr_array_sized_new(g_hash_table_size(hash_table));
    g_hash_table_foreach(hash_table, get_all_vals_helper, vals);
    return vals;
}

void setup_python_options(getopt_t *gopt)
{
    getopt_add_string(gopt, 0, "ppath", "", "Python destination directory");
    getopt_add_bool(gopt, 0, "python-no-init", 0, "Do not create __init__.py");
}

static int is_same_type(const lcm_typename_t *tn1, const lcm_typename_t *tn2)
{
    return !strcmp(tn1->lctypename, tn2->lctypename);
}

// static int
// is_same_package (const lcm_typename_t *tn1, const lcm_typename_t *tn2) {
//    return ! strcmp (tn1->package, tn2->package);
//}

static char _struct_format(lcm_member_t *member)
{
    const char *type_name = member->type->lctypename;
    if (!strcmp("byte", type_name))
        return 'B';
    if (!strcmp("boolean", type_name))
        return 'b';
    if (!strcmp("int8_t", type_name))
        return 'b';
    if (!strcmp("int16_t", type_name))
        return 'h';
    if (!strcmp("int32_t", type_name))
        return 'i';
    if (!strcmp("int64_t", type_name))
        return 'q';
    if (!strcmp("float", type_name))
        return 'f';
    if (!strcmp("double", type_name))
        return 'd';
    return 0;
}

static int _primitive_type_size(const char *type_name)
{
    if (!strcmp("byte", type_name))
        return 1;
    if (!strcmp("boolean", type_name))
        return 1;
    if (!strcmp("int8_t", type_name))
        return 1;
    if (!strcmp("int16_t", type_name))
        return 2;
    if (!strcmp("int32_t", type_name))
        return 4;
    if (!strcmp("int64_t", type_name))
        return 8;
    if (!strcmp("float", type_name))
        return 4;
    if (!strcmp("double", type_name))
        return 8;
    assert(0);
    return 0;
}

static void _emit_decode_one(const lcmgen_t *lcm, FILE *f, lcm_struct_t *structure,
                             lcm_member_t *structure_member, const char *accessor, int indent,
                             const char *sfx)
{
    const char *type_name = structure_member->type->lctypename;
    const char *member_name = structure_member->membername;
    const char *short_name = structure_member->type->shortname;
    if (!strcmp("string", type_name)) {
        emit(indent, "__%s_len = struct.unpack('>I', buf.read(4))[0]", member_name);
        emit(indent, "%sbuf.read(__%s_len)[:-1].decode('utf-8', 'replace')%s", accessor,
             member_name, sfx);
    } else if (!strcmp("byte", type_name)) {
        emit(indent, "%sstruct.unpack('B', buf.read(1))[0]%s", accessor, sfx);
    } else if (!(strcmp("boolean", type_name))) {
        emit(indent, "%sbool(struct.unpack('b', buf.read(1))[0])%s", accessor, sfx);
    } else if (!strcmp("int8_t", type_name)) {
        emit(indent, "%sstruct.unpack('b', buf.read(1))[0]%s", accessor, sfx);
    } else if (!strcmp("int16_t", type_name)) {
        emit(indent, "%sstruct.unpack('>h', buf.read(2))[0]%s", accessor, sfx);
    } else if (!strcmp("int32_t", type_name)) {
        emit(indent, "%sstruct.unpack('>i', buf.read(4))[0]%s", accessor, sfx);
    } else if (!strcmp("int64_t", type_name)) {
        emit(indent, "%sstruct.unpack('>q', buf.read(8))[0]%s", accessor, sfx);
    } else if (!strcmp("float", type_name)) {
        emit(indent, "%sstruct.unpack('>f', buf.read(4))[0]%s", accessor, sfx);
    } else if (!strcmp("double", type_name)) {
        emit(indent, "%sstruct.unpack('>d', buf.read(8))[0]%s", accessor, sfx);
    } else {
        if (is_same_type(structure_member->type, structure->structname)) {
            emit(indent, "%s%s._decode_one(buf)%s", accessor, short_name, sfx);
        } else {
            emit(indent, "%s%s._decode_one(buf)%s", accessor, type_name, sfx);
        }
    }
}

static void _emit_decode_list(const lcmgen_t *lcm, FILE *f, lcm_struct_t *structure,
                              lcm_member_t *structure_member, const char *accessor, int indent,
                              int is_first, const char *len, int fixed_len)
{
    const char *type_name = structure_member->type->lctypename;
    const char *suffix = "";
    if (!is_first) {
        suffix = ")";
    }
    if (!strcmp("byte", type_name)) {
        emit(indent, "%sbuf.read(%s%s)%s", accessor, fixed_len ? "" : "self.", len, suffix);
    } else if (!strcmp("boolean", type_name)) {
        if (fixed_len) {
            emit(indent, "%s[bool(x) for x in struct.unpack('>%s%c', buf.read(%d))]%s", accessor,
                 len, _struct_format(structure_member), atoi(len) * _primitive_type_size(type_name),
                 suffix);
        } else {
            emit(indent,
                 "%s[bool(x) for x in struct.unpack('>%%d%c' %% self.%s, buf.read(self.%s))]%s",
                 accessor, _struct_format(structure_member), len, len, suffix);
        }
    } else if (!strcmp("int8_t", type_name) || !strcmp("int16_t", type_name) ||
               !strcmp("int32_t", type_name) || !strcmp("int64_t", type_name) ||
               !strcmp("float", type_name) || !strcmp("double", type_name)) {
        if (fixed_len) {
            emit(indent, "%sstruct.unpack('>%s%c', buf.read(%d))%s", accessor, len,
                 _struct_format(structure_member), atoi(len) * _primitive_type_size(type_name),
                 suffix);
        } else {
            if (_primitive_type_size(type_name) > 1) {
                emit(indent, "%sstruct.unpack('>%%d%c' %% self.%s, buf.read(self.%s * %d))%s",
                     accessor, _struct_format(structure_member), len, len,
                     _primitive_type_size(type_name), suffix);
            } else {
                emit(indent, "%sstruct.unpack('>%%d%c' %% self.%s, buf.read(self.%s))%s", accessor,
                     _struct_format(structure_member), len, len, suffix);
            }
        }
    } else {
        assert(0);
    }
}

static void _flush_read_struct_fmt(const lcmgen_t *lcm, FILE *f, GQueue *formats,
                                   GQueue *member_queue)
{
    int nfmts = g_queue_get_length(formats);
    assert(nfmts == g_queue_get_length(member_queue));
    if (nfmts == 0)
        return;

    fprintf(f, "        ");
    int fmtsize = 0;
    while (!g_queue_is_empty(member_queue)) {
        lcm_member_t *member = (lcm_member_t *) g_queue_pop_head(member_queue);
        emit_continue("self.%s", member->membername);
        if (!g_queue_is_empty(member_queue)) {
            emit_continue(", ");
        }
        fmtsize += _primitive_type_size(member->type->lctypename);
    }
    emit_continue(" = struct.unpack(\">");
    while (!g_queue_is_empty(formats)) {
        emit_continue("%c", GPOINTER_TO_INT(g_queue_pop_head(formats)));
    }
    emit_end("\", buf.read(%d))%s", fmtsize, nfmts == 1 ? "[0]" : "");
}

static void emit_python_decode_one(const lcmgen_t *lcm, FILE *f, lcm_struct_t *structure)
{
    emit(1, "@staticmethod");
    emit(1, "def _decode_one(buf):");
    emit(2, "self = %s()", structure->structname->shortname);

    GQueue *struct_fmt = g_queue_new();
    GQueue *struct_members = g_queue_new();

    for (unsigned int m = 0; m < g_ptr_array_size(structure->members); m++) {
        lcm_member_t *structure_member = (lcm_member_t *) g_ptr_array_index(structure->members, m);
        char fmt = _struct_format(structure_member);

        if (!structure_member->dimensions->len) {
            if (fmt && strcmp(structure_member->type->lctypename, "boolean")) {
                g_queue_push_tail(struct_fmt, GINT_TO_POINTER((int) fmt));
                g_queue_push_tail(struct_members, structure_member);
            } else {
                _flush_read_struct_fmt(lcm, f, struct_fmt, struct_members);
                char *accessor = g_strdup_printf("self.%s = ", structure_member->membername);
                _emit_decode_one(lcm, f, structure, structure_member, accessor, 2, "");
                g_free(accessor);
            }
        } else {
            _flush_read_struct_fmt(lcm, f, struct_fmt, struct_members);

            GString *accessor = g_string_new("");
            g_string_append_printf(accessor, "self.%s", structure_member->membername);

            // iterate through the dimensions of the member, building up
            // an accessor string, and emitting for loops
            unsigned int n;
            for (n = 0; n < structure_member->dimensions->len - 1; n++) {
                lcm_dimension_t *dim =
                    (lcm_dimension_t *) g_ptr_array_index(structure_member->dimensions, n);

                if (n == 0) {
                    emit(2, "%s = []", accessor->str);
                } else {
                    emit(2 + n, "%s.append([])", accessor->str);
                }

                if (dim->mode == LCM_CONST) {
                    emit(2 + n, "for i%d in range(%s):", n, dim->size);
                } else {
                    emit(2 + n, "for i%d in range(self.%s):", n, dim->size);
                }

                if (n > 0 && n < structure_member->dimensions->len - 1) {
                    g_string_append_printf(accessor, "[i%d]", n - 1);
                }
            }

            // last dimension.
            lcm_dimension_t *last_dim = (lcm_dimension_t *) g_ptr_array_index(
                structure_member->dimensions, structure_member->dimensions->len - 1);
            int last_dim_fixed_len = last_dim->mode == LCM_CONST;

            if (lcm_is_primitive_type(structure_member->type->lctypename) &&
                0 != strcmp(structure_member->type->lctypename, "string")) {
                // member is a primitive non-string type.  Emit code to
                // decode a full array in one call to struct.unpack
                if (n == 0) {
                    g_string_append_printf(accessor, " = ");
                } else {
                    g_string_append_printf(accessor, ".append(");
                }

                _emit_decode_list(lcm, f, structure, structure_member, accessor->str, 2 + n, n == 0,
                                  last_dim->size, last_dim_fixed_len);
            } else {
                // member is either a string type or an inner LCM type.  Each
                // array element must be decoded individually
                if (n == 0) {
                    emit(2, "%s = []", accessor->str);
                } else {
                    emit(2 + n, "%s.append ([])", accessor->str);
                    g_string_append_printf(accessor, "[i%d]", n - 1);
                }
                if (last_dim_fixed_len) {
                    emit(2 + n, "for i%d in range(%s):", n, last_dim->size);
                } else {
                    emit(2 + n, "for i%d in range(self.%s):", n, last_dim->size);
                }
                g_string_append_printf(accessor, ".append(");
                _emit_decode_one(lcm, f, structure, structure_member, accessor->str, n + 3, ")");
            }
            g_string_free(accessor, TRUE);
        }
    }
    _flush_read_struct_fmt(lcm, f, struct_fmt, struct_members);
    emit(2, "return self");

    g_queue_free(struct_fmt);
    g_queue_free(struct_members);
    fprintf(f, "\n");
}

static void emit_python_decode(const lcmgen_t *lcm, FILE *f, lcm_struct_t *structure)
{
    // clang-format off
    emit(1, "@staticmethod");
    emit(1, "def decode(data: bytes):");
    emit(2,     "if hasattr(data, 'read'):");
    emit(3,         "buf = data");
    emit(2,     "else:");
    emit(3,         "buf = BytesIO(data)");
    emit(2,     "if buf.read(8) != %s._get_packed_fingerprint():", structure->structname->shortname);
    emit(3,         "raise ValueError(\"Decode error\")");
    emit(2,     "return %s._decode_one(buf)", structure->structname->shortname);
    fprintf (f, "\n");
    // clang-format on
}

static void _emit_encode_one(const lcmgen_t *lcm, FILE *f, lcm_struct_t *structure,
                             lcm_member_t *structure_member, const char *accessor, int indent)
{
    const char *type_name = structure_member->type->lctypename;
    const char *member_name = structure_member->membername;
    if (!strcmp("string", type_name)) {
        emit(indent, "__%s_encoded = %s.encode('utf-8')", member_name, accessor);
        emit(indent, "buf.write(struct.pack('>I', len(__%s_encoded)+1))", member_name);
        emit(indent, "buf.write(__%s_encoded)", member_name);
        emit(indent, "buf.write(b\"\\0\")");
    } else if (!strcmp("byte", type_name)) {
        emit(indent, "buf.write(struct.pack('B', %s))", accessor);
    } else if (!strcmp("int8_t", type_name) || !strcmp("boolean", type_name)) {
        emit(indent, "buf.write(struct.pack('b', %s))", accessor);
    } else if (!strcmp("int16_t", type_name)) {
        emit(indent, "buf.write(struct.pack('>h', %s))", accessor);
    } else if (!strcmp("int32_t", type_name)) {
        emit(indent, "buf.write(struct.pack('>i', %s))", accessor);
    } else if (!strcmp("int64_t", type_name)) {
        emit(indent, "buf.write(struct.pack('>q', %s))", accessor);
    } else if (!strcmp("float", type_name)) {
        emit(indent, "buf.write(struct.pack('>f', %s))", accessor);
    } else if (!strcmp("double", type_name)) {
        emit(indent, "buf.write(struct.pack('>d', %s))", accessor);
    } else {
        const char *short_name = structure_member->type->shortname;
        const char *gpf = "_get_packed_fingerprint()";
        if (is_same_type(structure_member->type, structure->structname)) {
            emit(indent, "assert %s.%s == %s.%s", accessor, gpf, short_name, gpf);
        } else {
            emit(indent, "assert %s.%s == %s.%s", accessor, gpf, type_name, gpf);
        }
        emit(indent, "%s._encode_one(buf)", accessor);
    }
}

static void _emit_encode_list(const lcmgen_t *lcm, FILE *f, lcm_struct_t *structure,
                              lcm_member_t *structure_member, const char *accessor, int indent,
                              const char *len, int fixed_len)
{
    const char *type_name = structure_member->type->lctypename;
    if (!strcmp("byte", type_name)) {
        emit(indent, "buf.write(bytearray(%s[:%s%s]))", accessor, (fixed_len ? "" : "self."), len);
        return;
    } else if (!strcmp("boolean", type_name) || !strcmp("int8_t", type_name) ||
               !strcmp("int16_t", type_name) || !strcmp("int32_t", type_name) ||
               !strcmp("int64_t", type_name) || !strcmp("float", type_name) ||
               !strcmp("double", type_name)) {
        if (fixed_len) {
            emit(indent, "buf.write(struct.pack('>%s%c', *%s[:%s]))", len,
                 _struct_format(structure_member), accessor, len);
        } else {
            emit(indent, "buf.write(struct.pack('>%%d%c' %% self.%s, *%s[:self.%s]))",
                 _struct_format(structure_member), len, accessor, len);
        }
    } else {
        assert(0);
    }
}

static void _flush_write_struct_fmt(FILE *f, GQueue *formats, GQueue *members)
{
    assert(g_queue_get_length(formats) == g_queue_get_length(members));
    if (g_queue_is_empty(formats))
        return;
    emit_start(2, "buf.write(struct.pack(\">");
    while (!g_queue_is_empty(formats)) {
        emit_continue("%c", GPOINTER_TO_INT(g_queue_pop_head(formats)));
    }
    emit_continue("\", ");
    while (!g_queue_is_empty(members)) {
        lcm_member_t *lm = (lcm_member_t *) g_queue_pop_head(members);
        emit_continue("self.%s", lm->membername);
        if (!g_queue_is_empty(members)) {
            emit_continue(", ");
        }
    }
    emit_end("))");
}

static void emit_python_encode_one(const lcmgen_t *lcm, FILE *f, lcm_struct_t *structure)
{
    emit(1, "def _encode_one(self, buf):");
    if (!g_ptr_array_size(structure->members))
        emit(2, "pass");

    GQueue *struct_fmt = g_queue_new();
    GQueue *struct_members = g_queue_new();

    for (unsigned int m = 0; m < g_ptr_array_size(structure->members); m++) {
        lcm_member_t *member = (lcm_member_t *) g_ptr_array_index(structure->members, m);
        char fmt = _struct_format(member);

        if (!member->dimensions->len) {
            if (fmt) {
                g_queue_push_tail(struct_fmt, GINT_TO_POINTER((int) fmt));
                g_queue_push_tail(struct_members, member);
            } else {
                _flush_write_struct_fmt(f, struct_fmt, struct_members);
                char *accessor = g_strdup_printf("self.%s", member->membername);
                _emit_encode_one(lcm, f, structure, member, accessor, 2);
                g_free(accessor);
            }
        } else {
            _flush_write_struct_fmt(f, struct_fmt, struct_members);
            GString *accessor = g_string_new("");
            g_string_append_printf(accessor, "self.%s", member->membername);

            unsigned int n;
            for (n = 0; n < member->dimensions->len - 1; n++) {
                lcm_dimension_t *dim = (lcm_dimension_t *) g_ptr_array_index(member->dimensions, n);

                g_string_append_printf(accessor, "[i%d]", n);
                if (dim->mode == LCM_CONST) {
                    emit(2 + n, "for i%d in range(%s):", n, dim->size);
                } else {
                    emit(2 + n, "for i%d in range(self.%s):", n, dim->size);
                }
            }

            // last dimension.
            lcm_dimension_t *last_dim = (lcm_dimension_t *) g_ptr_array_index(
                member->dimensions, member->dimensions->len - 1);
            int last_dim_fixed_len = last_dim->mode == LCM_CONST;

            if (lcm_is_primitive_type(member->type->lctypename) &&
                0 != strcmp(member->type->lctypename, "string")) {
                _emit_encode_list(lcm, f, structure, member, accessor->str, 2 + n, last_dim->size,
                                  last_dim_fixed_len);
            } else {
                if (last_dim_fixed_len) {
                    emit(2 + n, "for i%d in range(%s):", n, last_dim->size);
                } else {
                    emit(2 + n, "for i%d in range(self.%s):", n, last_dim->size);
                }
                g_string_append_printf(accessor, "[i%d]", n);
                _emit_encode_one(lcm, f, structure, member, accessor->str, n + 3);
            }

            g_string_free(accessor, TRUE);
        }
    }
    _flush_write_struct_fmt(f, struct_fmt, struct_members);

    g_queue_free(struct_fmt);
    g_queue_free(struct_members);
    fprintf(f, "\n");
}

static void emit_python_encode(const lcmgen_t *lcm, FILE *f, lcm_struct_t *structure)
{
    emit(1, "def encode(self):");
    emit(2, "buf = BytesIO()");
    emit(2, "buf.write(%s._get_packed_fingerprint())", structure->structname->shortname);
    emit(2, "self._encode_one(buf)");
    emit(2, "return buf.getvalue()");
    fprintf(f, "\n");
}

static void emit_member_initializer(const lcmgen_t *lcm, FILE *f, lcm_member_t *structure_member,
                                    int dim_num)
{
    if (dim_num == structure_member->dimensions->len) {
        const char *type_name = structure_member->type->lctypename;
        const char *initializer = NULL;
        if (!strcmp(type_name, "byte"))
            initializer = "0";
        else if (!strcmp(type_name, "boolean"))
            initializer = "False";
        else if (!strcmp(type_name, "int8_t"))
            initializer = "0";
        else if (!strcmp(type_name, "int16_t"))
            initializer = "0";
        else if (!strcmp(type_name, "int32_t"))
            initializer = "0";
        else if (!strcmp(type_name, "int64_t"))
            initializer = "0";
        else if (!strcmp(type_name, "float"))
            initializer = "0.0";
        else if (!strcmp(type_name, "double"))
            initializer = "0.0";
        else if (!strcmp(type_name, "string"))
            initializer = "\"\"";

        if (initializer != NULL) {
            fprintf(f, "%s", initializer);
        } else {
            fprintf(f, "%s()", type_name);
        }
        return;
    }
    if (dim_num == structure_member->dimensions->len - 1 &&
        // Arrays of bytes get treated as byte strings, so that they can be more
        // efficiently packed and unpacked.
        !strcmp(structure_member->type->lctypename, "byte")) {
        fprintf(f, "b\"\"");
        return;
    }
    lcm_dimension_t *dim =
        (lcm_dimension_t *) g_ptr_array_index(structure_member->dimensions, dim_num);
    if (dim->mode == LCM_VAR) {
        fprintf(f, "[]");
    } else {
        fprintf(f, "[ ");
        emit_member_initializer(lcm, f, structure_member, dim_num + 1);
        fprintf(f, " for dim%d in range(%s) ]", dim_num, dim->size);
    }
}

static void emit_python_init(const lcmgen_t *lcm, FILE *f, lcm_struct_t *structure)
{
    fprintf(f, "    def __init__(self):\n");
    unsigned int m;
    for (m = 0; m < structure->members->len; m++) {
        lcm_member_t *member = (lcm_member_t *) g_ptr_array_index(structure->members, m);
        fprintf(f, "        self.%s = ", member->membername);

        emit_member_initializer(lcm, f, member, 0);
        fprintf(f, "\n");
        emit_member_comment(f, 2, member->comment, member);
        fprintf(f, "\n");
    }
    if (0 == m) {
        fprintf(f, "        pass\n");
    }
    fprintf(f, "\n");
}

static void emit_python_fingerprint(const lcmgen_t *lcm, FILE *f, lcm_struct_t *structure)
{
    const char *short_name = structure->structname->shortname;
    emit(1, "@staticmethod");
    emit(1, "def _get_hash_recursive(parents):");
    emit(2, "if %s in parents: return 0", short_name);
    for (unsigned int m = 0; m < structure->members->len; m++) {
        lcm_member_t *member = (lcm_member_t *) g_ptr_array_index(structure->members, m);
        if (!lcm_is_primitive_type(member->type->lctypename)) {
            emit(2, "newparents = parents + [%s]", short_name);
            break;
        }
    }
    emit_start(2, "tmphash = (0x%" PRIx64, structure->hash);
    for (unsigned int m = 0; m < structure->members->len; m++) {
        lcm_member_t *member = (lcm_member_t *) g_ptr_array_index(structure->members, m);
        const char *msn = member->type->shortname;
        if (!lcm_is_primitive_type(member->type->lctypename)) {
            const char *ghr = "_get_hash_recursive(newparents)";
            if (is_same_type(member->type, structure->structname) ||
                strlen(member->type->package) == 0) {
                emit_continue("+ %s.%s", msn, ghr);
            } else {
                emit_continue("+ %s.%s.%s", member->type->package, msn, ghr);
            }
        }
    }
    emit_end(") & 0xffffffffffffffff");
    // clang-format off
    emit(2,     "tmphash  = (((tmphash<<1)&0xffffffffffffffff) + "
                             "(tmphash>>63)) & 0xffffffffffffffff");
    emit(2,     "return tmphash");


    emit(1, "_packed_fingerprint = None");
    emit(0, "");

    emit(1, "@staticmethod");
    emit(1, "def _get_packed_fingerprint():");
    emit(2,     "if %s._packed_fingerprint is None:", short_name);
    emit(3,         "%s._packed_fingerprint = struct.pack("
                             "\">Q\", %s._get_hash_recursive([]))", short_name, short_name);
    emit(2,     "return %s._packed_fingerprint", short_name);
    // clang-format off
    fprintf (f, "\n");

    emit(1, "def get_hash(self):");
    emit(2,     "\"\"\"Get the LCM hash of the struct\"\"\"");
    emit(2,     "return struct.unpack(\">Q\", %s._get_packed_fingerprint())[0]", short_name);
    fprintf(f, "\n");
}

static void
emit_python_dependencies (const lcmgen_t *lcm, FILE *f, lcm_struct_t *structure, int write_init_py )
{
    // Find the set of types to import
    GHashTable *dependencies = g_hash_table_new (g_str_hash, g_str_equal);
    for (unsigned int m=0; m < structure->members->len; m++) {
        lcm_member_t *member = (lcm_member_t *) g_ptr_array_index (structure->members, m);
        if (lcm_is_primitive_type(member->type->lctypename)){
            continue;
        }
        int no_package = g_str_equal(member->type->package, "");
        if (write_init_py && !no_package) {
            // pyright (The vscode python static analyzer) refuses to understand
            // `import foo.bar` in cases where `import foo` works.
            // https://github.com/microsoft/pyright/issues/6674
            // Therefore, when __init__.py is generated, import only the package name.
            if (! g_hash_table_lookup (dependencies, member->type->package)) {
                g_hash_table_insert (dependencies, member->type->package,
                                    member->type->package);
            }
        } else {
            // Otherwise import each full type.
            if (! g_hash_table_lookup (dependencies, member->type->lctypename)) {
                g_hash_table_insert (dependencies, member->type->lctypename,
                                    member->type->lctypename);
            }
        }
    }

    // Emit the set of imports.
    GPtrArray *deps = _hash_table_get_vals (dependencies);
    for (int i=0; i < deps->len; i++) {
        const char *package = (char *) g_ptr_array_index (deps, i);
        emit(0, "import %s\n", package);
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
    _package_contents_t *package = (_package_contents_t *) malloc (sizeof(_package_contents_t));
    package->enums = g_ptr_array_new ();
    package->structs = g_ptr_array_new ();
    package->name = strdup (name);
    return package;
}

static void _package_contents_free (_package_contents_t *package)
{
    g_ptr_array_free (package->enums, TRUE);
    g_ptr_array_free (package->structs, TRUE);
    free (package->name);
    free (package);
}

static int
emit_package (lcmgen_t *lcm, _package_contents_t *package)
{
    // create the package directory, if necessary
    char **dirs = g_strsplit (package->name, ".", 0);
    char *pdname = build_filenamev (dirs);
    char package_dir[PATH_MAX];
    char package_dir_prefix[PATH_MAX];
    int have_package = dirs[0] != NULL;
    int write_init_py = !getopt_get_bool(lcm->gopt, "python-no-init");

    int ret = snprintf(package_dir_prefix, PATH_MAX, "%s%s", getopt_get_string(lcm->gopt, "ppath"),
                       strlen(getopt_get_string(lcm->gopt, "ppath")) > 0 ? G_DIR_SEPARATOR_S : "");
    if (ret >= PATH_MAX || ret < 0) {
	    free(pdname);
	    err("Could not create package directory prefix string\n");
	    return -1;
    }
    ret = snprintf(package_dir, PATH_MAX, "%s%s%s", package_dir_prefix, pdname,
                   have_package ? G_DIR_SEPARATOR_S : "");
    if (ret >= PATH_MAX || ret < 0) {
	    free(pdname);
	    err("Could not create package directory string\n");
	    return -1;
    }
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
    if (have_package && write_init_py) {
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
                        "lcm-gen " LCM_VERSION_STRING "\n"
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
                        char *module_name = strdup(words[1]+1); // ignore leading dot
                        g_hash_table_replace(init_py_imports, module_name,
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
    for (int i=0; i < package->enums->len; i++) {
        lcm_enum_t *enumeration = (lcm_enum_t *) g_ptr_array_index (package->enums, i);

        char path[PATH_MAX];
        int return_value = snprintf(path, sizeof(path), "%s%s.py", package_dir, enumeration->enumname->shortname);
        if (return_value < 0) {
            fprintf(stderr, "Error: failed to create path string");
            return -1;
        }

        if(init_py_fp &&
           !g_hash_table_lookup(init_py_imports, enumeration->enumname->shortname))
            fprintf(init_py_fp, "from .%s import %s as %s\n",
                    enumeration->enumname->shortname,
                    enumeration->enumname->shortname,
                    enumeration->enumname->shortname);

        if (!lcm_needs_generation(lcm, enumeration->lcmfile, path))
            continue;

        FILE *f = fopen(path, "w");
        if (f==NULL) return -1;

        fprintf(f, "\"\"\"LCM type definitions\n"
                "This file automatically generated by lcm.\n"
                "DO NOT MODIFY BY HAND!!!!\n"
                "lcm-gen " LCM_VERSION_STRING "\n"
                "\"\"\"\n"
                "\n"
                "from io import BytesIO\n"
                "import struct\n\n");

        // enums always encoded as int32
        emit (0, "class %s(object):", enumeration->enumname->shortname);
        emit (1, "__slots__ = [ \"value\" ]");
        for (unsigned int v = 0; v < enumeration->values->len; v++) {
            lcm_enum_value_t *value = (lcm_enum_value_t *) g_ptr_array_index(enumeration->values, v);
            emit(1, "%s = %i", value->valuename, value->value);
        }

        // clang-format off
        emit(1, "_packed_fingerprint = struct.pack(\">Q\", 0x%"PRIx64")", enumeration->hash);
        fprintf(f, "\n");

        emit(1, "def __init__ (self, value):");
        emit(2,     "self.value = value");
        fprintf(f, "\n");

        emit(1, "@staticmethod");
        emit(1, "def _get_hash_recursive(parents):");
        emit(2,     "return 0x%"PRIx64, enumeration->hash);

        emit(1, "@staticmethod");
        emit(1, "def _get_packed_fingerprint():");
        emit(2,     "return %s._packed_fingerprint", enumeration->enumname->shortname);
        fprintf(f, "\n");

        emit(1, "def encode(self):");
        emit(2,     "return struct.pack(\">Qi\", 0x%"PRIx64", self.value)", enumeration->hash);

        emit(1, "def _encode_one(self, buf):");
        emit(2,     "buf.write (struct.pack(\">i\", self.value))");
        fprintf(f, "\n");

        emit(1, "@staticmethod");
        emit(1, "def decode(data):");
        emit(2,     "if hasattr (data, 'read'):");
        emit(3,         "buf = data");
        emit(2,     "else:");
        emit(3,         "buf = BytesIO(data)");
        emit(2,     "if buf.read(8) != %s._packed_fingerprint:", enumeration->enumname->shortname);
        emit(3,         "raise ValueError(\"Decode error\")");
        emit(2,     "return %s(struct.unpack(\">i\", buf.read(4))[0])", enumeration->enumname->shortname);


        emit(1, "@staticmethod");
        emit(1, "def _decode_one(buf):");
        emit(2,     "return %s(struct.unpack(\">i\", buf.read(4))[0])", enumeration->enumname->shortname);
        // clang-format on

        fprintf(f, "\n");
        fclose(f);
    }

    ////////////////////////////////////////////////////////////
    // STRUCTS
    for (int i = 0; i < package->structs->len; i++) {
        lcm_struct_t *structure = (lcm_struct_t *) g_ptr_array_index(package->structs, i);

        char path[PATH_MAX];
        int return_value =
            snprintf(path, sizeof(path), "%s%s.py", package_dir, structure->structname->shortname);
        if (return_value < 0) {
            fprintf(stderr, "Error: failed to create path string");
            return -1;
        }

        if (init_py_fp && !g_hash_table_lookup(init_py_imports, structure->structname->shortname))
            fprintf(init_py_fp, "from .%s import %s as %s\n", structure->structname->shortname,
                    structure->structname->shortname, structure->structname->shortname);

        if (!lcm_needs_generation(lcm, structure->lcmfile, path))
            continue;

        FILE *f = fopen(path, "w");
        if (f == NULL)
            return -1;

        fprintf(f,
                "\"\"\"LCM type definitions\n"
                "This file automatically generated by lcm.\n"
                "DO NOT MODIFY BY HAND!!!!\n"
                "\"\"\"\n"
                "\n");

        emit_comment(f, 0, structure->file_comment);

        fprintf(f,
                "\n"
                "from io import BytesIO\n"
                "import struct\n\n");

        emit_python_dependencies(lcm, f, structure, write_init_py);

        fprintf(f, "class %s(object):\n", structure->structname->shortname);
        emit_comment(f, 1, structure->comment);
        fprintf(f, "\n");

        fprintf(f, "    __slots__ = [");
        for (unsigned int m = 0; m < structure->members->len; m++) {
            lcm_member_t *member = (lcm_member_t *) g_ptr_array_index(structure->members, m);
            fprintf(f, "\"%s\"%s", member->membername, m < structure->members->len - 1 ? ", " : "");
        }
        fprintf(f, "]\n\n");

        fprintf(f, "    __typenames__ = [");
        for (unsigned int m = 0; m < structure->members->len; m++) {
            lcm_member_t *member = (lcm_member_t *) g_ptr_array_index(structure->members, m);
            fprintf(f, "\"%s\"%s", member->type->lctypename,
                    m < structure->members->len - 1 ? ", " : "");
        }
        fprintf(f, "]\n\n");

        fprintf(f, "    __dimensions__ = [");
        for (unsigned int m = 0; m < structure->members->len; m++) {
            lcm_member_t *member = (lcm_member_t *) g_ptr_array_index(structure->members, m);
            if (!member->dimensions->len) {
                fprintf(f, "None");
            } else {
                fprintf(f, "[");
                for (int n = 0; n < member->dimensions->len; n++) {
                    lcm_dimension_t *dim =
                        (lcm_dimension_t *) g_ptr_array_index(member->dimensions, n);
                    if (dim->mode == LCM_CONST) {
                        fprintf(f, "%s", dim->size);
                    } else {
                        fprintf(f, "\"%s\"", dim->size);
                    }
                    fprintf(f, "%s", (n < member->dimensions->len - 1) ? ", " : "");
                }
                fprintf(f, "]");
            }
            gboolean has_more = (m < structure->members->len - 1);
            fprintf(f, "%s", has_more ? ", " : "");
        }
        fprintf(f, "]\n\n");

        // CONSTANTS
        for (unsigned int ii = 0; ii < g_ptr_array_size(structure->constants); ii++) {
            lcm_constant_t *constant =
                (lcm_constant_t *) g_ptr_array_index(structure->constants, ii);
            assert(lcm_is_legal_const_type(constant->lctypename));
            emit(1, "%s = %s", constant->membername, constant->val_str);
            emit_comment(f, 1, constant->comment);
        }
        if (g_ptr_array_size(structure->constants) > 0)
            emit(0, "");

        emit_python_init(lcm, f, structure);
        emit_python_encode(lcm, f, structure);
        emit_python_encode_one(lcm, f, structure);
        emit_python_decode(lcm, f, structure);
        emit_python_decode_one(lcm, f, structure);
        emit_python_fingerprint(lcm, f, structure);
        fclose(f);
    }

    if (init_py_fp)
        fclose(init_py_fp);
    g_hash_table_destroy(init_py_imports);
    return 0;
}

int emit_python(lcmgen_t *lcm)
{
    GHashTable *package_table = g_hash_table_new_full(g_str_hash, g_str_equal, NULL,
                                                      (GDestroyNotify) _package_contents_free);

    // group the enums and structs by package
    for (unsigned int i = 0; i < lcm->enums->len; i++) {
        lcm_enum_t *enumeration = (lcm_enum_t *) g_ptr_array_index(lcm->enums, i);
        _package_contents_t *package = (_package_contents_t *) g_hash_table_lookup(
            package_table, enumeration->enumname->package);
        if (!package) {
            package = _package_contents_new(enumeration->enumname->package);
            g_hash_table_insert(package_table, package->name, package);
        }
        g_ptr_array_add(package->enums, enumeration);
    }

    for (unsigned int i = 0; i < lcm->structs->len; i++) {
        lcm_struct_t *structure = (lcm_struct_t *) g_ptr_array_index(lcm->structs, i);
        _package_contents_t *package = (_package_contents_t *) g_hash_table_lookup(
            package_table, structure->structname->package);
        if (!package) {
            package = _package_contents_new(structure->structname->package);
            g_hash_table_insert(package_table, package->name, package);
        }
        g_ptr_array_add(package->structs, structure);
    }

    GPtrArray *vals = _hash_table_get_vals(package_table);

    for (int i = 0; i < vals->len; i++) {
        _package_contents_t *package = (_package_contents_t *) g_ptr_array_index(vals, i);
        int status = emit_package(lcm, package);
        if (0 != status)
            return status;
    }

    g_ptr_array_free(vals, TRUE);

    g_hash_table_destroy(package_table);
    return 0;
}
