#include <stdio.h>
#include <stdint.h>
#include <ctype.h>
#include <string.h>
#include <unistd.h>
#include <stdarg.h>
#include <stdlib.h>
#include <assert.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <inttypes.h>
#include "lcmgen.h"

#include "tokenize.h"

lcm_struct_t *parse_struct(lcm_t *lcm, const char *lcmfile, tokenize_t *t);
lcm_enum_t *parse_enum(lcm_t *lcm, const char *lcmfile, tokenize_t *t);

lcm_t *lcm_create()
{
    lcm_t *lcm = (lcm_t*) calloc(1, sizeof(lcm_t));
    lcm->structs = g_ptr_array_new();
    lcm->enums = g_ptr_array_new();
    return lcm;
}

lcm_type_t *lcm_type_create()
{
    lcm_type_t *lt = (lcm_type_t*) calloc(1, sizeof(lcm_type_t));

    return lt;
}

lcm_struct_t *lcm_struct_create(const char *lcmfile, const char *structname)
{
    lcm_struct_t *lr = (lcm_struct_t*) calloc(1, sizeof(lcm_struct_t));
    lr->lcmfile    = strdup(lcmfile);
    lr->structname = strdup(structname);
    lr->members    = g_ptr_array_new();
    lr->enums      = g_ptr_array_new();
    lr->structs    = g_ptr_array_new();
    return lr;
}

lcm_enum_t *lcm_enum_create(const char *lcmfile, const char *name)
{
    lcm_enum_t *le = (lcm_enum_t*) calloc(1, sizeof(lcm_enum_t));
    le->lcmfile  = strdup(lcmfile);
    le->enumname = strdup(name);
    le->values   = g_ptr_array_new();

    return le;
}

lcm_enum_value_t *lcm_enum_value_create(const char *name)
{
    lcm_enum_value_t *lev = (lcm_enum_value_t*) calloc(1, sizeof(lcm_enum_t));

    lev->valuename = strdup(name);

    return lev;
}

lcm_member_t *lcm_member_create()
{
    lcm_member_t *lm = (lcm_member_t*) calloc(1, sizeof(lcm_member_t));
    return lm;
}

static int64_t hash_update(int64_t v, char c)
{
    v = ((v<<8) ^ (v>>55)) + c;

    return v;
}

static int64_t hash_string_update(int64_t v, const char *s)
{
    v = hash_update(v, strlen(s));

    for (; *s != 0; s++)
        v = hash_update(v, *s);

    return v;
}
 
int64_t lcm_struct_hash(lcm_struct_t *lr)
{
    int64_t v = 0x12345678;

    // NO: Purposefully, we do NOT include the structname in the hash.
    // this allows people to rename data types and still have them work.
    //  v = hash_string_update(v, lr->structname);

    for (unsigned int i = 0; i < g_ptr_array_size(lr->members); i++) {
        lcm_member_t *lm = g_ptr_array_index(lr->members, i);

        // hash the member name
        v = hash_string_update(v, lm->membername);

        // if the member is a primitive type, include the type
        // signature in the hash. Do not include them for compound
        // members, because their contents will be included, and we
        // don't want a struct's name change to break the hash.
        if (lcm_is_primitive_type(lm->type->typename))
            v = hash_string_update(v, lm->type->typename);

        // hash the dimensionality information
        int ndim = g_ptr_array_size(lm->dimensions);
        v = hash_update(v, ndim);
        for (int i = 0; i < ndim; i++) {
            lcm_dimension_t *dim = (lcm_dimension_t*) g_ptr_array_index(lm->dimensions, i);
            v = hash_update(v, dim->mode);
            v = hash_string_update(v, dim->size);
        }
    }

    return v;
}

int64_t lcm_enum_hash(lcm_enum_t *le)
{
    int64_t v = 0x87654321;

    v = hash_string_update(v, le->enumname);
    return v;
}

/** recursive-descent parser **/

// semantic error: it parsed fine, but it's illegal. (we don't try to
// identify the offending token)
void semantic_error(tokenize_t *t, const char *fmt, ...)
{
    va_list ap;
    va_start(ap, fmt);

    printf("\n");
    vprintf(fmt, ap);
    printf("\n");

    printf("%s : %i\n", t->path, t->line);
    printf("%s", t->line_buffer);

    va_end(ap);
    _exit(0);
}

