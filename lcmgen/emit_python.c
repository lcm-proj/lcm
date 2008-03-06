#include <stdio.h>
#include <stdint.h>
#include <assert.h>
#include <string.h>
#include <stdlib.h>
#include <ctype.h>
#include <inttypes.h>

#include <glib.h>

#include "lcmgen.h"

#define INDENT(n) (4*(n))

#define emit_start(n, ...) do { fprintf(f, "%*s", INDENT(n), ""); fprintf(f, __VA_ARGS__); } while (0)
#define emit_continue(...) do { fprintf(f, __VA_ARGS__); } while (0)
#define emit_end(...) do { fprintf(f, __VA_ARGS__); fprintf(f, "\n"); } while (0)
#define emit(n, ...) do { fprintf(f, "%*s", INDENT(n), ""); fprintf(f, __VA_ARGS__); fprintf(f, "\n"); } while (0)

#define err(...) fprintf (stderr, __VA_ARGS__)

static void
get_all_vals_helper (gpointer key, gpointer value, gpointer user_data)
{
    GPtrArray *vals = (GPtrArray*) user_data;

    g_ptr_array_add(vals, value);    
}

GPtrArray * _hash_table_get_vals (GHashTable *hash_table)
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
is_type_enum (const char *name, GPtrArray *enums)
{
    for (unsigned int en = 0; en < enums->len; en++) {
        lcm_enum_t *le = g_ptr_array_index(enums, en);
        if (! strcmp(le->enumname->typename, name)) return 1;
    }
    return 0;
}

static int
is_same_type (const lcm_typename_t *tn1, const lcm_typename_t *tn2) {
    return ! strcmp (tn1->typename, tn2->typename);
}

static int
is_same_package (const lcm_typename_t *tn1, const lcm_typename_t *tn2) {
    return ! strcmp (tn1->package, tn2->package);
}

static const char *
nil_initializer_string(const lcm_typename_t *type)
{
    if (!strcmp(type->typename, "byte")) return "0";
    if (!strcmp(type->typename, "boolean")) return "False";
    if (!strcmp(type->typename, "int8_t")) return "0";
    if (!strcmp(type->typename, "int16_t")) return "0";
    if (!strcmp(type->typename, "int32_t")) return "0";
    if (!strcmp(type->typename, "int64_t")) return "0";
    if (!strcmp(type->typename, "float")) return "0";
    if (!strcmp(type->typename, "double")) return "0";
    if (!strcmp(type->typename, "string")) return "\"\"";
    else return "None";
}

static char
_struct_format (const lcmgen_t *lcm, lcm_member_t *lm) 
{
    const char *tn = lm->type->typename;
    if (!strcmp ("byte", tn)) return 'B';
    if (!strcmp ("boolean", tn)) return 'b';
    if (!strcmp ("int8_t", tn)) return 'b';
    if (!strcmp ("int16_t", tn)) return 'h';
    if (!strcmp ("int32_t", tn)) return 'i';
    if (!strcmp ("int64_t", tn)) return 'q';
    if (!strcmp ("float", tn)) return 'f';
    if (!strcmp ("double", tn)) return 'd';
    if (is_type_enum (tn, lcm->enums)) return 'i';
    return 0;
}

static int
_primitive_type_size (const lcmgen_t *lcm, const char *tn)
{
    if (!strcmp ("byte", tn)) return 1;
    if (!strcmp ("boolean", tn)) return 1;
    if (!strcmp ("int8_t", tn)) return 1;
    if (!strcmp ("int16_t", tn)) return 2;
    if (!strcmp ("int32_t", tn)) return 4;
    if (!strcmp ("int64_t", tn)) return 8;
    if (!strcmp ("float", tn)) return 4;
    if (!strcmp ("double", tn)) return 8;
    if (is_type_enum (tn, lcm->enums)) return 4;
    assert (0);
}

