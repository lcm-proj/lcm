#include <stdio.h>
#include <stdint.h>
#include <assert.h>
#include <string.h>
#include <stdlib.h>
#include <ctype.h>
#include <inttypes.h>
#include <sys/stat.h>
#include <sys/types.h>

#include <glib.h>

#include "lcmgen.h"

#define INDENT(n) (4*(n))

#define emit_start(n, ...) do { fprintf(f, "%*s", INDENT(n), ""); fprintf(f, __VA_ARGS__); } while(0)
#define emit_continue(...) do { fprintf(f, __VA_ARGS__); } while(0)
#define emit_end(...) do { fprintf(f, __VA_ARGS__); fprintf(f, "\n"); } while(0)
#define emit(n, ...) do { fprintf(f, "%*s", INDENT(n), ""); fprintf(f, __VA_ARGS__); fprintf(f, "\n"); } while(0)

#define err(...) fprintf(stderr, __VA_ARGS__)

const char *typename_to_matlab_type(const char *typename)
{
    if(!strcmp(typename, "int8_t"))
        return "int8";
    if(!strcmp(typename, "int16_t"))
        return "int16";
    if(!strcmp(typename, "int32_t"))
        return "int32";
    if(!strcmp(typename, "int64_t"))
        return "int64";
    if(!strcmp(typename, "byte"))
        return "int8";
    if(!strcmp(typename, "float"))
        return "single";
    if(!strcmp(typename, "double"))
        return "double";
    if(!strcmp(typename, "string"))
        return "char";
    if(!strcmp(typename, "boolean"))
        return "logical";
    return NULL;
}

static void 
mkdir_with_parents(const char *path, mode_t mode)
{
    int len = strlen(path);
    for(int i = 0; i < len; i++) {
        if(path[i]=='/') {
            char *dirpath = malloc(i+1);
            strncpy(dirpath, path, i);
            dirpath[i]=0;

            mkdir(dirpath, mode);
            free(dirpath);

            i++; // skip the '/'
        }
    }
}

static char * 
build_filenamev(char **parts)
{
    char **p = parts;
    int total_len = 0;
    for(p = parts; *p; p++) {
        total_len += strlen(*p) + strlen(G_DIR_SEPARATOR_S "+");
    }
    total_len ++;
    char *result = malloc(total_len);
    memset(result, 0, total_len);
    for(p = parts; *p; p++) {
        if(! strlen(*p)) continue;
        strncat(result, "+", total_len);
        strncat(result, *p, total_len);
        if(*(p+1)) 
            strncat(result, G_DIR_SEPARATOR_S, total_len);
    }
    return result;
}

static void
get_all_vals_helper(gpointer key, gpointer value, gpointer user_data)
{
    GPtrArray *vals = (GPtrArray*) user_data;
    g_ptr_array_add(vals, value);    
}

static GPtrArray * 
_hash_table_get_vals(GHashTable *hash_table)
{
    GPtrArray *vals = g_ptr_array_sized_new(g_hash_table_size(hash_table));
    g_hash_table_foreach(hash_table, get_all_vals_helper, vals);
    return vals;
}

void setup_matlab_options(getopt_t *gopt)
{
    getopt_add_string(gopt, 0,   "mpath",     "",         
            "MATLAB destination directory");
}

static int
is_primitive_numeric_type(const char *t)
{
    return lcm_is_primitive_type(t) && strcmp(t, "string");
}

static int
_primitive_type_size(const char *tn)
{
    if(!strcmp("byte", tn)) return 1;
    if(!strcmp("boolean", tn)) return 1;
    if(!strcmp("int8_t", tn)) return 1;
    if(!strcmp("int16_t", tn)) return 2;
    if(!strcmp("int32_t", tn)) return 4;
    if(!strcmp("int64_t", tn)) return 8;
    if(!strcmp("float", tn)) return 4;
    if(!strcmp("double", tn)) return 8;
    if(!strcmp("string", tn)) return 1;
    assert(0);
}