// semantic warning: it parsed fine, but it's dangerous.
void semantic_warning(tokenize_t *t, const char *fmt, ...)
{
    va_list ap;
    va_start(ap, fmt);

    printf("\n");
    vprintf(fmt, ap);
    printf("\n");

    printf("%s : %i\n", t->path, t->line);
    printf("%s", t->line_buffer);

    va_end(ap);
}

void parse_error(tokenize_t *t, const char *fmt, ...)
{
    va_list ap;
    va_start(ap, fmt);

    printf("\n");
    vprintf(fmt, ap);
    printf("\n");

    printf("%s : %i\n", t->path, t->line);
    printf("%s", t->line_buffer);
    for (int i = 0; i < t->column; i++) {
        if (isspace(t->line_buffer[i]))
            printf("%c", t->line_buffer[i]);
        else
            printf(" ");
    }
    printf("^\n");

//  printf("%15s:%i.%i     ", t->path, t->line, t->column);

    va_end(ap);
    _exit(0);
}

/** If the next token is "tok", consume it and return 1. Else, return
 * 0. **/
int parse_try_consume(tokenize_t *t, const char *tok)
{
    int res = tokenize_peek(t);
    if (res == EOF)
        parse_error(t, "End of file while looking for %s.", tok);

    res = (!strcmp(t->token, tok));

    // consume if the token matched
    if (res)
        tokenize_next(t);

    return res;
}

/** Consume the next token. If it's not "tok", an error is emitted and
    the program exits. **/
void parse_require(tokenize_t *t, char *tok)
{
    int res = tokenize_next(t);
    if (res == EOF || strcmp(t->token, tok)) 
        parse_error(t, "expected token %s", tok);

}

/** require that the next token exist (not EOF). Description is a
 * human-readable description of what was expected to be read. **/
void require_next(tokenize_t *t, const char *description)
{
    int res = tokenize_next(t);
    if (res == EOF)
        parse_error(t, "End of file reached, expected %s.", description);
}

/* a very simple heuristic: does the type name contain the letters "int"? */
int is_integer_type(const char *typename)
{
    if (strstr(typename, "int")!=NULL)
        return 1;
    return 0;
}

/** parse a member declaration. This looks long and scary, but most of
 * the code is for semantic analysis (error checking) **/