static void
_emit_decode_one (const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls, 
        lcm_member_t *lm, const char *accessor, int indent, const char *sfx)
{
    const char *tn = lm->type->typename;
    const char *mn = lm->membername;
    const char *sn = lm->type->shortname;
    if (!strcmp ("string", tn)) {
        emit (indent, "__%s_len = struct.unpack('>I', buf.read(4))[0]", mn);
        emit (indent, "%sbuf.read(__%s_len)[:-1].decode('utf-8')%s",
                accessor, mn, sfx);
    } else if (!strcmp ("byte", tn)) {
        emit (indent, "%sstruct.unpack('B', buf.read(1))[0]%s", accessor, sfx);
    } else if (!strcmp ("int8_t", tn) || !(strcmp ("boolean", tn))) {
        emit (indent, "%sstruct.unpack('b', buf.read(1))[0]%s", accessor, sfx);
    } else if (!strcmp ("int16_t", tn)) {
        emit (indent, "%sstruct.unpack('>h', buf.read(2))[0]%s", accessor, sfx);
    } else if (!strcmp ("int32_t", tn) || 
            is_type_enum (lm->type->typename, lcm->enums)) {
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
            emit (indent, "%s%s.%s._decode_one(buf)%s", accessor, tn, sn, sfx);
        }
    }
}

static void
_flush_read_struct_fmt (const lcmgen_t *lcm, FILE *f, 
        GQueue *formats, GQueue *members)
{
    assert (g_queue_get_length (formats) == g_queue_get_length (members));
    if (g_queue_is_empty (formats)) return;

    fprintf (f, "        ");
    int fmtsize = 0;
    while (! g_queue_is_empty (members)) {
        lcm_member_t *lm = (lcm_member_t*) g_queue_pop_head (members);
        emit_continue ("self.%s", lm->membername);
        if (! g_queue_is_empty (members)) {
            emit_continue (", ");
        }
        fmtsize += _primitive_type_size (lcm, lm->type->typename);
    }
    emit_continue (" = struct.unpack(\">");
    while (! g_queue_is_empty (formats)) {
        emit_continue ("%c", GPOINTER_TO_INT (g_queue_pop_head (formats)));
    }
    emit_end ("\", buf.read(%d))", fmtsize);
}

static void
emit_python_decode_one (const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls)
{
    emit(1, "def _decode_one(buf):");
    emit (2, "self = %s()\n", ls->structname->shortname);

    GQueue *struct_fmt = g_queue_new ();
    GQueue *struct_members = g_queue_new ();

    for (unsigned int m = 0; m < g_ptr_array_size(ls->members); m++) {
        lcm_member_t *lm = g_ptr_array_index(ls->members, m);
        char fmt = _struct_format (lcm, lm);

        if (! lm->dimensions->len) {
            if (0 && fmt) {
                g_queue_push_tail (struct_fmt, GINT_TO_POINTER ((int)fmt));
                g_queue_push_tail (struct_members, lm);
            } else {
                _flush_read_struct_fmt (lcm, f, struct_fmt, struct_members);
                char accessor[strlen(lm->membername) + strlen("self.") + 4];
                snprintf (accessor, sizeof (accessor), "self.%s = ", 
                        lm->membername);
                _emit_decode_one (lcm, f, ls, lm, accessor, 2, "");
            }
        } else {
            _flush_read_struct_fmt (lcm, f, struct_fmt, struct_members);

            GString *accessor = g_string_new ("");
            g_string_append_printf (accessor, "self.%s", lm->membername);

            if (lm->dimensions->len != 1 || strcmp("byte", lm->type->typename)){
                emit (2, "%s = []", accessor->str);
            }

            int wrotebytes = 0;
            unsigned int n;
            for (n=0; n<lm->dimensions->len; n++) {
                lcm_dimension_t *dim = 
                    (lcm_dimension_t*) g_ptr_array_index (lm->dimensions, n);

                if (n == lm->dimensions->len-1 && 
                    !strcmp (lm->type->typename, "byte")) {
                    emit (2+n, "%s = buf.read(%s%s)", 
                            accessor->str, 
                            (dim->mode==LCM_CONST?"":"self."),
                            dim->size);
                    wrotebytes = 1;
                    break;
                }

                if (dim->mode == LCM_CONST) {
                    emit (2+n, "for i%d in range(%s):", n, dim->size);
                } else {
                    emit (2+n, "for i%d in range(self.%s):", n, dim->size);
                }

                if (n < lm->dimensions->len-1) {
                    emit (3+n, "%s.append ([])", accessor->str);
                    g_string_append_printf (accessor, "[i%d]", n);
                }
            }

            if (!wrotebytes) {
                g_string_append_printf (accessor, ".append(");
                _emit_decode_one (lcm, f, ls, lm, accessor->str, n+2, ")");
            }
            g_string_free (accessor, TRUE);
        }
    }
    _flush_read_struct_fmt (lcm, f, struct_fmt, struct_members);
    emit (2, "return self");

    g_queue_free (struct_fmt);
    g_queue_free (struct_members);
    emit (1, "_decode_one = staticmethod (_decode_one)");
    fprintf (f, "\n");
}

