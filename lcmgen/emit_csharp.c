#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#ifdef WIN32
#define __STDC_FORMAT_MACROS
#endif
#include <inttypes.h>
#include <assert.h>

#include <sys/stat.h>
#include <sys/types.h>

#include "lcmgen.h"
#include "sprintfalloc.h"
#include "getopt.h"

#ifdef WIN32
#include <lcm/windows/WinPorting.h>
#endif

#define INDENT(n) (4*(n))

#define emit_start(n, ...) do { fprintf(f, "%*s", INDENT(n), ""); fprintf(f, __VA_ARGS__); } while (0)
#define emit_continue(...) do { fprintf(f, __VA_ARGS__); } while (0)
#define emit_end(...) do { fprintf(f, __VA_ARGS__); fprintf(f, "\n"); } while (0)
#define emit(n, ...) do { fprintf(f, "%*s", INDENT(n), ""); fprintf(f, __VA_ARGS__); fprintf(f, "\n"); } while (0)

static char *dots_to_slashes(const char *s)
{
    char *p = strdup(s);

    for (char *t=p; *t!=0; t++)
        if (*t == '.')
            *t = G_DIR_SEPARATOR;

    return p;
}

static void make_dirs_for_file(const char *path)
{
#ifdef WIN32
    char *dirname = g_path_get_dirname(path);
    g_mkdir_with_parents(dirname, 0755);
    g_free(dirname);
#else
    int len = strlen(path);
    for (int i = 0; i < len; i++) {
        if (path[i]=='/') {
            char *dirpath = (char *) malloc(i+1);
            strncpy(dirpath, path, i);
            dirpath[i]=0;

            mkdir(dirpath, 0755);
            free(dirpath);

            i++; // skip the '/'
        }
    }
#endif
}

void setup_csharp_options(getopt_t *gopt)
{
    getopt_add_string(gopt, 0,   "csharp-path",        "",                       "C#.NET file destination directory");
    getopt_add_bool(gopt, 0,     "csharp-mkdir",       1,                        "Make C#.NET source directories automatically");
    getopt_add_bool(gopt, 0,     "csharp-strip-dirs",  0,						 "Do not generate folders for default and root namespace");
    getopt_add_string(gopt, 0,   "csharp-decl",        ": LCM.LCM.LCMEncodable", "String added to class declarations");
    getopt_add_string(gopt, 0,   "csharp-root-nsp",    "",						 "Root C#.NET namespace (wrapper) added before LCM package");
    getopt_add_string(gopt, 0,   "csharp-default-nsp", "LCMTypes",               "Default C#.NET namespace if LCM type has no package");
}

typedef struct
{
    char *storage;
    char *decode;
    char *encode;
} primitive_info_t;

static primitive_info_t *prim(char *storage, char *decode, char *encode)
{
    primitive_info_t *p = (primitive_info_t*) calloc(sizeof(primitive_info_t), 1);
    p->storage = storage;
    p->decode = decode;
    p->encode = encode;

    return p;
}

static int ndefaultpkg_warned = 0;

const char *make_fqn_csharp(lcmgen_t *lcm, const char *type_name)
{
	char *root_nsp = getopt_get_string(lcm->gopt, "csharp-root-nsp");

    if (strchr(type_name, '.') != NULL)
        return sprintfalloc("%s%s%s", root_nsp, (root_nsp[0] == 0 ? "" : "."), type_name);

    if (!ndefaultpkg_warned && !getopt_was_specified(lcm->gopt, "csharp-default-nsp")) {
        printf("Notice: enclosing LCM types without package into C#.NET namespace '%s'.\n", getopt_get_string(lcm->gopt, "csharp-default-nsp"));
        ndefaultpkg_warned = 1;
    }

	char *def_nsp = getopt_get_string(lcm->gopt, "csharp-default-nsp");
	if (strlen(def_nsp) > 0)
		return sprintfalloc("%s%s%s.%s", root_nsp, (root_nsp[0] == 0 ? "" : "."), getopt_get_string(lcm->gopt, "csharp-default-nsp"), type_name);
	else
		return sprintfalloc("%s.%s", root_nsp, type_name);
}