int parse_member(lcm_t *lcm, lcm_struct_t *lr, tokenize_t *t)
{
    lcm_type_t *lt = lcm_type_create();

    // inline type declaration?
    if (parse_try_consume(t, "struct")) {
        lcm_struct_t *lrr = parse_struct(lcm, lr->lcmfile, t);
        g_ptr_array_add(lr->structs, lrr);

        if (parse_try_consume(t, ";")) 
            return 0;
        
        // they're declaring something...
        lt->typename = strdup(lrr->structname);

        parse_error(t, "recursive structs not implemented.");

    } else  if (parse_try_consume(t, "enum")) {
        lcm_enum_t *le = parse_enum(lcm, lr->lcmfile, t);
        g_ptr_array_add(lr->enums, le);

        // did they just declare the enum and no variables that use it?
        if (parse_try_consume(t, ";")) 
            return 0;
        
        // they're declaring something...
        lt->typename = strdup(le->enumname);
    } else if (parse_try_consume(t, "union")) {
        parse_error(t, "unions not supported.");
    } else {
        // standard declaration
        require_next(t, "type identifier");

        if (!isalpha(t->token[0]) && t->token[0]!='_')
            parse_error(t, "invalid type name");
        
        lt->typename = strdup(t->token);

        if (getopt_get_bool(lcm->gopt, "warn-unsafe")) {
            if (!strcmp(lt->typename, "uint64_t")) {
                semantic_warning(t, "uint64_t has no safe java implementation");
            }
            else if (lcm_is_primitive_type(lt->typename) && lt->typename[0]=='u') {
                semantic_warning(t, "Unsigned types are not supported in java and require promotion. Suggest using next-larger signed type.");
            }
        }
    }

    require_next(t, "name identifier");

    while (1) {
        lcm_member_t *lm = lcm_member_create();
        lm->type = lt;

        if (!isalpha(t->token[0]) && t->token[0]!='_')
            parse_error(t, "Invalid member name: must start with [a-zA-Z_].");

        // make sure this name isn't already taken.
        for (unsigned int i = 0; i < g_ptr_array_size(lr->members); i++) {
            lcm_member_t *thislm = (lcm_member_t*) g_ptr_array_index(lr->members, i);
            if (!strcmp(thislm->membername, t->token)) {
                semantic_error(t, "Duplicate member name '%s'.", t->token);
            }
        }

        lm->membername = strdup(t->token);
        g_ptr_array_add(lr->members, lm);
        
        lm->dimensions = g_ptr_array_new();

        // (multi-dimensional) array declaration?
        while (parse_try_consume(t, "[")) {

            // pull out the size of the dimension, either a number or a variable name.
            require_next(t, "array size");

            lcm_dimension_t *dim = (lcm_dimension_t*) calloc(1, sizeof(lcm_dimension_t));
            
            if (isdigit(t->token[0])) {
                int sz = strtol(t->token, NULL, 0);
                if (sz <= 0)
                    semantic_error(t, "Constant array size must be > 0");

                dim->mode = LCM_CONST;
                dim->size = strdup(t->token);

            } else {
                if (t->token[0]==']')
                    semantic_error(t, "Array sizes must be declared either as a constant or variable.");
                if (!isalpha(t->token[0]) && t->token[0]!='_')
                    semantic_error(t, "Invalid array size variable name: must start with [a-zA-Z_].");

                // make sure the named variable is 
                // 1) previously declared and 
                // 2) an integer type
                int okay = 0;

                for (unsigned int i = 0; i < g_ptr_array_size(lr->members); i++) {
                    lcm_member_t *thislm = (lcm_member_t*) g_ptr_array_index(lr->members, i);
                    if (!strcmp(thislm->membername, t->token)) {
                        if (g_ptr_array_size(thislm->dimensions) != 0)
                            semantic_error(t, "Array dimension '%s' must be not be an array type.", t->token);
                        if (!is_integer_type(thislm->type->typename))
                            semantic_error(t, "Array dimension '%s' must be an integer type.", t->token);
                        okay = 1;
                        break;
                    }
                }
                if (!okay) 
                    semantic_error(t, "Unknown variable array index '%s'. Index variables must be declared before the array.", t->token);

                dim->mode = LCM_VAR;
                dim->size = strdup(t->token);
            }
            parse_require(t, "]");

            // increase the dimensionality of the array by one dimension.
            g_ptr_array_add(lm->dimensions, dim);
        }

        if (parse_try_consume(t, ",")) {
            require_next(t, "name identifier");
            continue;
        }

        break;
    }

    parse_require(t, ";");

    return 0;
}

int parse_enum_value(lcm_enum_t *le, tokenize_t *t)
{
    require_next(t, "enum name");

    lcm_enum_value_t *lev = lcm_enum_value_create(t->token);

    if (parse_try_consume(t, "=")) {
        require_next(t, "enum value literal");

        lev->value = strtol(t->token, NULL, 0);
    } else {
        // the didn't specify the value, compute the next largest value
        int32_t max = 0;

        for (unsigned int i = 0; i < g_ptr_array_size(le->values); i++) {
            lcm_enum_value_t *tmp = g_ptr_array_index(le->values, i);
            if (tmp->value > max)
                max = tmp->value;
        }

        lev->value = max + 1;
    }

    // make sure there aren't any duplicate values
    for (unsigned int i = 0; i < g_ptr_array_size(le->values); i++) {
        lcm_enum_value_t *tmp = g_ptr_array_index(le->values, i);
        if (tmp->value == lev->value)
            semantic_error(t, "Enum values %s and %s have the same value %d!", tmp->valuename, lev->valuename, lev->value);
        if (!strcmp(tmp->valuename, lev->valuename))
            semantic_error(t, "Enum value %s declared twice!", tmp->valuename);
    }

    g_ptr_array_add(le->values, lev);
    return 0;
}

/** assume the "struct" token is already consumed **/
lcm_struct_t *parse_struct(lcm_t *lcm, const char *lcmfile, tokenize_t *t)
{
    char     *name;

    require_next(t, "struct name");
    name = strdup(t->token);

    lcm_struct_t *lr = lcm_struct_create(lcmfile, name);
    
    parse_require(t, "{");
    
    while (!parse_try_consume(t, "}"))
        parse_member(lcm, lr, t);
    
    lr->hash = lcm_struct_hash(lr);

    free(name);
    return lr;
}