static void
emit_python_decode (const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls)
{
    emit (1, "def decode(data):");
    emit (2, "if hasattr (data, 'read'):");
    emit (3,     "buf = data");
    emit (2, "else:");
    emit (3,     "buf = StringIO.StringIO(data)");
    emit (2, "if buf.read(8) != %s._get_packed_fingerprint():", 
            ls->structname->shortname);
    emit (3,     "raise ValueError(\"Decode error\")");
    emit (2, "return %s._decode_one (buf)", ls->structname->shortname);
    emit (1, "decode = staticmethod (decode)");
    fprintf (f, "\n");
}

static void
_emit_encode_one (const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls, 
        lcm_member_t *lm, const char *accessor, int indent)
{
    const char *tn = lm->type->typename;
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
    } else if (!strcmp ("int32_t", tn) || 
            is_type_enum (lm->type->typename, lcm->enums)) {
        emit (indent, "buf.write(struct.pack('>i', %s))", accessor);
    } else if (!strcmp ("int64_t", tn)) {
        emit (indent, "buf.write(struct.pack('>q', %s))", accessor);
    } else if (!strcmp ("float", tn)) {
        emit (indent, "buf.write(struct.pack('>f', %s))", accessor);
    } else if (!strcmp ("double", tn)) {
        emit (indent, "buf.write(struct.pack('>d', %s))", accessor);
    } else {
        emit (indent, "%s._encode_one(buf)", accessor);
    }
}