static void
emit_matlab_properties(const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls)
{
    emit(1, "properties");
    for(unsigned int member = 0; member < ls->members->len; member++) {
        lcm_member_t *lm = g_ptr_array_index(ls->members, member);

        if(lm->dimensions->len > 0) {
            // emit initializer for array/cell members
            if(is_primitive_numeric_type(lm->type->typename)) {
                emit_start(2, "%s = zeros(", lm->membername);
            } else {
                emit_start(2, "%s = cell(", lm->membername);
            }

            // array dimensions...
            for(int di=0; di<lm->dimensions->len; di++) {
                lcm_dimension_t *dim = 
                    (lcm_dimension_t*) g_ptr_array_index(lm->dimensions, di);
                if(dim->mode == LCM_CONST) {
                    emit_continue("%s", dim->size);
                } else {
                    emit_continue("0");
                }
                if(di < lm->dimensions->len - 1)
                    emit_continue(", ");
            }
            if(lm->dimensions->len == 1)
                emit_continue(", 1");

            if(is_primitive_numeric_type(lm->type->typename)) {
                emit_end(", '%s');", 
                        typename_to_matlab_type(lm->type->typename));
            } else {
                emit_end(");");
            }
        } else {
            // non array members
            if(is_primitive_numeric_type(lm->type->typename)) {
                emit(2, "%s = %s(0);", lm->membername, 
                        typename_to_matlab_type(lm->type->typename));
            } else if(!strcmp(lm->type->typename, "string")) {
                emit(2, "%s = '';", lm->membername);
            } else {
                emit(2, "%s = %s;", lm->membername, lm->type->typename);
            }
        }
    }

    emit(1, "end\n");
}

// CONSTANTS
static void
emit_matlab_constants(const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls)
{
    fprintf(f, "    properties(Constant=true)\n");
    for(unsigned int cn = 0; cn < ls->constants->len; cn++) {
        lcm_constant_t *lc = g_ptr_array_index(ls->constants, cn);
        assert(lcm_is_legal_const_type(lc->typename));
        emit(2, "%s = %s(%s)", lc->membername, 
                typename_to_matlab_type(lc->typename), lc->val_str);
    }
    emit(2, "LCM_FINGERPRINT = uint8(%s.get_hash_recursive({}))';", 
            ls->structname->typename);
    fprintf(f, "    end\n\n");
}

static void
emit_matlab_encode_one(const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls)
{
    emit(1, "function data=encode_one(obj, isLittleEndian)");

    const char *swapbytes[] = { "swapbytes", "" };
    const char *swapendclause[] = { "else", "end" };

    emit(2, "if isLittleEndian");
    int eind = 0;
    for(unsigned int swapind=0; swapind<2; swapind++) {
        eind = 0;

        for(unsigned int m = 0; m < ls->members->len; m++) {
            lcm_member_t *lm = g_ptr_array_index(ls->members, m);

            if(0 == lm->dimensions->len) {
                if(is_primitive_numeric_type(lm->type->typename)) {
                    int bytes_per_elem = _primitive_type_size(lm->type->typename);
                    if(bytes_per_elem == 1) {
                        emit(3, "enc%d = typecast(%s(obj.%s), 'uint8');", 
                                eind, 
                                typename_to_matlab_type(lm->type->typename),
                                lm->membername);
                    } else {
                        emit(3, "enc%d = typecast(%s(%s(obj.%s)), 'uint8')';", 
                                eind, 
                                swapbytes[swapind], 
                                typename_to_matlab_type(lm->type->typename),
                                lm->membername);
                    }
                } else if(!strcmp(lm->type->typename, "string")) {
                    emit(3, "enc%d = [ unicode2native(obj.%s, 'UTF-8') 0]';", eind+1,
                            lm->membername);
                    emit(3, "enc%d = typecast(%s(uint32(length(enc%d))), 'uint8')';",
                            eind, swapbytes[swapind], eind+1);
                    eind++;
                } else {
                    emit(3, "enc%d = obj.%s.encode_one();", eind, lm->membername);
                }
            } else {
                if(is_primitive_numeric_type(lm->type->typename)) {
                    emit_start(3, "tmpar = %s(obj.%s(", 
                            typename_to_matlab_type(lm->type->typename),
                            lm->membername);
                } else {
                    emit_start(3, "tmpar = (obj.%s(", lm->membername);
                }

                for(int di=0; di<lm->dimensions->len; di++) {
                    lcm_dimension_t *dim = (lcm_dimension_t*) g_ptr_array_index(lm->dimensions, di);
                    if(dim->mode == LCM_CONST) {
                        emit_continue("1:%s", dim->size);
                    } else {
                        emit_continue("1:obj.%s", dim->size);
                    }
                    if(di < lm->dimensions->len-1)
                        emit_continue(",");
                }
                if(lm->dimensions->len > 1)  {
                    emit_end("))';");
                    emit(3, "tmpar = tmpar(:);");
                } else {
                    emit_end("));");
                }

                if(is_primitive_numeric_type(lm->type->typename)) {
                    int bytes_per_elem = _primitive_type_size(lm->type->typename);
                    if(bytes_per_elem == 1) {
                        emit(3, "enc%d = typecast(tmpar, 'uint8');", eind);
                    } else {
                        emit(3, "enc%d = typecast(%s(tmpar), 'uint8');", eind, swapbytes[swapind]);
                    }
                } else {
                    emit(3, "enc%d = [];", eind);
                    emit(3, "for tmpi = 1:length(tmpar)");

                    if(!strcmp(lm->type->typename, "string")) {
                        emit(4, "encstr = [ unicode2native(tmpar{tmpi}, 'UTF-8') 0];");
                        emit(4, "enc%d = [ enc%d ; typecast(%s(uint32(length(encstr))),'uint8')' ; encstr' ];",
                                eind, eind, swapbytes[swapind]);
                    } else {
                        emit(4, "enc%d = [ enc%d ; tmpar{tmpi}.encode_one(isLittleEndian) ];", eind, eind);
                    }

                    emit(3, "end");
                }
            }

            eind++;
        }
        emit(2, "%s", swapendclause[swapind]);
    }
    emit_start(2, "data = [");
    for(int i=0; i<eind; i++) {
        emit_continue("enc%d", i);
        if(i<eind-1)
            emit_continue(";");
    }
    emit_end("];");
    
    emit(1, "end\n");
}