/** assumes the "enum" token is already consumed **/
lcm_enum_t *parse_enum(lcm_t *lcm, const char *lcmfile, tokenize_t *t)
{
    char     *name;

    require_next(t, "enum name");
    name = strdup(t->token);

    lcm_enum_t *le = lcm_enum_create(lcmfile, name);
    parse_require(t, "{");
    
    while (!parse_try_consume(t, "}")) {
        parse_enum_value(le, t);
        
        parse_try_consume(t, ",");
        parse_try_consume(t, ";");
    }

    le->hash = lcm_enum_hash(le);
    free(name);
    return le;
}

/** parse entity (top-level construct), return EOF if eof. **/
int parse_entity(lcm_t *lcm, const char *lcmfile, tokenize_t *t)
{
    int res;

    res = tokenize_next(t);
    if (res==EOF)
        return EOF;

    if (!strcmp(t->token, "struct")) {
        lcm_struct_t *lr = parse_struct(lcm, lcmfile, t);
        g_ptr_array_add(lcm->structs, lr);
        return 0;
    }

    if (!strcmp(t->token, "enum")) {
        lcm_enum_t *le = parse_enum(lcm, lcmfile, t);
        g_ptr_array_add(lcm->enums, le);
        return 0;
    }

    if (!strcmp(t->token, "union")) {
        parse_error(t,"unions not implemented\n");

        return 0;
    }

    parse_error(t,"Missing struct/enum/union token.");
    return -1;

}

int handle_file(lcm_t *lcm, const char *path)
{
    tokenize_t *t = tokenize_create(path);

    if (t==NULL) {
        perror(path);
        return -1;
    }

    if (getopt_get_bool(lcm->gopt, "tokenize")) {
        int ntok = 0;
        printf("%6s %6s %6s: %s\n", "tok#", "line", "col", "token");

        while (tokenize_next(t)!=EOF)
            printf("%6i %6i %6i: %s\n", ntok++, t->line, t->column, t->token);
        return 0;
    }

    int res;
    do {
        res = parse_entity(lcm, path, t);
    } while (res != EOF);

    tokenize_destroy(t);
    return 0;
}

void lcm_type_dump(lcm_type_t *lt)
{
    char buf[1024];
    int pos = 0;

    pos += sprintf(&buf[pos], "%s", lt->typename);

    printf("\t%-20s", buf);
}

void lcm_member_dump(lcm_member_t *lm)
{
    lcm_type_dump(lm->type);

    printf("  ");

    printf("%s", lm->membername);

    int ndim = g_ptr_array_size(lm->dimensions);
    for (int i = 0; i < ndim; i++) {
        lcm_dimension_t *dim = g_ptr_array_index(lm->dimensions, i);
        switch (dim->mode) 
        {
        case LCM_CONST:
            printf(" [ (const) %s ]", dim->size);
            break;
        case LCM_VAR:
            printf(" [ (var) %s ]", dim->size);
            break;
        default:
            // oops! unhandled case
            assert(0);
        }
    }

    printf("\n");
}

void lcm_enum_dump(lcm_enum_t *le)
{
    printf("enum %s\n", le->enumname);
    for (unsigned int i = 0; i < g_ptr_array_size(le->values); i++) {
        lcm_enum_value_t *lev = g_ptr_array_index(le->values, i);
        printf("        %-20s  %i\n", lev->valuename, lev->value);
    }
}

void lcm_struct_dump(lcm_struct_t *lr)
{
    printf("struct %s [hash=0x%16"PRId64"]\n", lr->structname, lr->hash);

    for (unsigned int i = 0; i < g_ptr_array_size(lr->members); i++) {
        lcm_member_t *lm = g_ptr_array_index(lr->members, i);
        lcm_member_dump(lm);
    }

    for (unsigned int i = 0; i < g_ptr_array_size(lr->enums); i++) {
        lcm_enum_t *le = g_ptr_array_index(lr->enums, i);
        lcm_enum_dump(le);
    }

}

void lcm_dump(lcm_t *lcm)
{
    for (unsigned int i = 0; i < g_ptr_array_size(lcm->enums); i++) {
        lcm_enum_t *le = g_ptr_array_index(lcm->enums, i);
        lcm_enum_dump(le);
    }

    for (unsigned int i = 0; i < g_ptr_array_size(lcm->structs); i++) {
        lcm_struct_t *lr = g_ptr_array_index(lcm->structs, i);
        lcm_struct_dump(lr);
    }
}

/** Find and return the member whose name is name. **/
lcm_member_t *lcm_find_member(lcm_struct_t *lr, const char *name)
{
    for (unsigned int i = 0; i < g_ptr_array_size(lr->members); i++) {
        lcm_member_t *lm = (lcm_member_t*) g_ptr_array_index(lr->members, i);
        if (!strcmp(lm->membername, name))
            return lm;
    }

    assert(0);
}

