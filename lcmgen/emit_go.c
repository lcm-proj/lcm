#include <assert.h>
#include <ctype.h>
#include <errno.h>
#include <inttypes.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#ifdef WIN32
#define __STDC_FORMAT_MACROS  // Enable integer types
#endif

#include "lcmgen.h"

#define TABS "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"
#define emit_start(n, ...)           \
    do {                             \
        fprintf(f, "%.*s", n, TABS); \
        fprintf(f, __VA_ARGS__);     \
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
#define emit(n, ...)                 \
    do {                             \
        fprintf(f, "%.*s", n, TABS); \
        fprintf(f, __VA_ARGS__);     \
        fprintf(f, "\n");            \
    } while (0)
#define emit_nl()         \
    do {                  \
        fprintf(f, "\n"); \
    } while (0)

/*
 * Simple function that takes a string like this.is.a.string and turns it into
 * this_is_a_string.
 *
 * CAUTION: memory has to be freed manually.
 */
static char *dots_to_underscores(const char *const s)
{
    char *p = strdup(s);

    for (char *t = p; *t != 0; t++)
        if (*t == '.')
            *t = '_';

    return p;
}

/*
 * Simple function that takes a string like this.is.a.string and turns it into
 * this/is/a/string.
 *
 * CAUTION: memory has to be freed manually.
 */
static char *dots_to_path(const char *const s)
{
    char *p = strdup(s);

    for (char *t = p; *t != 0; t++)
        if (*t == '.')
            *t = G_DIR_SEPARATOR;

    return p;
}

/*
 * Simple function that takes a string like this.is.a.string and turns it into
 * string.
 *
 * CAUTION: memory has to be freed manually.
 */
static char *strip_dots(const char *const s)
{
    char *p = (char *) s;

    for (char *t = p; *t != 0; t++)
        if (*t == '.')
            p = t + 1;

    return strdup(p);
}

/*
 * Simple function that takes a string like this_is_a_string and turns it into
 * This_is_a_string.
 *
 * CAUTION: memory has to be freed manually.
 */
static char *first_to_upper(const char *const str)
{
    char *s = strdup(str);
    s[0] = toupper(s[0]);
    return s;
}

/*
 * Simple function that takes a string like this_is.a_string and turns it into
 * ThisIsAString. To follow Golang naming conventions.
 *
 * CAUTION: memory has to be freed manually.
 */
static char *go_name(const char *const str)
{
    char *ret = malloc(strlen(str) + 1);

    char *t = ret;
    int upper = FALSE;
    for (const char *p = str; *p != 0; p++) {
        switch (*p) {
        case '.':
            upper = TRUE;
            break;
        case '_':
            upper = TRUE;
            break;
        default:
            if (upper) {
                *t = toupper(*p);
                upper = FALSE;
            } else {
                *t = *p;
            }
            t++;
            break;
        }
    }
    *t = '\0';

    return ret;
}

/*
 * Returns the Golang corresponding type of an LCM type.
 */
static const char *const map_builtintype_name(const char *const type)
{
    if (strcmp(type, "boolean") == 0)
        return "bool";
    if (strcmp(type, "byte") == 0)
        return "byte";
    if (strcmp(type, "int8_t") == 0)
        return "int8";
    if (strcmp(type, "int16_t") == 0)
        return "int16";
    if (strcmp(type, "int32_t") == 0)
        return "int32";
    if (strcmp(type, "int64_t") == 0)
        return "int64";
    if (strcmp(type, "float") == 0)
        return "float32";
    if (strcmp(type, "double") == 0)
        return "float64";
    if (strcmp(type, "string") == 0)
        return "string";
    assert(0);
    return NULL;
}

/*
 * Returns the size of a primitive type (string excluded) in bytes.
 */
static int primitive_type_size(const char *const type)
{
    if (strcmp(type, "byte") == 0)
        return 1;
    if (strcmp(type, "boolean") == 0)
        return 1;
    if (strcmp(type, "int8_t") == 0)
        return 1;
    if (strcmp(type, "int16_t") == 0)
        return 2;
    if (strcmp(type, "int32_t") == 0)
        return 4;
    if (strcmp(type, "int64_t") == 0)
        return 8;
    if (strcmp(type, "float") == 0)
        return 4;
    if (strcmp(type, "double") == 0)
        return 8;
    assert(0);
    return 0;
}

/*
 * Returns the index of first member in which name is used as a variable
 * dimension or members->len if not found.
 * Starts at given index.
 */
unsigned int lcm_find_member_with_named_dimension(lcm_struct_t *ls, const char *const name,
                                                  unsigned int start)
{
    for (unsigned int i = start; i < ls->members->len; i++) {
        lcm_member_t *lm = (lcm_member_t *) g_ptr_array_index(ls->members, i);
        for (unsigned int j = 0; j < lm->dimensions->len; j++) {
            lcm_dimension_t *dim = (lcm_dimension_t *) g_ptr_array_index(lm->dimensions, j);
            if (dim->mode == LCM_VAR) {
                if (strcmp(name, dim->size) == 0) {
                    return i;
                }
            }
        }
    }
    return ls->members->len;
}

/*
 * Locate the lcm_struct_t of given lcm_member_t from lcmgen_t
 */
lcm_struct_t *lcm_find_struct(lcmgen_t *lcm, lcm_member_t *lm)
{
    int found = FALSE;
    lcm_struct_t *ls = NULL;

    for (unsigned int s = 0; s < lcm->structs->len; s++) {
        ls = (lcm_struct_t *) g_ptr_array_index(lcm->structs, s);

        if (strcmp(lm->type->lctypename, ls->structname->lctypename) == 0) {
            found = TRUE;
            break;
        }
    }

    if (!found) {
        return NULL;
    }

    return ls;
}

/*
 * Calculates the fingerprint during code generation.
 *
 * Algorithm as described in:
 *     https://lcm-proj.github.io/type_specification.html
 *
 * Returns calculated fingerprint or 0 on error
 */
struct __fingerprints {
    const struct __fingerprints *parent;
    uint64_t fingerprint;
};

uint64_t __lcm_recursive_fingerprint(lcmgen_t *lcm, lcm_struct_t *ls,
                                     const struct __fingerprints *fs)
{
    uint64_t fingerprint = ls->hash;

    // Check if we are present in list of hashes, bail out if its the case
    for (const struct __fingerprints *fs_ = fs; fs_ != NULL; fs_ = fs_->parent) {
        if (fs_->fingerprint == fingerprint) {
            return 0;
        }
    }
    struct __fingerprints fp;
    fp.parent = fs;
    fp.fingerprint = fingerprint;

    // Compute hash for all members
    for (unsigned int m = 0; m < ls->members->len; m++) {
        lcm_member_t *lm = (lcm_member_t *) g_ptr_array_index(ls->members, m);

        if (!lcm_is_primitive_type(lm->type->lctypename)) {
            lcm_struct_t *ls_ = lcm_find_struct(lcm, lm);

            if (ls_ != NULL) {
                fingerprint += __lcm_recursive_fingerprint(lcm, ls_, &fp);
            } else {
                fprintf(stderr, "Unable to locate fingerprint for member '%s' of '%s'\n",
                        lm->membername, ls->structname->shortname);
                return 0;
            }
        }
    }

    return (fingerprint << 1) + (fingerprint >> 63);
}
uint64_t lcm_get_fingerprint(lcmgen_t *lcm, lcm_struct_t *ls)
{
    return __lcm_recursive_fingerprint(lcm, ls, NULL);
}

/*
 * Returns the index of the first dimension named name or dimensions->len if
 * not found.
 * Starts at given index.
 */
unsigned int lcm_find_named_dimension(FILE *f, lcm_struct_t *ls, lcm_member_t *lm,
                                      const char *const name, unsigned int start)
{
    for (unsigned int i = start; i < lm->dimensions->len; i++) {
        lcm_dimension_t *dim = (lcm_dimension_t *) g_ptr_array_index(lm->dimensions, i);
        if (dim->mode == LCM_VAR) {
            if (strcmp(name, dim->size) == 0) {
                return i;
            }
        }
    }
    return lm->dimensions->len;
}

/*
 * Takes a typical LCM membername like rapid.test and turns it into something
 * usable in go.
 *
 * CAUTION: memory has to be freed manually.
 */
static char *go_membername(lcm_struct_t *ls, const char *const str, int method)
{
    char *membername = go_name(str);

    if (lcm_find_member_with_named_dimension(ls, str, 0) >= ls->members->len) {
        // If not a read-only attribute, uppercase it to export it.
        membername[0] = toupper(membername[0]);
    } else if (method) {
        // If read-only should be method invocation.
        size_t len = strlen(membername);
        membername = realloc(membername, len + 3);
        membername[0] = toupper(membername[0]);
        membername[len++] = '(';
        membername[len++] = ')';
        membername[len++] = '\0';
    }

    return membername;
}

/*
 * More sofisticated than map_builtintype_name(const char *). If it cannot find
 * a corresponding builtin type, this function assumes that we are dealing with
 * nested types. By doing so, it uses string manipulation to make the types
 * work with Golang.
 *
 * Fingerprint suffix is added if fingerprint is != 0
 *
 * CAUTION: memory has to be freed manually.
 */
static char *go_typename(const char *const package, const char *const typepackage,
                         const char *const type, const int64_t fingerprint)
{
    if (lcm_is_primitive_type(type)) {
        return strdup(map_builtintype_name(type));
    }

    // In case none of the above
    GString *name = g_string_new("");

    char *gotype = go_name(type);

    // Uppercase first character to make it public
    gotype[0] = toupper(gotype[0]);

    if (strcmp(package, typepackage) != 0) {
        char *pkg = strip_dots(typepackage);
        g_string_printf(name, "%s.%s", pkg, gotype);
        free(pkg);
    } else {
        name = g_string_append(name, gotype);
    }
    free(gotype);

    // Add fingerprint suffix
    if (fingerprint != 0) {
        g_string_append_printf(name, "_%lx", (uint64_t) fingerprint);
    }

    char *ret = name->str;
    g_string_free(name, FALSE);
    return ret;
}

/*
 * Returns the go filename
 *
 * CAUTION: memory has to be freed manually.
 */
char *go_filename(lcmgen_t *lcm, const char *const dir, const char *const name,
                  uint64_t fingerprint, const char *const suffix)
{
    if (fingerprint != 0)
        return g_strdup_printf("%s/%s_%lx%s.go", dir, name, fingerprint, suffix);
    else
        return g_strdup_printf("%s/%s%s.go", dir, name, suffix);
}

/*
 * Emits a LCM comment.
 */
static void emit_comment(FILE *f, int indent, const char *const comment)
{
    if (!comment)
        return;

    gchar **lines = g_strsplit(comment, "\n", 0);
    for (int line = 0; lines[line]; line++) {
        emit(indent, "// %s", lines[line]);
    }

    g_strfreev(lines);
}

static void emit_auto_generated_warning(FILE *f)
{
    fprintf(f,
            "// THIS IS AN AUTOMATICALLY GENERATED FILE.  DO NOT MODIFY\n"
            "// BY HAND!!\n"
            "//\n"
            "// Generated by lcm-gen\n\n");
}

/*
 * Emits the decoding function for primitive types.
 */
static void emit_encode_function(FILE *f, const char *const type, const char *const src, int indent)
{
    if (!strcmp(type, "boolean")) {
        emit(indent, "if p.%s {", src);
        emit(indent + 1, "data[offset] = 1");
        emit(indent, "} else {");
        emit(indent + 1, "data[offset] = 0");
        emit(indent, "}");
        emit(indent, "offset += 1");
    } else if (!strcmp(type, "byte")) {
        emit(indent, "data[offset] = p.%s", src);
        emit(indent, "offset += 1");
    } else if (!strcmp(type, "int8_t")) {
        emit(indent, "data[offset] = byte(p.%s)", src);
        emit(indent, "offset += 1");
    } else if (!strcmp(type, "int16_t")) {
        emit(indent, "binary.BigEndian.PutUint16(data[offset:],");
        emit(indent + 1, "uint16(p.%s))", src);
        emit(indent, "offset += 2");
    } else if (!strcmp(type, "int32_t")) {
        emit(indent, "binary.BigEndian.PutUint32(data[offset:],");
        emit(indent + 1, "uint32(p.%s))", src);
        emit(indent, "offset += 4");
    } else if (!strcmp(type, "int64_t")) {
        emit(indent, "binary.BigEndian.PutUint64(data[offset:],");
        emit(indent + 1, "uint64(p.%s))", src);
        emit(indent, "offset += 8");
    } else if (!strcmp(type, "float")) {
        emit(indent, "binary.BigEndian.PutUint32(data[offset:],");
        emit(indent + 1, "math.Float32bits(p.%s))", src);
        emit(indent, "offset += 4");
    } else if (!strcmp(type, "double")) {
        emit(indent, "binary.BigEndian.PutUint64(data[offset:],");
        emit(indent + 1, "math.Float64bits(p.%s))", src);
        emit(indent, "offset += 8");
    } else if (!strcmp(type, "string")) {
        emit(indent, "{");
        emit(indent + 1, "bstr := []byte(p.%s)", src);
        emit(indent + 1, "binary.BigEndian.PutUint32(data[offset:],");
        emit(indent + 2, "uint32(len(bstr))+1)");
        emit(indent + 1, "offset += 4");
        emit(indent + 1, "offset += copy(data[offset:], bstr)");
        emit(indent + 1, "data[offset] = 0");
        emit(indent + 1, "offset += 1");
        emit(indent, "}");
    } else {
        assert(0);
    }
}

/*
 * Emits the encoding function for primitive types.
 */
static void emit_decode_function(FILE *f, const char *const type, const char *const dst, int indent)
{
    if (!strcmp(type, "boolean")) {
        emit(indent, "if data[offset] != 0 {");
        emit(indent + 1, "p.%s = true", dst);
        emit(indent, "} else {");
        emit(indent + 1, "p.%s = false", dst);
        emit(indent, "}");
        emit(indent, "offset += 1");
    } else if (!strcmp(type, "byte")) {
        emit(indent, "p.%s = data[offset]", dst);
        emit(indent, "offset += 1");
    } else if (!strcmp(type, "int8_t")) {
        emit(indent, "p.%s = int8(data[offset])", dst);
        emit(indent, "offset += 1");
    } else if (!strcmp(type, "int16_t")) {
        emit(indent, "p.%s = int16(binary.BigEndian.Uint16(data[offset:]))", dst);
        emit(indent, "offset += 2");
    } else if (!strcmp(type, "int32_t")) {
        emit(indent, "p.%s = int32(binary.BigEndian.Uint32(data[offset:]))", dst);
        emit(indent, "offset += 4");
    } else if (!strcmp(type, "int64_t")) {
        emit(indent, "p.%s = int64(binary.BigEndian.Uint64(data[offset:]))", dst);
        emit(indent, "offset += 8");
    } else if (!strcmp(type, "float")) {
        emit(indent, "p.%s = math.Float32frombits(binary.BigEndian.Uint32(data[offset:]))", dst);
        emit(indent, "offset += 4");
    } else if (!strcmp(type, "double")) {
        emit(indent, "p.%s = math.Float64frombits(binary.BigEndian.Uint64(data[offset:]))", dst);
        emit(indent, "offset += 8");
    } else if (!strcmp(type, "string")) {
        emit(indent, "{");
        emit(indent + 1, "length := int(binary.BigEndian.Uint32(data[offset:]))");
        emit(indent + 1, "offset += 4");
        emit(indent + 1, "if length < 1 {");
        emit(indent + 2, "return fmt.Errorf(\"Decoded string length is negative\")");
        emit(indent + 1, "}");
        emit(indent + 1, "p.%s = string(data[offset : offset+length-1])", dst);
        emit(indent + 1, "offset += length");
        emit(indent, "}");
    }
}

/*
 * In order to fill slices, they have to be made first. This function emits
 * the necessary Go code when called. As more than one slice has to be created
 * when they are nested, this function takes start_dim as an argument.
 */
static void emit_go_slice_make(FILE *f, int indent, const char *const package, lcm_member_t *lm,
                               unsigned int start_dim, const char *const name,
                               const char *const size, const int64_t fingerprint)
{
    emit_start(indent, "p.%s = make(", name);

    for (unsigned int i = start_dim; i < lm->dimensions->len; i++) {
        lcm_dimension_t *dim = (lcm_dimension_t *) g_ptr_array_index(lm->dimensions, i);

        if (dim->mode == LCM_VAR) {
            emit_continue("[]");
        } else {
            emit_continue("[%s]", dim->size);
        }
    }

    char *type = go_typename(package, lm->type->package, lm->type->lctypename, fingerprint);
    emit_end("%s, p.%s)", type, size);
    free(type);
}

/*
 * Function to emit (nested) for loop(s), as this has to happen quite often in
 * case of multi-dimensional arrays.
 */
static unsigned int emit_go_array_loops(FILE *f, lcmgen_t *lcm, lcm_struct_t *ls, lcm_member_t *lm,
                                        GString *arraystr, int slice_emit, unsigned int end,
                                        const uint64_t fingerprint)
{
    unsigned int n;
    GString *slicestr = g_string_new(NULL);

    for (n = 0; n < end; n++) {
        lcm_dimension_t *dim = (lcm_dimension_t *) g_ptr_array_index(lm->dimensions, n);

        if (slice_emit)
            g_string_assign(slicestr, arraystr->str);

        g_string_append_printf(arraystr, "[i%d]", n);

        if (dim->mode == LCM_VAR) {
            char *size = go_membername(ls, dim->size, FALSE);
            const char *type =
                map_builtintype_name(lcm_find_member(ls, dim->size)->type->lctypename);

            if (slice_emit)
                emit_go_slice_make(f, n + 1, ls->structname->package, lm, n, slicestr->str, size,
                                   fingerprint);

            emit(1 + n, "for i%d := %s(0); i%d < p.%s; i%d++ {", n, type, n, size, n);

            free(size);
        } else {
            emit(1 + n, "for i%d := 0; i%d < %s; i%d++ {", n, n, dim->size, n);
        }
    }

    g_string_free(slicestr, TRUE);

    return n;
}

/*
 * Ends the loops created with emit_go_array_loops(...).
 */
static void emit_go_array_loops_end(FILE *f, unsigned int n)
{
    for (; n > 0; n--)
        emit(n, "}");
}

/*
 * Emits the header of the .go file.
 */
static void emit_go_header(FILE *f, const char *const gopackage)
{
    emit_auto_generated_warning(f);
    emit(0, "package %s", gopackage);
    emit_nl();
}

/*
 * Emits the import of required packages.
 */
static void emit_go_lcm_imports(FILE *f, lcmgen_t *lcm, lcm_struct_t *ls)
{
    emit(0, "import (");
    emit(1, "\"encoding/binary\"");
    emit(1, "\"fmt\"");
    emit(1, "\"math\"");
    emit(1, "\"math/bits\"");
    for (int i = 0; i < ls->members->len; i++) {
        lcm_member_t *lm = (lcm_member_t *) g_ptr_array_index(ls->members, i);

        if (lcm_is_primitive_type(lm->type->lctypename))
            continue;

        int imported = FALSE;
        for (int j = i - 1; j > 0; j--) {
            lcm_member_t *lm_ = (lcm_member_t *) g_ptr_array_index(ls->members, j);

            if (strcmp(lm->type->package, lm_->type->package) == 0)
                imported = TRUE;
        }

        // Import if not already imported and not our own package
        if (!imported && strcmp(ls->structname->package, lm->type->package) != 0) {
            char *package = dots_to_path(lm->type->package);
            char *prefix = getopt_get_string(lcm->gopt, "go-import-prefix");
            emit(1, "\"%s%s\"", prefix, package);
            free(package);
        }
    }
    emit(0, ")");
    emit_nl();

    // Silence (possible) not used import warning by go compiler
    emit(0, "const _ = math.Pi");
    emit(0, "const _ = bits.UintSize");
    emit_nl();
}

/*
 * Emits the fingerprint of this particular struct as a constant.
 */
static void emit_go_lcm_fingerprint_const(FILE *f, uint64_t hash, const char *const type)
{
    emit(0, "const %s_Fingerprint uint64 = 0x%016" PRIx64, type, hash);
    emit_nl();
}

/*
 * Emits the fingerprint function or constant if pre-calculated.
 */
static void emit_continue_go_fingerprint_string(FILE *f, lcmgen_t *lcm, const char *const type)
{
    emit_continue("%s_Fingerprint", type);
    if (!getopt_get_bool(lcm->gopt, "go-fingerprint")) {
        emit_continue("()");
    }
}

/*
 * Emits the const defintions.
 */
static void emit_go_lcm_const_definitions(FILE *f, lcmgen_t *lcm, lcm_struct_t *ls,
                                          const char *const gotype, const uint64_t fingerprint)
{
    if (!ls->constants->len) {
        return;
    }

    unsigned int mlen = 0, tlen = 0;
    for (unsigned int i = 0; i < ls->constants->len; i++) {
        lcm_constant_t *lc = (lcm_constant_t *) g_ptr_array_index(ls->constants, i);

        char *constname = go_membername(ls, lc->membername, FALSE);
        // Constants can only be in our own package, use "" to skip package
        // prefix
        char *consttype = go_typename("", "", lc->lctypename, fingerprint);

        if (strlen(constname) > mlen)
            mlen = strlen(constname);

        if (strlen(consttype) > tlen)
            tlen = strlen(consttype);

        free(consttype);
        free(constname);
    }

    emit(0, "// LCM constants");
    emit(0, "const (");
    for (unsigned int i = 0; i < ls->constants->len; i++) {
        lcm_constant_t *lc = (lcm_constant_t *) g_ptr_array_index(ls->constants, i);

        emit_comment(f, 1, lc->comment);

        char *constname = go_membername(ls, lc->membername, FALSE);
        // Constants can only be in our own package, use "" to skip package
        // prefix
        char *consttype = go_typename("", "", lc->lctypename, fingerprint);
        char *structtype = go_typename("", "", ls->structname->lctypename, fingerprint);

        int mspaces = mlen - strlen(constname) + 1;
        int tspaces = tlen - strlen(consttype) + 1;
        // Always prepend with typename otherwise we might have collisions
        // with other types constants

        emit(1, "%s_%s%*s%s%*s= %s", structtype, constname, mspaces, "", consttype, tspaces, "",
             lc->val_str);

        free(structtype);
        free(consttype);
        free(constname);
    }
    emit(0, ")");
    emit_nl();
}

/*
 * Emits the struct's defintion.
 */
static int emit_go_lcm_struct_definition(FILE *f, lcmgen_t *lcm, lcm_struct_t *ls,
                                         const char *const gotype, const uint64_t fingerprint)
{
    unsigned int max_member_len = 0;
    unsigned int max_type_len = 0;

    for (unsigned int i = 0; i < ls->members->len; i++) {
        lcm_member_t *lm = (lcm_member_t *) g_ptr_array_index(ls->members, i);

        char *membername = go_membername(ls, lm->membername, FALSE);
        if (strlen(membername) > max_member_len)
            max_member_len = strlen(membername);
        free(membername);

        char *membertype;
        if (lcm_is_primitive_type(lm->type->lctypename)) {
            membertype = go_typename("", "", lm->type->lctypename, 0);
        } else {
            uint64_t lm_fingerprint = 0;
            if (fingerprint != 0) {
                lcm_struct_t *ls_lm = lcm_find_struct(lcm, lm);
                if (ls_lm == NULL) {
                    fprintf(stderr, "Unable to locate %s\n", lm->membername);
                    return -1;
                }
                lm_fingerprint = lcm_get_fingerprint(lcm, ls_lm);
            }

            membertype = go_typename(ls->structname->package, lm->type->package,
                                     lm->type->lctypename, lm_fingerprint);
        }

        GString *arraystr = g_string_new(NULL);
        for (unsigned int d = 0; d < lm->dimensions->len; d++) {
            lcm_dimension_t *dim = (lcm_dimension_t *) g_ptr_array_index(lm->dimensions, d);
            if (dim->mode == LCM_CONST) {
                g_string_append_printf(arraystr, "[%s]", dim->size);
            } else {
                g_string_append(arraystr, "[]");
            }
        }

        int membertype_len = strlen(membertype) + arraystr->len;
        if (membertype_len > max_type_len)
            max_type_len = membertype_len;
        free(membertype);
        g_string_free(arraystr, TRUE);
    }

    emit_comment(f, 0, ls->comment);
    emit(0, "type %s struct {", gotype);

    for (unsigned int i = 0; i < ls->members->len; i++) {
        lcm_member_t *lm = (lcm_member_t *) g_ptr_array_index(ls->members, i);

        emit_comment(f, 1, lm->comment);

        char *membername = go_membername(ls, lm->membername, FALSE);
        char *membertype = NULL;

        if (lcm_is_primitive_type(lm->type->lctypename)) {
            membertype = go_typename("", "", lm->type->lctypename, 0);
        } else {
            uint64_t member_fp = 0;
            if (fingerprint != 0) {
                lcm_struct_t *ls_lm = lcm_find_struct(lcm, lm);
                if (ls_lm == NULL) {
                    fprintf(stderr, "Unable to locate %s\n", lm->membername);
                    return -1;
                }

                member_fp = lcm_get_fingerprint(lcm, ls_lm);
            }
            membertype = go_typename(ls->structname->package, lm->type->package,
                                     lm->type->lctypename, member_fp);
        }

        GString *arraystr = g_string_new(NULL);
        for (unsigned int d = 0; d < lm->dimensions->len; d++) {
            lcm_dimension_t *dim = (lcm_dimension_t *) g_ptr_array_index(lm->dimensions, d);
            if (dim->mode == LCM_CONST) {
                g_string_append_printf(arraystr, "[%s]", dim->size);
            } else {
                g_string_append(arraystr, "[]");
            }
        }

        GString *gotag = g_string_new(lm->membername);
        char *gotag_name = getopt_get_string(lcm->gopt, "go-tag-name");

        if (strlen(gotag_name) > 0) {
            g_string_printf(gotag, "%s:\"%s\"", gotag_name, lm->membername);
        }

        int type_spaces = max_member_len - strlen(membername) + 1;
        int tag_spaces = max_type_len - strlen(membertype) - arraystr->len + 1;
        emit(1, "%s%*s%s%s%*s`%s`", membername, type_spaces, "", arraystr->str, membertype,
             tag_spaces, "", gotag->str);

        free(membername);
        free(membertype);
        g_string_free(arraystr, TRUE);
        g_string_free(gotag, TRUE);
    }

    emit(0, "}");
    emit_nl();

    return 0;
}

/*
 * This function emits code that will create an exact copy of the struct.
 */
static void emit_go_lcm_deep_copy(FILE *f, lcmgen_t *lcm, lcm_struct_t *ls,
                                  const char *const gotype, const uint64_t fingerprint)
{
    emit(0, "// Copy creates a deep copy");
    emit(0, "// TODO: fix the fugly x and p names...");
    emit(0, "func (x *%s) Copy() (p %s) {", gotype, gotype);

    for (unsigned int m = 0; m < ls->members->len; m++) {
        lcm_member_t *lm = (lcm_member_t *) g_ptr_array_index(ls->members, m);

        char *membername = go_membername(ls, lm->membername, FALSE);

        if (lcm_is_primitive_type(lm->type->lctypename)) {
            if (!lm->dimensions->len) {
                emit(1, "p.%s = x.%s", membername, membername);
            } else {
                GString *arraystr = g_string_new(membername);
                unsigned int n = emit_go_array_loops(f, lcm, ls, lm, arraystr, TRUE,
                                                     lm->dimensions->len, fingerprint);
                emit(n + 1, "p.%s = x.%s", arraystr->str, arraystr->str);
                emit_go_array_loops_end(f, n);
                g_string_free(arraystr, TRUE);
            }
        } else {
            if (!lm->dimensions->len) {
                emit(1, "p.%s = x.%s.Copy()", membername, membername);
            } else {
                GString *arraystr = g_string_new(membername);
                unsigned int n = emit_go_array_loops(f, lcm, ls, lm, arraystr, TRUE,
                                                     lm->dimensions->len, fingerprint);
                emit(n + 1, "p.%s = x.%s.Copy()", arraystr->str, arraystr->str);
                emit_go_array_loops_end(f, n);
                g_string_free(arraystr, TRUE);
            }
        }

        emit_nl();
        free(membername);
    }

    emit(1, "return");
    emit(0, "}");
    emit_nl();
}

/*
 * Emits the main decode function.
 */
static void emit_go_lcm_encode(FILE *f, lcmgen_t *lcm, lcm_struct_t *ls, const char *const gotype)
{
    emit(0, "// Encode encodes a message (fingerprint & data) into binary form");
    emit(0, "//");
    emit(0, "// returns Encoded data or error");
    emit(0, "func (p *%s) Encode() (data []byte, err error) {", gotype);
    emit(1, "var size int");
    emit(1, "if size, err = p.Size(); err != nil {");
    emit(2, "return");
    emit(1, "}");
    emit_nl();
    emit(1, "data = make([]byte, 8+size)");
    emit_start(1, "binary.BigEndian.PutUint64(data, ");
    emit_continue_go_fingerprint_string(f, lcm, gotype);
    emit_end(")");
    emit_nl();
    emit(1, "var d []byte");
    emit(1, "if d, err = p.MarshalBinary(); err != nil {");
    emit(2, "return");
    emit(1, "}");
    emit_nl();
    emit(1, "if copied := copy(data[8:], d); copied != size {");
    emit(2, "return []byte{},");
    emit(3, "fmt.Errorf(\"Encoding error, buffer not filled (%%v != %%v)\", copied, size)");
    emit(1, "}");
    emit(1, "return");
    emit(0, "}");
    emit_nl();
}

/*
 * Emits getters for read-only struct members. That is all who are used as the
 * size definition for LCM dynamic arrays.
 */
static void emit_go_lcm_read_only_getters(FILE *f, lcmgen_t *lcm, lcm_struct_t *ls,
                                          const char *const gotype, const uint64_t fingerprint)
{
    for (unsigned int k = 0; k < ls->members->len; k++) {
        lcm_member_t *lm = (lcm_member_t *) g_ptr_array_index(ls->members, k);

        unsigned int i = lcm_find_member_with_named_dimension(ls, lm->membername, k);
        if (i >= ls->members->len) {
            continue;
        }

        char *methodname = go_membername(ls, lm->membername, TRUE);
        char *structname = go_membername(ls, lm->membername, FALSE);
        char *type = go_typename(ls->structname->package, lm->type->package, lm->type->lctypename,
                                 fingerprint);

        emit(0, "// %s returns the value of dynamic array size attribute", methodname);
        emit(0, "// %s.%s.", gotype, lm->membername);
        emit(0, "// And validates that the size is correct for all fields in which it is used.");
        emit(0, "func (p *%s) %s (%s, error) {", gotype, methodname, type);

        unsigned int iteration = 0;

        for (; i < ls->members->len;
             i = lcm_find_member_with_named_dimension(ls, lm->membername, i + 1)) {
            lcm_member_t *lm_ = (lcm_member_t *) g_ptr_array_index(ls->members, i);
            char *membername = go_membername(ls, lm_->membername, TRUE);

            unsigned int j = lcm_find_named_dimension(f, ls, lm_, lm->membername, 0);

            for (; j < lm_->dimensions->len;
                 j = lcm_find_named_dimension(f, ls, lm_, lm->membername, j + 1)) {
                GString *arraystr = g_string_new(NULL);
                unsigned int n =
                    emit_go_array_loops(f, lcm, ls, lm_, arraystr, FALSE, j, fingerprint);

                if (iteration == 0) {
                    emit(j + 1, "// Set value to first dynamic array using this size");
                    emit(j + 1, "// %s%s", membername, arraystr->str);
                    if (n == 0) {
                        emit(j + 1, "p.%s = %s(len(p.%s%s))", structname,
                             map_builtintype_name(lm->type->lctypename), membername, arraystr->str);
                    } else {
                        emit(j + 2, "p.%s = %s(len(p.%s%s))", structname,
                             map_builtintype_name(lm->type->lctypename), membername, arraystr->str);
                    }
                }
                if (iteration == 1 || n != 0) {
                    emit(j + 1, "// Validate size matches all other dynamic arrays");
                    emit_nl();
                }
                if (iteration != 0 || n != 0) {
                    emit(j + 1, "// %s%s", membername, arraystr->str);
                    emit(j + 1, "if int(p.%s) != len(p.%s%s) {", structname, membername,
                         arraystr->str);
                    emit(j + 2, "return 0, fmt.Errorf(\"Defined dynamic array size not \"+");
                    emit(j + 3, "\"matching actual array size (got %%d expected %%d for %s%s)\",",
                         membername, arraystr->str);
                    emit(j + 3, "len(p.%s%s), p.%s)", membername, arraystr->str, structname);
                    emit(j + 1, "}");
                }

                g_string_free(arraystr, TRUE);
                emit_go_array_loops_end(f, n);
                iteration++;
            }

            emit_nl();

            free(membername);
        }

        emit(1, "// Return size");
        emit(1, "return p.%s, nil", structname);
        emit(0, "}");
        emit_nl();

        free(type);
        free(structname);
        free(methodname);
    }
}

/*
 * Emits validation code for a struct member that is used as the size
 * definition for a LCM dynamic array, and populates the read-only member with
 * correct value.
 */
static void emit_go_lcm_dynamic_array_check(FILE *f, lcm_struct_t *ls, lcm_member_t *lm)
{
    if (lcm_find_member_with_named_dimension(ls, lm->membername, 0) < ls->members->len) {
        char *methodname = go_membername(ls, lm->membername, TRUE);
        char *typename = go_membername(ls, lm->membername, FALSE);
        emit(1, "// Validate and populate p.%s", typename);
        emit(1, "if _, err = p.%s; err != nil {", methodname);
        emit(2, "return");
        emit(1, "}");
        free(typename);
        free(methodname);
    }
}

/*
 * Emits Golang code to marshal each of the structs members into a byte slice
 * that can be send.
 */
static void emit_go_lcm_marshal_binary(FILE *f, lcmgen_t *lcm, lcm_struct_t *ls,
                                       const char *const gotype, const uint64_t fingerprint)
{
    emit(0, "// MarshalBinary implements the BinaryMarshaller interface");
    emit(0, "func (p *%s) MarshalBinary() (data []byte, err error) {", gotype);
    if (ls->members->len) {
        emit(1, "var size int");
        emit(1, "if size, err = p.Size(); err != nil {");
        emit(2, "return");
        emit(1, "}");
        emit_nl();
        emit(1, "data = make([]byte, size)");
        emit(1, "offset := 0");
        emit_nl();
    }

    for (unsigned int i = 0; i < ls->members->len; i++) {
        lcm_member_t *lm = (lcm_member_t *) g_ptr_array_index(ls->members, i);

        emit(1, "// LCM struct name: %s", lm->membername);

        char *membername = go_membername(ls, lm->membername, FALSE);

        if (lcm_is_primitive_type(lm->type->lctypename)) {
            if (!lm->dimensions->len) {
                emit_encode_function(f, lm->type->lctypename, membername, 1);
            } else {
                GString *arraystr = g_string_new(membername);
                unsigned int n = emit_go_array_loops(f, lcm, ls, lm, arraystr, FALSE,
                                                     lm->dimensions->len, fingerprint);
                emit_encode_function(f, lm->type->lctypename, arraystr->str, 1 + n);
                emit_go_array_loops_end(f, n);
                g_string_free(arraystr, TRUE);
            }
        } else {
            if (!lm->dimensions->len) {
                emit(1, "{");
                emit(2, "var tmp []byte");
                emit(2, "if tmp, err = p.%s.MarshalBinary(); err != nil {", membername);
                emit(3, "return");
                emit(2, "}");
                emit(2, "offset += copy(data[offset:], tmp)");
                emit(1, "}");
            } else {
                GString *arraystr = g_string_new(NULL);
                unsigned int n = emit_go_array_loops(f, lcm, ls, lm, arraystr, FALSE,
                                                     lm->dimensions->len, fingerprint);
                emit(2, "var tmp []byte");
                emit(2, "if tmp, err = p.%s%s.MarshalBinary(); err != nil {", membername,
                     arraystr->str);
                emit(3, "return");
                emit(2, "}");
                emit(2, "offset += copy(data[offset:], tmp)");
                emit_go_array_loops_end(f, n);
                g_string_free(arraystr, TRUE);
            }
        }

        emit_nl();
        free(membername);
    }

    emit(1, "return");
    emit(0, "}");
    emit_nl();
}

/*
 * Emits the main decode function.
 */
static void emit_go_lcm_decode(FILE *f, lcmgen_t *lcm, lcm_struct_t *ls, const char *const gotype)
{
    emit(0, "// Decode decodes a message (fingerprint & data) from binary form");
    emit(0, "// and verifies that the fingerprint match the expected");
    emit(0, "//");
    emit(0, "// param data The buffer containing the encoded message");
    emit(0, "// returns Error");
    emit(0, "func (p *%s) Decode(data []byte) (err error) {", gotype);
    emit(1, "length := len(data)");
    emit(1, "if length < 8 {");
    emit(2, "return fmt.Errorf(\"Missing fingerprint in buffer\")");
    emit(1, "}");
    emit_nl();
    emit_start(1, "if fp := binary.BigEndian.Uint64(data[:8]); fp != ");
    emit_continue_go_fingerprint_string(f, lcm, gotype);
    emit_end(" {");
    emit(2, "return fmt.Errorf(\"Fingerprints does not match (got %%x expected %%x)\",");
    emit_start(3, "fp, ");
    emit_continue_go_fingerprint_string(f, lcm, gotype);
    emit_end(")");
    emit(1, "}");
    emit_nl();
    emit(1, "if err = p.UnmarshalBinary(data[8:]); err != nil {");
    emit(2, "return");
    emit(1, "}");
    emit_nl();
    emit(1, "length -= 8");
    emit(1, "var size int");
    emit(1, "if size, err = p.Size(); err != nil {");
    emit(2, "return");
    emit(1, "}");
    emit(1, "if length != size {");
    emit(2, "return fmt.Errorf(\"Missing data in buffer (size missmatch, got %%v expected %%v)\",");
    emit(3, "length, size)");
    emit(1, "}");
    emit_nl();
    emit(1, "return");
    emit(0, "}");
    emit_nl();
}

/*
 * Emits Golang code to unmarshal data from a byte slice back into the structs
 * members.
 */
static void emit_go_lcm_unmarshal_binary(FILE *f, lcmgen_t *lcm, lcm_struct_t *ls,
                                         const char *const gotype, const uint64_t fingerprint)
{
    int readVar = 0;
    emit(0, "// UnmarshalBinary implements the BinaryUnmarshaler interface");
    emit(0, "func (p *%s) UnmarshalBinary(data []byte) (err error) {", gotype);
    if (ls->members->len) {
        emit(1, "offset := 0");
        emit_nl();
    }

    for (unsigned int m = 0; m < ls->members->len; m++) {
        lcm_member_t *lm = (lcm_member_t *) g_ptr_array_index(ls->members, m);

        char *membername = go_membername(ls, lm->membername, FALSE);

        if (lcm_is_primitive_type(lm->type->lctypename)) {
            if (!lm->dimensions->len) {
                emit_decode_function(f, lm->type->lctypename, membername, 1);
            } else {
                GString *arraystr = g_string_new(membername);
                unsigned int n = emit_go_array_loops(f, lcm, ls, lm, arraystr, TRUE,
                                                     lm->dimensions->len, fingerprint);
                emit_decode_function(f, lm->type->lctypename, arraystr->str, 1 + n);
                emit_go_array_loops_end(f, n);
                g_string_free(arraystr, TRUE);
            }
        } else {
            if (!lm->dimensions->len) {
                emit(1, "if err = p.%s.UnmarshalBinary(data[offset:]); err != nil {", membername);
                emit(2, "return");
                emit(1, "}");
                emit(1, "{");
                emit(2, "var size int");
                emit(2, "if size, err = p.%s.Size(); err != nil {", membername);
                emit(3, "return");
                emit(2, "}");
                emit(2, "offset += size");
                emit(1, "}");
            } else {
                GString *arraystr = g_string_new(membername);
                unsigned int n = emit_go_array_loops(f, lcm, ls, lm, arraystr, TRUE,
                                                     lm->dimensions->len, fingerprint);
                emit(n + 1, "if err = p.%s.UnmarshalBinary(data[offset:]); err != nil {",
                     arraystr->str);
                emit(n + 2, "return");
                emit(n + 1, "}");
                emit(n + 1, "var size int");
                emit(n + 1, "if size, err = p.%s.Size(); err != nil {", arraystr->str);
                emit(n + 2, "return");
                emit(n + 1, "}");
                emit(n + 1, "offset += size");
                emit_go_array_loops_end(f, n);
                g_string_free(arraystr, TRUE);
            }
        }

        emit_nl();
        free(membername);
    }

    emit(1, "return");
    emit(0, "}");
    emit_nl();
}

/*
 * Emits code to calculate the fingerprint in runtime.
 *
 * Algorithm as described in:
 *     https://lcm-proj.github.io/type_specification.html
 */
static void emit_go_lcm_fingerprint(FILE *f, lcmgen_t *lcm, lcm_struct_t *ls,
                                    const char *const typename, const char *const gotype,
                                    const uint64_t fingerprint)
{
    emit(0, "// Fingerprint generates the LCM fingerprint value for this message");
    emit(0, "func %s_Fingerprint(path ...uint64) uint64 {", gotype);
    emit(1, "for _, v := range path {");
    emit(2, "if v == %s_Fingerprint {", typename);
    emit(3, "return 0");
    emit(2, "}");
    emit(1, "}");
    emit_nl();

    emit(1, "path = append(path, %s_Fingerprint)", typename);
    emit_start(1, "return bits.RotateLeft64(%s_Fingerprint", typename);

    for (unsigned int m = 0; m < ls->members->len; m++) {
        lcm_member_t *lm = (lcm_member_t *) g_ptr_array_index(ls->members, m);

        if (!lcm_is_primitive_type(lm->type->lctypename)) {
            emit_end("+");
            char *lm_gotype = go_typename(ls->structname->package, lm->type->package,
                                          lm->type->lctypename, fingerprint);
            emit_start(2, "%s_Fingerprint(path...)", lm_gotype);
            free(lm_gotype);
        }
    }

    emit_end(", 1)");
    emit(0, "}");
    emit_nl();
}

/*
 * Emits code to calculate the size a string in bytes. This takes into account
 * that LCM will send 4 bytes prepending the string and one terminating it.
 */
static void emit_go_lcm_string_size(FILE *f, int indent, const char *const str_prefix,
                                    const char *const str_name, const char *const str_postfix,
                                    const char *const rec_val)
{
    emit(indent, "%s += 4 // LCM string length", rec_val);
    emit(indent, "%s += len([]byte(%s%s%s))", rec_val, str_prefix, str_name, str_postfix);
    emit(indent, "%s += 1 // LCM zero termination", rec_val);
}

/*
 * Emits codes to calculate the actual size in bytes when this message is
 * marshalled.
 */
static void emit_go_lcm_size(FILE *f, lcmgen_t *lcm, lcm_struct_t *ls, const char *const gotype,
                             const uint64_t fingerprint)
{
    emit(0, "// Size returns the size of this message in bytes");
    emit(0, "func (p *%s) Size() (size int, err error) {", gotype);
    emit_nl();

    if (!ls->members->len) {
        goto ret_size;
    }

    for (unsigned int m = 0; m < ls->members->len; m++) {
        lcm_member_t *lm = (lcm_member_t *) g_ptr_array_index(ls->members, m);

        char *membername = go_membername(ls, lm->membername, FALSE);

        if (lcm_is_primitive_type(lm->type->lctypename)) {
            if (!lm->dimensions->len) {
                if (strcmp(lm->type->lctypename, "string") == 0) {
                    emit_go_lcm_string_size(f, 1, "p.", membername, "", "size");
                } else {
                    emit_go_lcm_dynamic_array_check(f, ls, lm);
                    emit(1, "size += %d // p.%s", primitive_type_size(lm->type->lctypename),
                         membername);
                }
            } else {
                GString *arraystr = g_string_new(NULL);
                unsigned int n = emit_go_array_loops(f, lcm, ls, lm, arraystr, FALSE,
                                                     lm->dimensions->len, fingerprint);

                if (strcmp(lm->type->lctypename, "string") == 0) {
                    emit_go_lcm_string_size(f, n + 1, "p.", membername, arraystr->str, "size");
                } else {
                    emit(n + 1, "size += %d // p.%s", primitive_type_size(lm->type->lctypename),
                         membername);
                }

                emit_go_array_loops_end(f, n);
                g_string_free(arraystr, TRUE);
            }
        } else {
            if (!lm->dimensions->len) {
                emit(1, "{");
                emit(2, "var tmp int");
                emit(2, "if tmp, err = p.%s.Size(); err != nil {", membername);
                emit(3, "return");
                emit(2, "}");
                emit(2, "size += tmp");
                emit(1, "}");
            } else {
                GString *arraystr = g_string_new(NULL);
                unsigned int n = emit_go_array_loops(f, lcm, ls, lm, arraystr, FALSE,
                                                     lm->dimensions->len, fingerprint);
                emit(n + 1, "var tmp int");
                emit(n + 1, "if tmp, err = p.%s%s.Size(); err != nil {", membername, arraystr->str);
                emit(n + 2, "return");
                emit(n + 1, "}");
                emit(n + 1, "size += tmp");
                emit_go_array_loops_end(f, n);
                g_string_free(arraystr, TRUE);
            }
        }

        free(membername);

        emit_nl();
    }

ret_size:
    emit(1, "return");
    emit(0, "}");
}

int emit_go_lcm(lcmgen_t *lcm, lcm_struct_t *ls, const char *const dir, int64_t fingerprint)
{
    int res = 0;
    char *typename = strip_dots(ls->structname->lctypename);
    char *gopackage = strip_dots(ls->structname->package);
    char *gotype = go_typename("", "", ls->structname->lctypename, fingerprint);
    char *path = go_filename(lcm, dir, typename, fingerprint, "");

    if (!lcm_needs_generation(lcm, ls->lcmfile, path))
        return 0;

    if (getopt_get_bool(lcm->gopt, "go-no-overwrite")) {
        if (access(path, F_OK) == 0) {
            printf("%s exists, skipped\n", path);
            return 0;
        } else {
            if (errno != ENOENT) {
                perror(path);
                res = -1;
                goto ret_free;
            }
        }
    }

    FILE *f = fopen(path, "w");
    if (f == NULL) {
        perror(path);
        res = -1;
        goto ret_free;
    }

    // Header
    emit_go_header(f, gopackage);

    // Imports
    emit_go_lcm_imports(f, lcm, ls);

    // Fingerprint const
    if (fingerprint == 0) {
        emit_go_lcm_fingerprint_const(f, ls->hash, typename);
    } else {
        emit_go_lcm_fingerprint_const(f, fingerprint, gotype);
    }

    // Const definitions
    emit_go_lcm_const_definitions(f, lcm, ls, gotype, fingerprint);

    // Struct definition
    if (emit_go_lcm_struct_definition(f, lcm, ls, gotype, fingerprint) != 0) {
        res = -1;
        goto ret_file;
    }

    // Functions
    // Deep copy
    emit_go_lcm_deep_copy(f, lcm, ls, gotype, fingerprint);

    // Encode
    emit_go_lcm_encode(f, lcm, ls, gotype);

    // MarshalBinary
    emit_go_lcm_marshal_binary(f, lcm, ls, gotype, fingerprint);

    // Decode
    emit_go_lcm_decode(f, lcm, ls, gotype);

    // UnmarshalBinary
    emit_go_lcm_unmarshal_binary(f, lcm, ls, gotype, fingerprint);

    // Getters for read only fields (all fields used as array sizes)
    emit_go_lcm_read_only_getters(f, lcm, ls, gotype, fingerprint);

    // Fingerprint
    if (fingerprint == 0) {
        emit_go_lcm_fingerprint(f, lcm, ls, typename, gotype, fingerprint);
    }

    // Size
    emit_go_lcm_size(f, lcm, ls, gotype, fingerprint);

ret_file:
    fclose(f);

ret_free:
    free(typename);
    free(gopackage);
    free(gotype);
    free(path);

    return res;
}

int emit_go_gopacket(lcmgen_t *lcm, lcm_struct_t *ls, const char *const dir, int64_t fingerprint)
{
    char *typename = strip_dots(ls->structname->lctypename);
    char *gopackage = strip_dots(ls->structname->package);
    char *gotype = go_typename("", "", ls->structname->lctypename, fingerprint);
    char *path = go_filename(lcm, dir, typename, fingerprint, "_gopacket");

    if (!lcm_needs_generation(lcm, ls->lcmfile, path))
        return 0;

    FILE *f = fopen(path, "w");
    if (f == NULL)
        return -1;

    // Header
    emit_go_header(f, gopackage);

    // Imports
    emit(0, "import (");
    emit(1, "\"github.com/google/gopacket\"");
    emit(1, "\"github.com/google/gopacket/layers\"");
    emit(0, ")");
    emit_nl();

    // The gopacket LayerClass assigned to us
    emit(0, "var layerClass%s = gopacket.LayerTypeZero", gotype);
    emit_nl();

    // Register our decoder
    emit(0, "func init() {");
    emit(1, "// Register ourselves as decoders for %s.%s", gopackage, typename);
    emit(1, "layerClass%s = layers.RegisterLCMLayerType(", gotype);
    emit(2, "0,");
    emit(2, "\"%s\",", gotype);
    emit_start(2, "layers.LCMFingerprint(");
    emit_continue_go_fingerprint_string(f, lcm, gotype);
    emit_end("),");
    emit(2, "gopacket.DecodeFunc(decodeFunc%s))", gotype);
    emit(0, "}");
    emit_nl();

    // Implement the gopacket Decoder go interface
    emit(0, "// decodeFunc%s is the implementation of Decoder (type DecodeFunc)", gotype);
    emit(0, "func decodeFunc%s(data []byte, pb gopacket.PacketBuilder) error {", gotype);
    emit(1, "lcm := %s{}", gotype);
    emit_nl();
    emit(1, "if err := lcm.DecodeFromBytes(data, pb); err != nil {");
    emit(2, "return err");
    emit(1, "}");
    emit_nl();
    emit(1, "pb.AddLayer(&lcm)");
    emit(1, "pb.SetApplicationLayer(&lcm)");
    emit_nl();
    emit(1, "return nil // No further call since we have no payload");
    emit(0, "}");
    emit_nl();

    // Implement the gopacket DecodingLayer go interface
    emit(0, "// DecodeFromBytes implements DecodingLayer");
    emit(0, "func (lcm *%s) DecodeFromBytes(data []byte, df gopacket.DecodeFeedback) error {",
         gotype);
    emit(1, "return lcm.Decode(data)");
    emit(0, "}");
    emit_nl();

    emit(0, "// CanDecode implements DecodingLayer");
    emit(0, "func (%s) CanDecode() gopacket.LayerClass {", gotype);
    emit(1, "return layerClass%s", gotype);
    emit(0, "}");
    emit_nl();

    emit(0, "// NextLayerType implements DecodingLayer");
    emit(0, "func (%s) NextLayerType() gopacket.LayerType {", gotype);
    emit(1, "return gopacket.LayerTypePayload");
    emit(0, "}");
    emit_nl();

    // Implement the gopacket Layer go interface
    emit(0, "// LayerType implements Layer");
    emit(0, "func (%s) LayerType() gopacket.LayerType {", gotype);
    emit(1, "return layerClass%s", gotype);
    emit(0, "}");
    emit_nl();

    emit(0, "// Payload implements Layer");
    emit(0, "func (lcm *%s) LayerContents() []byte {", gotype);
    emit(1, "data, err := lcm.Encode()");
    emit(1, "if err != nil {");
    emit(2, "panic(err) // What should we do here!?");
    emit(1, "}");
    emit(1, "return data");
    emit(0, "}");
    emit_nl();

    emit(0, "// LayerPayload implements Layer");
    emit(0, "func (%s) LayerPayload() []byte {", gotype);
    emit(1, "return nil");
    emit(0, "}");
    emit_nl();

    // Implement the gopacket ApplicationLayer go interface (embeds Layer)
    emit(0, "// Payload implements ApplicationLayer");
    emit(0, "func (%s) Payload() []byte {", gotype);
    emit(1, "return nil");
    emit(0, "}");
    emit_nl();

    free(gopackage);
    free(gotype);
    free(path);
    free(typename);

    fclose(f);

    return 0;
}

void setup_go_options(getopt_t *gopt)
{
    // clang-format off
    getopt_add_string(gopt, 0, "go-path", ".",
        "Location for .go files");
    getopt_add_bool(gopt, 0, "go-mkdir", TRUE,
        "Create parent directories as needed");
    getopt_add_string(gopt, 0, "go-tag-name", "",
        "Give the struct member tag the a name (e.g. json for `json:\"tag\"`)");
    getopt_add_bool(gopt, 0, "go-strip-dirs", FALSE,
        "Do not generate directories for Go packages");
    getopt_add_bool(gopt, 0, "go-fingerprint", FALSE,
        "Add fingerprint as suffix to filenames and types, fingerprint is "
        "pre-calculate to const");
    getopt_add_bool(gopt, 0, "go-no-overwrite", FALSE,
        "Do not overwrite an existing target file, skip and log");
    getopt_add_bool(gopt, 0, "go-emit-gopacket", FALSE,
        "Emit gopacket API");
    getopt_add_string(gopt, 0, "go-import-prefix", "",
        "Add this package prefix to all LCM type import statements");
    getopt_add_string(gopt, 0, "go-default-package", "lcmtypes",
        "Default Go package if LCM type has no package");
    // clang-format on
}

int emit_go(lcmgen_t *lcm)
{
    int res = 0;

    GString *gopath = g_string_new(getopt_get_string(lcm->gopt, "go-path"));
    gopath = g_string_append_c(gopath, '/');

    ////////////////////////////////////////////////////////////
    // ENUMS
    // Deprecated and thus not supported
    if (lcm->enums->len) {
        fprintf(stderr, "Go generator does not support enums.\n");
        res = -1;
        goto ret;
    }

    if (!strlen(lcm->package)) {
        fprintf(stderr, "Not yet implemented: go-default-package\n");
        res = -1;
        goto ret;
    }

    if (strcmp(getopt_get_string(lcm->gopt, "package-prefix"), "") != 0) {
        fprintf(stderr, "Go generator does not support --package-prefix\n");
        res = -1;
        goto ret;
    }

    ////////////////////////////////////////////////////////////
    // STRUCTS
    for (unsigned int i = 0; i < lcm->structs->len; i++) {
        GString *dir = g_string_new(gopath->str);

        lcm_struct_t *ls = (lcm_struct_t *) g_ptr_array_index(lcm->structs, i);

        if (!getopt_get_bool(lcm->gopt, "go-strip-dirs")) {
            char *pkg_path = dots_to_path(ls->structname->package);
            dir = g_string_append(dir, pkg_path);
            dir = g_string_append_c(dir, '/');
            free(pkg_path);
        }

        // Create path if not existing and requested
        if (getopt_get_bool(lcm->gopt, "go-mkdir")) {
            char *dirname = g_path_get_dirname(dir->str);
            g_mkdir_with_parents(dirname, 0755);
            g_free(dirname);
        }

        if (ls->enums->len) {
            fprintf(stderr, "Go generator does not support enums.\n");
            return -1;
        }

        if (ls->structs->len) {
            fprintf(stderr, "Go generator does not (yet) support embedded structs.\n");
            return -1;
        }

        int64_t fingerprint = 0;
        if (getopt_get_bool(lcm->gopt, "go-fingerprint")) {
            fingerprint = lcm_get_fingerprint(lcm, ls);
            if (fingerprint == 0) {
                fprintf(
                    stderr,
                    "Unable to calculate fingerprint as requested (--go-fingerprint) for '%s'.\n",
                    ls->structname->shortname);
                res = -1;
                goto ret;
            }
        }

        if (emit_go_lcm(lcm, ls, dir->str, fingerprint)) {
            fprintf(stderr, "Emit Go LCM structs failed.\n");
            res = -1;
            goto ret;
        }

        if (getopt_get_bool(lcm->gopt, "go-emit-gopacket") &&
            emit_go_gopacket(lcm, ls, dir->str, fingerprint)) {
            fprintf(stderr, "Emit Go gopacket API failed.\n");
            res = -1;
            goto ret;
        }

        g_string_free(dir, TRUE);
    }

ret:
    g_string_free(gopath, TRUE);

    return res;
}