static void
emit_matlab_encode(const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls)
{
    emit(1, "function data=encode(obj)");
    emit(2,     "[ctype, msize, endian] = computer;");
    emit(2,     "isLittleEndian = (endian == 'L');");
    emit(2,     "data=[ %s.LCM_FINGERPRINT ; obj.encode_one(isLittleEndian) ];", ls->structname->typename);
    emit(1, "end\n");
}

static void
emit_matlab_decode_one(const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls)
{
    emit(1, "function [obj, bytes_consumed]=decode_one(buf, off, isLittleEndian)");
    emit(2, "obj = %s();\n", ls->structname->typename);
    emit(2, "start_off = off;");

    const char *swapbytes[] = { "swapbytes", "" };
    const char *swapendclause[] = { "else", "end" };

    emit(2, "if isLittleEndian");
    for(unsigned int swapind=0; swapind<2; swapind++) {

        for(unsigned int m = 0; m < ls->members->len; m++) {
            lcm_member_t *lm = g_ptr_array_index(ls->members, m);

            if(is_primitive_numeric_type(lm->type->typename)) {
                // handle all numerical primitive types here

                int bytes_per_elem = _primitive_type_size(lm->type->typename);

                if(0 == lm->dimensions->len) {
                    // this is the easy case.  member is a single numerical
                    // primitive type.
                    int nbytes = bytes_per_elem;
                    emit(3, "obj.%s = %s(typecast(buf(off:off+%d), '%s'));",
                            lm->membername, 
                            swapbytes[swapind],
                            nbytes - 1, 
                            typename_to_matlab_type(lm->type->typename));
                    emit(3, "off = off + %d;", nbytes);
                } else {
                    // slightly more complex... member is a numerical array.
                    int varsize = 0;
                    int const_nelem = 1;
                    for(int di=0; di<lm->dimensions->len; di++) {
                        lcm_dimension_t *dim = 
                            (lcm_dimension_t*) g_ptr_array_index(lm->dimensions, di);
                        if(dim->mode == LCM_VAR) {
                            varsize = 1;
                            break;
                        }
                        const_nelem *= atoi(dim->size);
                    }

                    // if the array dimensions are all constant, then
                    // we can precompute the array size here, and save a tiny
                    // bit of time at decode time
                    if(!varsize) {
                        int nbytes = const_nelem * bytes_per_elem;
                        emit(3, "obj.%s = %s(typecast(buf(off:off+%d), '%s'));",
                                lm->membername, 
                                swapbytes[swapind],
                                nbytes - 1, 
                                typename_to_matlab_type(lm->type->typename));
                        emit(3, "off = off + %d;", nbytes);
                    } else {
                        // at least one of the array dimensions is variable.  Therefore
                        // the total array size is variable and not known until decode time.
                        //
                        // emit code to calculate array size at decode time.
                        if(lm->dimensions->len > 1) {
                            emit_start(3, "nbytes = (");
                        } else {
                            emit_start(3, "nbytes = ");
                        }

                        for(int di=0; di<lm->dimensions->len; di++) {
                            lcm_dimension_t *dim = 
                                (lcm_dimension_t*) g_ptr_array_index(lm->dimensions, di);

                            if(dim->mode == LCM_CONST) {
                                emit_continue("%s", dim->size);
                            } else {
                                emit_continue("obj.%s", dim->size);
                            }
                            if(di < lm->dimensions->len - 1) 
                                emit_continue(" * ");
                        }

                        if(lm->dimensions->len > 1) {
                            if(bytes_per_elem > 1)
                                emit_end(") * %d;", bytes_per_elem);
                            else
                                emit_end(");");
                        } else {
                            if(bytes_per_elem > 1)
                                emit_end(" * %d;", bytes_per_elem);
                            else
                                emit_end(";");
                        }

                        emit(3, "obj.%s = %s(typecast(buf(off:off+nbytes-1), '%s'));",
                                lm->membername, swapbytes[swapind],
                                typename_to_matlab_type(lm->type->typename));
                        emit(3, "off = off + nbytes;");
                    }

                    // does the array need to be reshaped?
                    if(lm->dimensions->len > 1) {
                        emit_start(3, "obj.%s = reshape(obj.%s", lm->membername, lm->membername);
                        for(int di=0; di<lm->dimensions->len; di++) {
                            lcm_dimension_t *dim = 
                                (lcm_dimension_t*) g_ptr_array_index(lm->dimensions, di);

                            if(dim->mode == LCM_CONST) {
                                emit_continue(", %s", dim->size);
                            } else {
                                emit_continue(", obj.%s", dim->size);
                            }
                        }
                        emit_end(");");
                    }
                }
            } else {
                // handle string and struct types here.
                int is_string = !strcmp(lm->type->typename, "string");

                if(0 == lm->dimensions->len) {
                    // this is the easy case.  member is a single string or struct

                    if(is_string) {
                        emit(3, "nbytes = %s(typecast(buf(off:off+3), 'uint32'));", swapbytes[swapind]);
                        emit(3, "obj.%s = native2unicode(buf(off+4:off+2+nbytes), 'UTF-8')';",
                                lm->membername);
                        emit(3, "off = off + 4 + nbytes;");
                    } else {
                        emit(3, "[ obj.%s, bc ] = %s.decode_one(buf, off, isLittleEndian);", 
                                lm->membername, lm->type->typename);
                        emit(3, "off = off + bc;");
                    }
                } else {
                    // define a cell array to hold the data
                    emit_start(3, "obj.%s = cell(", lm->membername);

                    if(lm->dimensions->len == 1) {
                        lcm_dimension_t *dim = 
                            (lcm_dimension_t*) g_ptr_array_index(lm->dimensions, 0);
                        emit_end("%s%s, 1);", 
                                (dim->mode == LCM_CONST) ? "" : "obj.", 
                                dim->size);

                    } else {
                        for(int di=0; di<lm->dimensions->len; di++) {
                            lcm_dimension_t *dim = 
                                (lcm_dimension_t*) g_ptr_array_index(lm->dimensions, di);
                            emit_continue("%s%s", 
                                    (dim->mode == LCM_CONST) ? "" : "obj.", 
                                    dim->size);
                            if(di < lm->dimensions->len - 1) 
                                emit_continue(", ");
                        }
                        emit_end(");");
                    }

                    // now emit code to fill in the cell array
                    for(int di=0; di<lm->dimensions->len; di++) {
                        lcm_dimension_t *dim = 
                            (lcm_dimension_t*) g_ptr_array_index(lm->dimensions, di);

                        emit(3 + di, "for ind%d = 1:%s%s", 
                                di, 
                                (dim->mode == LCM_CONST) ? "" : "obj.", 
                                dim->size);

                        if(di == lm->dimensions->len - 1) {
                            if(is_string) {
                                emit(4 + di, "nbytes = %s(typecast(buf(off:off+3), 'uint32'));",
                                        swapbytes[swapind]);
                                emit(4 + di, "off = off + 4;");
                                emit_start(4 + di, "obj.%s{", lm->membername);
                            } else {
                                emit_start(4 + di, "[ obj.%s{", lm->membername);
                            }

                            for(int dj=0; dj<lm->dimensions->len; dj++) {
                                emit_continue("ind%d", dj);
                                if(dj < lm->dimensions->len - 1)
                                    emit_continue(", ");
                            }

                            if(is_string) {
                                emit_end("} = native2unicode(buf(off:off+nbytes-2), 'UTF-8')';");
                                emit(4 + di, "off = off + nbytes;");
                            }  else {
                                emit_end("}, bc] = %s.decode_one(buf, off, isLittleEndian);", lm->type->typename);
                                emit(4 + di, "off = off + bc;");
                            }
                        }

                        emit(3 + di, "end");
                    }
                }
            }
        }
        emit(2, "%s", swapendclause[swapind]);
    }

    emit(2, "bytes_consumed = off - start_off;");

    emit(1, "end\n");
}