/** returns 1 if the type is one of the built-in primitives. **/
int lcm_is_primitive_type(const char *t)
{
    return (!strcmp(t, "uint8_t") ||
            !strcmp(t, "int8_t") ||
            !strcmp(t, "byte") ||
            !strcmp(t, "uint16_t") ||
            !strcmp(t, "int16_t") ||
            !strcmp(t, "uint32_t") ||
            !strcmp(t, "int32_t") ||
            !strcmp(t, "uint64_t") ||
            !strcmp(t, "int64_t") ||
            !strcmp(t, "float") ||
            !strcmp(t, "double") ||
            !strcmp(t, "string") ||
            !strcmp(t, "boolean"));
}

void setup_c_options(getopt_t *gopt);
int emit_c(lcm_t *lcm);

void setup_java_options(getopt_t *gopt);
int emit_java(lcm_t *lcm);

void setup_python_options(getopt_t *gopt);
int emit_python(lcm_t *lcm);

int main(int argc, char *argv[])
{
    getopt_t *gopt = getopt_create();

    getopt_add_bool  (gopt, 'h',   "help",    0,     "Show this help");
    getopt_add_bool  (gopt, 't',  "tokenize", 0,     "Show tokenization");
    getopt_add_bool  (gopt, 'd',   "debug",   0,     "Show parsed file");
    getopt_add_bool  (gopt, 0,     "lazy",    0,     "Generate output file only if .lcm is newer");
    getopt_add_bool  (gopt, 0,    "warn-unsafe", 1,  "Emit warnings for cross-platform unsafe declarations");

    getopt_add_spacer(gopt, "**** C options ****");
    getopt_add_bool  (gopt, 'c', "c",         0,     "Emit C code");
    setup_c_options(gopt);

    getopt_add_spacer(gopt, "**** Java options ****");
    getopt_add_bool  (gopt, 'j', "java",      0,     "Emit Java code");
    setup_java_options(gopt);

    getopt_add_spacer(gopt, "**** Python options ****");
    getopt_add_bool  (gopt, 'p', "python",      0,     "Emit Python code");
    setup_python_options(gopt);

    if (!getopt_parse(gopt, argc, argv, 1) || getopt_get_bool(gopt,"help")) {
        printf("Usage: %s [options] <input files>\n\n", argv[0]);
        getopt_do_usage(gopt);
        return 0;
    }

    lcm_t *lcm = lcm_create();
    lcm->gopt = gopt;

    for (unsigned int i = 0; i < g_ptr_array_size(gopt->extraargs); i++) {
        char *path = g_ptr_array_index(gopt->extraargs, i);

        int res = handle_file(lcm, path);
        if (res)
            return -1;
    }

    int did_something = 0;
    // if they requested tokenizing (debug) output, we've done that now. Exit.
    if (getopt_get_bool(gopt, "tokenize")) {
        did_something = 1;
        return 0;
    }

    if (getopt_get_bool(gopt, "debug")) {
        did_something = 1;
        lcm_dump(lcm);
    }

    if (getopt_get_bool(gopt, "c")) {
        did_something = 1;
        if (emit_c(lcm)) {
            printf("An error occurred while emitting C code.\n");
        }
    }

    if (getopt_get_bool(gopt, "java")) {
        did_something = 1;
        if (emit_java(lcm)) {
            printf("An error occured while emitting Java code.\n");
        }
    }

    if (getopt_get_bool(gopt, "python")) {
        did_something = 1;
        if (emit_python(lcm)) {
            printf("An error occured while emitting Python code.\n");
        }
    }

    if (did_something == 0) {
        printf("No actions specified. Try --help.\n");
    }

    return 0;
}

int lcm_needs_generation(lcm_t *lcm, const char *declaringfile, const char *outfile)
{
    struct stat instat, outstat;
    int res;

    if (!getopt_get_bool(lcm->gopt, "lazy"))
        return 1;

    res = stat(declaringfile, &instat);
    if (res) {
        printf("Funny error: can't stat the .lcm file");
        perror(declaringfile);
        return 1;
    }

    res = stat(outfile, &outstat);
    if (res) {
//      perror(outfile);
        return 1;
    }

    return instat.st_mtime > outstat.st_mtime;
}