/** # -> replace1
    @ -> replace2
**/
static void freplace(FILE *f, const char *haystack, const char *replace1)
{
    int len = strlen(haystack);
    
    for (int pos = 0; pos < len; pos++)
    {
        if (haystack[pos]=='#')
            fprintf(f, "%s", replace1);
        else
            fprintf(f, "%c", haystack[pos]);
    }
}

static void make_accessor(lcm_member_t *lm, const char *obj, char *s)
{
    int ndim = g_ptr_array_size(lm->dimensions);
    int pos = 0;
    s[0] = 0;

    pos += sprintf(s, "%s%s%s", obj, obj[0]==0 ? "" : ".", lm->membername);
	if (ndim > 0)
	{
		pos += sprintf(&s[pos], "[");
		for (int d = 0 ; d < ndim; d++)
		{
			pos += sprintf(&s[pos],"%c", 'a'+d);
			if (d < (ndim-1))
				pos += sprintf(&s[pos], ",");
		}
		pos += sprintf(&s[pos], "]");
	}
}

static int struct_has_string_member(lcm_struct_t *lr) 
{
    for (unsigned int member = 0; member < g_ptr_array_size(lr->members); member++) {
        lcm_member_t *lm = (lcm_member_t *) g_ptr_array_index(lr->members, member);
        if(!strcmp("string", lm->type->lctypename))
            return 1;
    }
    return 0;
}

static const char * dim_size_prefix(const char *dim_size) {
    char *eptr = NULL;
    long asdf = strtol(dim_size, &eptr, 0);
    (void) asdf;  // suppress compiler warnings
    if(*eptr == '\0')
        return "";
    else
        return "this.";
}