static void
emit_matlab_decode(const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls)
{
    emit(1, "function obj=decode(data)");
    emit(1, "obj=[];");
    emit(2,     "if ~all(data(1:8) == %s.LCM_FINGERPRINT)", 
            ls->structname->typename);
    emit(3,         "error('Decode error: %s');", ls->structname->typename);
    emit(2,     "end");
    emit(2,     "[ctype, msize, endian] = computer;");
    emit(2,     "isLittleEndian = (endian == 'L');");
    emit(2,     "[obj, bytesconsumed] = %s.decode_one(data, 9, isLittleEndian);",
            ls->structname->typename);
    emit(1, "end\n");
}

static void
emit_matlab_fingerprint(const lcmgen_t *lcm, FILE *f, lcm_struct_t *ls)
{
    emit(1, "function hash=get_hash_recursive(parents)");
    emit(2,     "for i=1:length(parents)");
    emit(3,         "if strcmp(parents{i}, \'%s\')", ls->structname->typename);
    emit(4,             "hash=0;");
    emit(4,             "return");
    emit(3,         "end");
    emit(2,     "end");
    emit(2,     "");
    emit(2,     "newparents = {parents{:} '%s'};", ls->structname->typename);
    
    // fingerprint computation code is convoluted and ugly because MATLAB
    // integer arithmetic is saturating (no overflow), and because MATLAB
    // as of r2008 does not support 64-bit arithmetic.  sucktastic.
    emit_start(2, "hash = uint16([");
    for(int i=7; i>=0; i--) {
        emit_continue(" %d", (int)((ls->hash >> (i*8)) & 0xff));
    }
    emit_end(" ]);");

    emit(2, "mask = uint16(255);");

    for(unsigned int m = 0; m < ls->members->len; m++) {
        lcm_member_t *lm = g_ptr_array_index(ls->members, m);
        if(! lcm_is_primitive_type(lm->type->typename)) {
            emit(2, "carry = 0;");
            emit(2, "phash = %s.get_hash_recursive(newparents);", 
                    lm->type->typename);
            emit(2, "for i=1:8");
            emit(3,     "index=9-i;");
            emit(3,     "s=phash(index) + hash(index) + carry;");
            emit(3,     "hash(index) = bitand(s, mask);");
            emit(3,     "carry = bitshift(s, -8);");
            emit(2, "end");
        }
    }

    emit(2,     "carry=0;");
    emit(2,     "for i=1:8");
    emit(3,         "index=9-i;");
    emit(3,         "s=bitshift(hash(index), 1) + carry;");
    emit(3,         "hash(index) = bitand(s, mask);");
    emit(3,         "carry = bitshift(s, -8);");
    emit(2,     "end");
    emit(2,     "hash(8) = hash(8) + carry;");
    emit(1, "end");
    emit(1, "");
    emit(1, "function hash=get_hash()");
    emit(2,     "hash=%s.LCM_FINGERPRINT;", ls->structname->typename);
    emit(1, "end");
}