static void
_flush_write_struct_fmt (FILE *f, GQueue *formats, GQueue *members)
{
    assert (g_queue_get_length (formats) == g_queue_get_length (members));
    if (g_queue_is_empty (formats)) return;
    emit_start (2, "buf.write (struct.pack(\">");
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

    GQueue *struct_fmt = g_queue_new ();
    GQueue *struct_members = g_queue_new ();

    for (unsigned int m = 0; m < g_ptr_array_size(ls->members); m++) {
        lcm_member_t *lm = g_ptr_array_index(ls->members, m);
        char fmt = _struct_format (lcm, lm);

        if (! lm->dimensions->len) {
            if (fmt) {
                g_queue_push_tail (struct_fmt, GINT_TO_POINTER ((int)fmt));
                g_queue_push_tail (struct_members, lm);
            } else {
                _flush_write_struct_fmt (f, struct_fmt, struct_members);
                char accessor[strlen(lm->membername) + strlen("self.") + 1];
                snprintf (accessor, sizeof (accessor), "self.%s", 
                        lm->membername);
                _emit_encode_one (lcm, f, ls, lm, accessor, 2);
            }
        } else {
            _flush_write_struct_fmt (f, struct_fmt, struct_members);
            GString *accessor = g_string_new ("");
            g_string_append_printf (accessor, "self.%s", lm->membername);

            unsigned int n;
            int wrotebytes = 0;
            for (n=0; n<lm->dimensions->len; n++) {
                lcm_dimension_t *dim = 
                    (lcm_dimension_t*) g_ptr_array_index (lm->dimensions, n);

                if (n == lm->dimensions->len-1 && 
                    !strcmp (lm->type->typename, "byte")) {
                    emit (2+n, "buf.write(%s[:%s%s])", 
                            accessor->str, (dim->mode==LCM_CONST?"":"self."),
                            dim->size);
                    wrotebytes = 1;
                    break;
                }

                g_string_append_printf (accessor, "[i%d]", n);
                if (dim->mode == LCM_CONST) {
                    emit (2+n, "for i%d in range(%s):", n, dim->size);
                } else {
                    emit (2+n, "for i%d in range(self.%s):", n, dim->size);
                }
            }

            if (!wrotebytes) {
                _emit_encode_one (lcm, f, ls, lm, accessor->str, n+2);
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
emit_python_init (const lcmgen_t *lcm, FILE *f, lcm_struct_t *lr)
{
    fprintf(f, "    def __init__(self):\n");
    unsigned int member;
    for (member = 0; member < lr->members->len; member++) {
        lcm_member_t *lm = g_ptr_array_index(lr->members, member);
        fprintf(f, "        self.%s = ", lm->membername);

        if( 0 == lm->dimensions->len ) {
            if (is_type_enum(lm->type->typename, lcm->enums) ) {
                fprintf(f, "0\n");
            } else {
                fprintf(f, "%s\n", nil_initializer_string(lm->type));
            }
        } else {
            fprintf(f, "[]\n");
        }
    }
    if (0 == member) { fprintf(f, "        pass\n"); }
    fprintf(f, "\n");
}

static void
emit_python_fingerprint (const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls)
{
    const char *sn = ls->structname->shortname;
    emit (1, "_hash = None");

    emit (1, "def _get_hash_recursive (parents):");
    emit (2,     "if %s in parents: return 0", sn);
    emit (2,     "newparents = parents + [%s]", sn);
    emit_start (2, "tmphash = (0x%"PRIx64, ls->hash);
    for (unsigned int m = 0; m < ls->members->len; m++) {
        lcm_member_t *lm = g_ptr_array_index(ls->members, m);
        const char *msn = lm->type->shortname;
        if (! lcm_is_primitive_type (lm->type->typename)) {
            const char *ghr = "_get_hash_recursive(newparents)";
            if (is_same_type (lm->type, ls->structname)) {
                emit_continue ("+ %s.%s", msn, ghr);
            } else if (is_same_package (lm->type, ls->structname)) {
                emit_continue ("+ %s.%s.%s", msn, msn, ghr);
            } else {
                emit_continue ("+ %s.%s.%s", lm->type->typename, msn, ghr);
            }
        }
    }
    emit_end (") & 0xffffffffffffffff");
    emit (2, "tmphash  = (((tmphash<<1)&0xffffffffffffffff)  + "
            "(tmphash>>63)) & 0xffffffffffffffff ");
    emit (2,     "return tmphash");
    emit (1, "_get_hash_recursive=staticmethod(_get_hash_recursive)");

    emit (1, "_packed_fingerprint = None");
    emit (1, "");
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
        lcm_member_t *lm = g_ptr_array_index (ls->members, m);
        if (! lcm_is_primitive_type (lm->type->typename)) {
            if (strlen (lm->type->package) && 
                ! is_same_package (ls->structname, lm->type)) {
                if (! g_hash_table_lookup (dependencies, lm->type->typename)) {
                    g_hash_table_insert (dependencies, lm->type->typename, 
                            lm->type->typename);
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
        const char *package = g_ptr_array_index (deps, i);
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
    _package_contents_t *pc = malloc (sizeof(_package_contents_t));
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
    char *pdname = g_build_filenamev (dirs);
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
            g_mkdir_with_parents (package_dir, 0755);
        }
        if (!g_file_test (package_dir, G_FILE_TEST_IS_DIR)) {
            err ("Could not create directory %s\n", package_dir);
            return -1;
        }
    }

    // write the package __init__.py files, if necessary
    if (have_package) {
        int ndirs = 0;
        for (ndirs=0; dirs[ndirs]; ndirs++);

        for (int i=0 ; i<ndirs; i++) {
            char *initpy_fname_parts[ndirs + 4];
            initpy_fname_parts[0] = package_dir_prefix;
            for (int j=0; j<=i; j++) {
                initpy_fname_parts[j+1] = dirs[j];
            }
            initpy_fname_parts[i+2] = "__init__.py";
            initpy_fname_parts[i+3] = NULL;

            char *initpy_fname = g_build_filenamev (initpy_fname_parts);
            if (! g_file_test (initpy_fname, G_FILE_TEST_EXISTS)) {
                FILE *initpy_fp = fopen (initpy_fname, "w");
                if (!initpy_fp) {
                    free (initpy_fname);
                    return -1;
                }

                fprintf (initpy_fp, "\"\"\"LCM package __init__.py file\n"
                        "This file automatically generated by lcm.\n"
                        "\"\"\"\n\n");
                fclose (initpy_fp);
            }
            free (initpy_fname);
        }
    }
    g_strfreev (dirs);

    ////////////////////////////////////////////////////////////
    // ENUMS
    for (int i=0; i<pc->enums->len; i++) {
        lcm_enum_t *le = g_ptr_array_index (pc->enums, i);

        char path[PATH_MAX];
        sprintf (path, "%s%s.py", package_dir, le->enumname->shortname);

        FILE *f = fopen(path, "w");
        if (f==NULL) return -1;

        fprintf(f, "\"\"\"LCM type definitions\n"
                "This file automatically generated by lcm.\n"
                "DO NOT MODIFY BY HAND!!!!\n"
                "\"\"\"\n"
                "\n"
                "import struct\n");

        // enums always encoded as int32
        emit (0, "class %s:", le->enumname->shortname);
        for (unsigned int v = 0; v < le->values->len; v++) {
            lcm_enum_value_t *lev = g_ptr_array_index(le->values, v);
            emit(1, "%s = %i", lev->valuename, lev->value);
        }

        emit (1, "_hash = 0x%"PRIx64, le->hash);
        emit (1, "def _get_hash_recursive(parents):");
        emit (2,     "return %s._hash", le->enumname->shortname);
        emit (1, "_get_hash_recursive=staticmethod(_get_hash_recursive)");
        emit (1, "def _get_packed_fingerprint():");
        emit (2,     "return struct.pack(\">Q\", %s._hash)", le->enumname->shortname);
        emit (1, "_get_packed_fingerprint = staticmethod(_get_packed_fingerprint)");
        fprintf (f, "\n");
        fclose (f);
    }

    ////////////////////////////////////////////////////////////
    // STRUCTS
    for (int i = 0; i<pc->structs->len; i++) {
        lcm_struct_t *ls = g_ptr_array_index(pc->structs, i);

        char path[PATH_MAX];
        sprintf (path, "%s%s.py", package_dir, ls->structname->shortname);

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

        fprintf(f, "class %s:\n", ls->structname->shortname);
        fprintf (f,"    __slots__ = (");
        for (unsigned int member = 0; member < ls->members->len; member++) {
            lcm_member_t *lm = g_ptr_array_index (ls->members, member);
            fprintf (f, "\"%s\"%s", lm->membername, 
                    member < ls->members->len-1 ? ", " : "");
        }
        fprintf (f, ")\n\n");

        emit_python_init (lcm, f, ls);
        emit_python_encode (lcm, f, ls);
        emit_python_encode_one (lcm, f, ls);
        emit_python_decode (lcm, f, ls);
        emit_python_decode_one (lcm, f, ls);
        emit_python_fingerprint (lcm, f, ls);
        fclose (f);
    }
    return 0;
}

int emit_python(lcmgen_t *lcm)
{
    GHashTable *packages = g_hash_table_new_full (g_str_hash, 
            g_str_equal, NULL, (GDestroyNotify)_package_contents_free);

    // group the enums and structs by package
    for (unsigned int i = 0; i < lcm->enums->len; i++) {
        lcm_enum_t *le = g_ptr_array_index(lcm->enums, i);
        _package_contents_t *pc = g_hash_table_lookup (packages, 
                le->enumname->package);
        if (!pc) {
            pc = _package_contents_new (le->enumname->package);
            g_hash_table_insert (packages, pc->name, pc);
        }
        g_ptr_array_add (pc->enums, le);
    }

    for (unsigned int i = 0; i < lcm->structs->len; i++) {
        lcm_struct_t *ls = g_ptr_array_index(lcm->structs, i);
        _package_contents_t *pc = g_hash_table_lookup (packages, 
                ls->structname->package);
        if (!pc) {
            pc = _package_contents_new (ls->structname->package);
            g_hash_table_insert (packages, pc->name, pc);
        }
        g_ptr_array_add (pc->structs, ls);
    }

    GPtrArray *vals = _hash_table_get_vals (packages);

    for (int i=0; i<vals->len; i++) {
        _package_contents_t *pc = g_ptr_array_index (vals, i);
        int status = emit_package (lcm, pc); 
        if (0 != status) return status;
    }

    g_ptr_array_free (vals, TRUE);

    g_hash_table_destroy (packages);
    return 0;

}