int emit_csharp(lcmgen_t *lcm)
{
    GHashTable *type_table = g_hash_table_new(g_str_hash, g_str_equal);

    g_hash_table_insert(type_table, "byte",   prim("byte",  
                                             "# = ins.ReadByte();",  
                                             "outs.Write(#);"));
    g_hash_table_insert(type_table, "int8_t",   prim("byte",  
                                               "# = ins.ReadByte();",  
                                               "outs.Write(#);"));
    g_hash_table_insert(type_table, "int16_t",  prim("short", 
                                               "# = ins.ReadInt16();", 
                                               "outs.Write(#);"));
    g_hash_table_insert(type_table, "int32_t",  prim("int",   
                                               "# = ins.ReadInt32();",
                                               "outs.Write(#);"));
    g_hash_table_insert(type_table, "int64_t",  prim("long",  
                                               "# = ins.ReadInt64();",  
                                               "outs.Write(#);"));
    g_hash_table_insert(type_table, "string",   prim("String",
                                               "__strbuf = new byte[ins.ReadInt32()-1]; ins.ReadFully(__strbuf); ins.ReadByte(); # = System.Text.Encoding.GetEncoding(\"US-ASCII\").GetString(__strbuf);",
                                               "__strbuf = System.Text.Encoding.GetEncoding(\"US-ASCII\").GetBytes(#); outs.Write(__strbuf.Length+1); outs.Write(__strbuf, 0, __strbuf.Length); outs.Write((byte) 0);"));
    g_hash_table_insert(type_table, "boolean",  prim("bool",
                                               "# = ins.ReadBoolean();",
                                               "outs.Write(#);"));
    g_hash_table_insert(type_table, "float",    prim("float",
                                               "# = ins.ReadSingle();",
                                               "outs.Write(#);"));
    g_hash_table_insert(type_table, "double",   prim("double",
                                               "# = ins.ReadDouble();",
                                               "outs.Write(#);"));
    
    //////////////////////////////////////////////////////////////
    // ENUMS
    for (unsigned int en = 0; en < g_ptr_array_size(lcm->enums); en++) {
        lcm_enum_t *le = (lcm_enum_t *) g_ptr_array_index(lcm->enums, en);
        
        const char *classname = make_fqn_csharp(lcm, le->enumname->lctypename);
        char *path = sprintfalloc("%s%s%s.cs", 
                                  getopt_get_string(lcm->gopt, "csharp-path"),
                                  strlen(getopt_get_string(lcm->gopt, "csharp-path")) > 0 ? G_DIR_SEPARATOR_S : "",
								  dots_to_slashes((getopt_get_bool(lcm->gopt, "csharp-strip-dirs") ? le->enumname->lctypename : classname)));

        if (!lcm_needs_generation(lcm, le->lcmfile, path))
            continue;

        if (getopt_get_bool(lcm->gopt, "csharp-mkdir"))
            make_dirs_for_file(path);

        FILE *f = fopen(path, "w");
        if (f==NULL)
            return -1;

        emit(0, "using System;");
        emit(0, "using System.Collections.Generic;");
        emit(0, "using System.IO;");
        emit(0, "using LCM.LCM;");
        emit(0, " ");

		char *root_nsp = getopt_get_string(lcm->gopt, "csharp-root-nsp");
        if (strlen(le->enumname->package) > 0)
            emit(0, "namespace %s%s%s", root_nsp, (root_nsp[0] == 0 ? "" : "."), le->enumname->package);
        else
		{
			char *def_nsp = getopt_get_string(lcm->gopt, "csharp-default-nsp");
			if (strlen(def_nsp) > 0)
				emit(0, "namespace %s%s%s", root_nsp, (root_nsp[0] == 0 ? "" : "."), def_nsp);
			else
				emit(0, "namespace %s", root_nsp);
		}

        emit(0, "{");
        emit(1, "public sealed class %s %s", le->enumname->shortname, getopt_get_string(lcm->gopt, "csharp-decl"));

        emit(1, "{");
        emit(2, "public int value;");
        emit(0, " ");

        for (unsigned int v = 0; v < g_ptr_array_size(le->values); v++) {
            lcm_enum_value_t *lev = (lcm_enum_value_t *) g_ptr_array_index(le->values, v);
            emit(2, "public const int %-16s = %i;", lev->valuename, lev->value);
        }
        emit(0," ");

        emit(2,"public %s(int value) { this.value = value; }", 
                le->enumname->shortname);
        emit(0," ");

        emit(2,"public int getValue() { return value; }");
        emit(0," ");

        emit(2,"public void _encodeRecursive(LCMDataOutputStream outs)");
        emit(2,"{");
        emit(3,"outs.WriteInt(this.value);");
        emit(2,"}");
        emit(0," ");

        emit(2,"public void Encode(LCMDataOutputStream outs)");
        emit(2,"{");
        emit(3,"outs.Write((long) LCM_FINGERPRINT);");
        emit(3,"_encodeRecursive(outs);");
        emit(2,"}");
        emit(0," ");

        emit(2,"public static %s _decodeRecursiveFactory(LCMDataInputStream ins)", make_fqn_csharp(lcm, le->enumname->lctypename));
        emit(2,"{");
        emit(3,"%s o = new %s(0);", make_fqn_csharp(lcm, le->enumname->lctypename), make_fqn_csharp(lcm, le->enumname->lctypename));
        emit(3,"o._decodeRecursive(ins);");
        emit(3,"return o;");
        emit(2,"}");
        emit(0," ");

        emit(2,"public void _decodeRecursive(LCMDataInputStream ins)");
        emit(2,"{");
        emit(3,"this.value = ins.ReadInt();");
        emit(2,"}");
        emit(0," ");

        emit(2,"public %s(LCMDataInputStream ins)", le->enumname->shortname);
        emit(2,"{");
        emit(3,"ulong hash = (ulong) ins.ReadInt64();");
        emit(3,"if (hash != LCM_FINGERPRINT)");
        emit(4,     "throw new System.IO.IOException(\"LCM Decode error: bad fingerprint\");");
        emit(3,"_decodeRecursive(ins);");
        emit(2,"}");
        emit(0," ");

        emit(2,"public %s Copy()", classname);
        emit(2,"{");
        emit(3,"return new %s(this.value);", classname);
        emit(2,"}");
        emit(0," ");

        emit(2,"public const ulong _hashRecursive(List<String> clss)");
        emit(2,"{");
        emit(3,"return LCM_FINGERPRINT;");
        emit(2,"}");
        emit(0," ");
        emit(2, "public static const ulong LCM_FINGERPRINT = 0x%016"PRIx64"L;", le->hash);
        emit(1, "}");
        emit(0, "}");
        fclose(f);
    }
    
    for (unsigned int st = 0; st < g_ptr_array_size(lcm->structs); st++) {
        lcm_struct_t *lr = (lcm_struct_t *) g_ptr_array_index(lcm->structs, st);

        const char *classname = make_fqn_csharp(lcm, lr->structname->lctypename);
        char *path = sprintfalloc("%s%s%s.cs", 
                                  getopt_get_string(lcm->gopt, "csharp-path"), 
                                  strlen(getopt_get_string(lcm->gopt, "csharp-path")) > 0 ? "/" : "",
								  dots_to_slashes((getopt_get_bool(lcm->gopt, "csharp-strip-dirs") ? lr->structname->lctypename : classname)));

        if (!lcm_needs_generation(lcm, lr->lcmfile, path))
            continue;

        if (getopt_get_bool(lcm->gopt, "csharp-mkdir"))
            make_dirs_for_file(path);

        FILE *f = fopen(path, "w");
        if (f==NULL)
            return -1;

        emit(0, "/* LCM type definition class file\n"
                " * This file was automatically generated by lcm-gen\n"
                " * DO NOT MODIFY BY HAND!!!!\n"
                " */\n");
        
        emit(0, "using System;");
        emit(0, "using System.Collections.Generic;");
        emit(0, "using System.IO;");
        emit(0, "using LCM.LCM;");
        emit(0, " ");
        
		char *root_nsp = getopt_get_string(lcm->gopt, "csharp-root-nsp");
        if (strlen(lr->structname->package) > 0)
            emit(0, "namespace %s%s%s", root_nsp, (root_nsp[0] == 0 ? "" : "."), lr->structname->package);
        else
		{
			char *def_nsp = getopt_get_string(lcm->gopt, "csharp-default-nsp");
			if (strlen(def_nsp) > 0)
				emit(0, "namespace %s%s%s", root_nsp, (root_nsp[0] == 0 ? "" : "."), def_nsp);
			else
				emit(0, "namespace %s", root_nsp);
		}

        emit(0, "{");
        emit(1, "public sealed class %s %s", lr->structname->shortname, getopt_get_string(lcm->gopt, "csharp-decl"));
        emit(1, "{");

        for (unsigned int member = 0; member < g_ptr_array_size(lr->members); member++) {
            lcm_member_t *lm = (lcm_member_t *) g_ptr_array_index(lr->members, member);
            primitive_info_t *pinfo = (primitive_info_t*) g_hash_table_lookup(type_table, lm->type->lctypename);

            emit_start(2, "public ");
            
            if (pinfo==NULL)  {
                emit_continue("%s", make_fqn_csharp(lcm, lm->type->lctypename));
            } else {
                emit_continue("%s", pinfo->storage);
            }

			if (g_ptr_array_size(lm->dimensions) > 0)
			{
				emit_continue("[");
				for (unsigned int i = 0; i < (g_ptr_array_size(lm->dimensions)-1); i++)
					emit_continue(",");
				emit_continue("]");
			}
            emit_continue(" %s", lm->membername);
            emit_end(";");
        }
        emit(0," ");

        // public constructor
        emit(2,"public %s()", lr->structname->shortname);
        emit(2,"{");

        // pre-allocate any fixed-size arrays.
        for (unsigned int member = 0; member < g_ptr_array_size(lr->members); member++) {
            lcm_member_t *lm = (lcm_member_t *) g_ptr_array_index(lr->members, member);
            primitive_info_t *pinfo = (primitive_info_t*) g_hash_table_lookup(type_table, lm->type->lctypename);

            if (g_ptr_array_size(lm->dimensions)==0 || !lcm_is_constant_size_array(lm))
                continue;

            emit_start(3, "%s = new ", lm->membername);
            if (pinfo != NULL)
                emit_continue("%s", pinfo->storage);
            else 
                emit_continue("%s", make_fqn_csharp(lcm, lm->type->lctypename));
      
			emit_continue("[");
            for (unsigned int i = 0; i < g_ptr_array_size(lm->dimensions); i++) {
                lcm_dimension_t *dim = (lcm_dimension_t*) g_ptr_array_index(lm->dimensions, i);
                emit_continue("%s", dim->size);
				if (i < (g_ptr_array_size(lm->dimensions)-1))
					emit_continue(",");
            }
            emit_end("];");
        }
        emit(2,"}");
        emit(0," ");

        emit(2, "public static readonly ulong LCM_FINGERPRINT;");
        emit(2, "public static readonly ulong LCM_FINGERPRINT_BASE = 0x%016"PRIx64"L;", lr->hash);
        emit(0," ");

        //////////////////////////////////////////////////////////////
        // CONSTANTS
        for (unsigned int cn = 0; cn < g_ptr_array_size(lr->constants); cn++) {
            lcm_constant_t *lc = (lcm_constant_t *) g_ptr_array_index(lr->constants, cn);
            assert(lcm_is_legal_const_type(lc->lctypename));

            if (!strcmp(lc->lctypename, "int8_t") ||
                !strcmp(lc->lctypename, "int16_t") ||
                !strcmp(lc->lctypename, "int32_t")) {
                emit(2, "public const int %s = %s;", lc->membername, lc->val_str);
            } else if (!strcmp(lc->lctypename, "int64_t")) {
                emit(2, "public const long %s = %sL;", lc->membername, lc->val_str);
            } else if (!strcmp(lc->lctypename, "float")) {
                emit(2, "public const float %s = %s;", lc->membername, lc->val_str);
            } else if (!strcmp(lc->lctypename, "double")) {
                emit(2, "public const double %s = %s;", lc->membername, lc->val_str);
            } else {
                assert(0);
            }
        }
        if (g_ptr_array_size(lr->constants) > 0)
            emit(0, "");

        ///////////////// encode //////////////////
        emit(2, "static %s()",  lr->structname->shortname);
        emit(2, "{");
        emit(3, "LCM_FINGERPRINT = _hashRecursive(new List<String>());");
        emit(2, "}");
        emit(0, " ");

        emit(2, "public static ulong _hashRecursive(List<String> classes)");
        emit(2, "{");
        emit(3, "if (classes.Contains(\"%s\"))", make_fqn_csharp(lcm, lr->structname->lctypename));
        emit(4,     "return 0L;");
        emit(0, " ");
        emit(3, "classes.Add(\"%s\");", make_fqn_csharp(lcm, lr->structname->lctypename));

        emit(3, "ulong hash = LCM_FINGERPRINT_BASE");
        for (unsigned int member = 0; member < g_ptr_array_size(lr->members); member++) {
            lcm_member_t *lm = (lcm_member_t *) g_ptr_array_index(lr->members, member);
            primitive_info_t *pinfo = (primitive_info_t*) g_hash_table_lookup(type_table, lm->type->lctypename);

            if (pinfo)
                continue;

            emit(4, " + %s._hashRecursive(classes)", make_fqn_csharp(lcm, lm->type->lctypename));
        }
        emit(4,";");

        emit(3, "classes.RemoveAt(classes.Count - 1);");
        emit(3, "return (hash<<1) + ((hash>>63)&1);");

        emit(2, "}");
        emit(0, " ");

        ///////////////// encode //////////////////

        emit(2,"public void Encode(LCMDataOutputStream outs)");
        emit(2,"{");
        emit(3,"outs.Write((long) LCM_FINGERPRINT);");
        emit(3,"_encodeRecursive(outs);");
        emit(2,"}");
        emit(0," ");

        emit(2,"public void _encodeRecursive(LCMDataOutputStream outs)");
        emit(2,"{");
        if(struct_has_string_member(lr))
            emit(3, "byte[] __strbuf = null;");
        char accessor[1024];

        for (unsigned int member = 0; member < g_ptr_array_size(lr->members); member++) {
            lcm_member_t *lm = (lcm_member_t *) g_ptr_array_index(lr->members, member);
            primitive_info_t *pinfo = (primitive_info_t*) g_hash_table_lookup(type_table, lm->type->lctypename);
            make_accessor(lm, "this", accessor);

            for (unsigned int i = 0; i < g_ptr_array_size(lm->dimensions); i++) {
                lcm_dimension_t *dim = (lcm_dimension_t*) g_ptr_array_index(lm->dimensions, i);
                emit(3+i, "for (int %c = 0; %c < %s%s; %c++) {", 
                        'a'+i, 'a'+i, dim_size_prefix(dim->size), dim->size, 'a'+i);
            }
            
            emit_start(3 + g_ptr_array_size(lm->dimensions),"");
            if (pinfo != NULL)
                freplace(f, pinfo->encode, accessor);
            else
                freplace(f, "#._encodeRecursive(outs);", accessor);
            emit_end(" ");                

            for (unsigned int i = 0; i < g_ptr_array_size(lm->dimensions); i++) {
                emit(3 + g_ptr_array_size(lm->dimensions) - i - 1, "}");
            }
            emit(0," ");
        }
        emit(2,"}");
        emit(0," ");

        ///////////////// decode //////////////////

        // decoding constructors
        emit(2, "public %s(byte[] data) : this(new LCMDataInputStream(data))", lr->structname->shortname);
        emit(2, "{");
        emit(2, "}");
        emit(0, " ");

        emit(2,"public %s(LCMDataInputStream ins)", lr->structname->shortname);
        emit(2,"{");
        emit(3,"if ((ulong) ins.ReadInt64() != LCM_FINGERPRINT)");
        emit(4,     "throw new System.IO.IOException(\"LCM Decode error: bad fingerprint\");");
        emit(0," ");
        emit(3,"_decodeRecursive(ins);");
        emit(2,"}");
        emit(0," ");

        emit(2,"public static %s _decodeRecursiveFactory(LCMDataInputStream ins)", make_fqn_csharp(lcm, lr->structname->lctypename));
        emit(2,"{");
        emit(3,"%s o = new %s();", make_fqn_csharp(lcm, lr->structname->lctypename), make_fqn_csharp(lcm, lr->structname->lctypename));
        emit(3,"o._decodeRecursive(ins);");
        emit(3,"return o;");
        emit(2,"}");
        emit(0," ");

        emit(2,"public void _decodeRecursive(LCMDataInputStream ins)");
        emit(2,"{");
        if(struct_has_string_member(lr))
            emit(3, "byte[] __strbuf = null;");
        for (unsigned int member = 0; member < g_ptr_array_size(lr->members); member++) {
            lcm_member_t *lm = (lcm_member_t *) g_ptr_array_index(lr->members, member);
            primitive_info_t *pinfo = (primitive_info_t*) g_hash_table_lookup(type_table, lm->type->lctypename);

            make_accessor(lm, "this", accessor);

            // allocate an array if necessary
            if (g_ptr_array_size(lm->dimensions) > 0) {

                emit_start(3, "this.%s = new ", lm->membername);

                if (pinfo != NULL)
                    emit_continue("%s", pinfo->storage);
                else
                    emit_continue("%s", make_fqn_csharp(lcm, lm->type->lctypename));

				if (g_ptr_array_size(lm->dimensions) > 0)
				{
					emit_continue("[");
					for (unsigned int i = 0; i < g_ptr_array_size(lm->dimensions); i++) {
						lcm_dimension_t *dim = (lcm_dimension_t*) g_ptr_array_index(lm->dimensions, i);
						emit_continue("(int) %s", dim->size);
						if (i < (g_ptr_array_size(lm->dimensions)-1))
							emit_continue(",");
					}
					emit_continue("]");
				}
                emit_end(";");
            }

            for (unsigned int i = 0; i < g_ptr_array_size(lm->dimensions); i++) {
                lcm_dimension_t *dim = (lcm_dimension_t*) g_ptr_array_index(lm->dimensions, i);
                emit(3+i, "for (int %c = 0; %c < %s%s; %c++) {", 
                        'a'+i, 'a'+i, dim_size_prefix(dim->size), dim->size, 'a'+i);
            }
            
            emit_start(3 + g_ptr_array_size(lm->dimensions),"");
            if (pinfo != NULL) 
                freplace(f, pinfo->decode, accessor);
            else {
                emit_continue("%s = %s._decodeRecursiveFactory(ins);", accessor, make_fqn_csharp(lcm, lm->type->lctypename));
            }
            emit_end("");

            for (unsigned int i = 0; i < g_ptr_array_size(lm->dimensions); i++) {
                emit(3 + g_ptr_array_size(lm->dimensions) - i - 1, "}");
            }

            emit(0," ");
        }

        emit(2,"}");
        emit(0," ");


        ///////////////// copy //////////////////

        emit(2,"public %s Copy()", classname);
        emit(2,"{");
        emit(3,"%s outobj = new %s();", classname, classname);

        for (unsigned int member = 0; member < g_ptr_array_size(lr->members); member++) {
            lcm_member_t *lm = (lcm_member_t *) g_ptr_array_index(lr->members, member);
            primitive_info_t *pinfo = (primitive_info_t*) g_hash_table_lookup(type_table, lm->type->lctypename);
            make_accessor(lm, "", accessor);

            // allocate an array if necessary
            if (g_ptr_array_size(lm->dimensions) > 0) {

                emit_start(3, "outobj.%s = new ", lm->membername);

                if (pinfo != NULL)
                    emit_continue("%s", pinfo->storage);
                else
                    emit_continue("%s", make_fqn_csharp(lcm, lm->type->lctypename));

				if (g_ptr_array_size(lm->dimensions) > 0)
				{
					emit_continue("[");
					for (unsigned int i = 0; i < g_ptr_array_size(lm->dimensions); i++) {
						lcm_dimension_t *dim = (lcm_dimension_t*) g_ptr_array_index(lm->dimensions, i);
						emit_continue("(int) %s", dim->size);
						if (i < (g_ptr_array_size(lm->dimensions)-1))
							emit_continue(",");
					}
					emit_continue("]");
				}
                emit_end(";");
            }


            for (unsigned int i = 0; i < g_ptr_array_size(lm->dimensions); i++) {
                lcm_dimension_t *dim = (lcm_dimension_t*) g_ptr_array_index(lm->dimensions, i);
                emit(3+i, "for (int %c = 0; %c < %s%s; %c++) {", 
                        'a'+i, 'a'+i, dim_size_prefix(dim->size), dim->size, 'a'+i);
            }
            
            if (pinfo != NULL) {

				emit_start(3+g_ptr_array_size(lm->dimensions), "outobj.%s", lm->membername);
				if (g_ptr_array_size(lm->dimensions) > 0)
				{
					emit_continue("[");
					for (unsigned int i = 0; i < g_ptr_array_size(lm->dimensions); i++) {
						emit_continue("%c", 'a'+i);
						if (i < (g_ptr_array_size(lm->dimensions)-1))
							emit_continue(",");
					}
					emit_continue("]");
				}

                emit_continue(" = this.%s", lm->membername);
				if (g_ptr_array_size(lm->dimensions) > 0)
				{
					emit_continue("[");
					for (unsigned int i = 0; i < g_ptr_array_size(lm->dimensions); i++) {
						emit_continue("%c", 'a'+i);
						if (i < (g_ptr_array_size(lm->dimensions)-1))
							emit_continue(",");
					}
					emit_continue("]");
				}

                emit_end(";");
                
            } else {
                emit(3 + g_ptr_array_size(lm->dimensions), "outobj.%s = this.%s.Copy();", accessor, accessor);
            }

            for (unsigned int i = 0; i < g_ptr_array_size(lm->dimensions); i++) {
                emit(3 + g_ptr_array_size(lm->dimensions) - i - 1, "}");
            }

            emit(0," ");
        }

        emit(3,"return outobj;");
        emit(2,"}");

        ////////
        emit(1, "}");
        emit(0, "}\n");
        fclose(f);
    }
 
/* XXX unfinished
   
    hashtable_iterator_t *hit = hashtable_iterator_create(type_table);
    hashtable_entry_t *entry;
    while ((entry = hashtable_iterator_next(hit)) != NULL) {
        free((char*) entry->value);
    }
    hashtable_iterator_destroy(hit);

    hashtable_destroy(type_table);
*/
    return 0;
}