typedef struct {
    char *name;
    GPtrArray *structs;
} _package_contents_t;

static _package_contents_t * _package_contents_new(const char *name)
{
    _package_contents_t *pc = malloc(sizeof(_package_contents_t));
    pc->structs = g_ptr_array_new();
    pc->name = strdup(name);
    return pc;
}

static void _package_contents_free(_package_contents_t *pc)
{
    g_ptr_array_free(pc->structs, TRUE);
    free(pc->name);
    free(pc);
}

static int
emit_package(lcmgen_t *lcm, _package_contents_t *pc)
{
    // create the package directory, if necessary
    char **dirs = g_strsplit(pc->name, ".", 0);
    char *pdname = build_filenamev(dirs);
    char package_dir[PATH_MAX];
    char package_dir_prefix[PATH_MAX];
    int have_package = dirs[0] != NULL;

    sprintf(package_dir_prefix, "%s%s", getopt_get_string(lcm->gopt, "mpath"), 
            strlen(getopt_get_string(lcm->gopt, "mpath")) > 0 ? 
            G_DIR_SEPARATOR_S : "");
    sprintf(package_dir, "%s%s%s", package_dir_prefix, pdname,
            have_package ? G_DIR_SEPARATOR_S : "");
    free(pdname);
    if(strlen(package_dir)) {
        if(! g_file_test(package_dir, G_FILE_TEST_EXISTS)) {
            mkdir_with_parents(package_dir, 0755);
        }
        if(!g_file_test(package_dir, G_FILE_TEST_IS_DIR)) {
            err("Could not create directory %s\n", package_dir);
            return -1;
        }
    }

    ////////////////////////////////////////////////////////////
    // STRUCTS
    for(int i = 0; i<pc->structs->len; i++) {
        lcm_struct_t *ls = g_ptr_array_index(pc->structs, i);

        char path[PATH_MAX];
        sprintf(path, "%s%s.m", package_dir, ls->structname->shortname);

        FILE *f = fopen(path, "w");
        if(f==NULL) return -1;

        fprintf(f, "%% LCM type definition\n"
                "%% This file automatically generated by lcm-gen.\n"
                "%% DO NOT MODIFY BY HAND!!!!\n"
                "\n");

        fprintf(f, "classdef %s\n", ls->structname->shortname);

        emit_matlab_properties(lcm, f, ls);
        emit_matlab_constants(lcm, f, ls);

        emit(1, "methods\n");
        emit_matlab_encode(lcm, f, ls);
        emit_matlab_encode_one(lcm, f, ls);
        emit(1, "end\n");
        emit(1, "methods(Static)");
        emit_matlab_decode(lcm, f, ls);
        emit_matlab_decode_one(lcm, f, ls);
        emit_matlab_fingerprint(lcm, f, ls);
        emit(1, "end\n");

        fprintf(f, "end\n\n");
        fclose(f);
    }
    return 0;
}

int emit_matlab(lcmgen_t *lcm)
{
    GHashTable *packages = g_hash_table_new_full(g_str_hash, 
            g_str_equal, NULL, (GDestroyNotify)_package_contents_free);

    // group the structs by package
    for(unsigned int i = 0; i < lcm->structs->len; i++) {
        lcm_struct_t *ls = g_ptr_array_index(lcm->structs, i);
        _package_contents_t *pc = g_hash_table_lookup(packages, 
                ls->structname->package);
        if(!pc) {
            pc = _package_contents_new(ls->structname->package);
            g_hash_table_insert(packages, pc->name, pc);
        }
        g_ptr_array_add(pc->structs, ls);
    }

    GPtrArray *vals = _hash_table_get_vals(packages);

    for(int i=0; i<vals->len; i++) {
        _package_contents_t *pc = g_ptr_array_index(vals, i);
        int status = emit_package(lcm, pc); 
        if(0 != status) return status;
    }

    g_ptr_array_free(vals, TRUE);

    g_hash_table_destroy(packages);
    return 0;

}
