#include <ctype.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>
#ifndef WIN32
#include <unistd.h>
#endif
#include <assert.h>
#include <inttypes.h>
#include <stdarg.h>
#include <stdlib.h>
#include <sys/stat.h>
#include <sys/types.h>
#include "../lcm/lcm_version.h"
#include "lcmgen.h"

#ifdef WIN32
#include <lcm/windows/WinPorting.h>
#endif

#include "getopt.h"
#include "tokenize.h"

void setup_c_options(getopt_t *gopt);
int emit_c(lcmgen_t *lcm);

void setup_java_options(getopt_t *gopt);
int emit_java(lcmgen_t *lcm);

void setup_python_options(getopt_t *gopt);
int emit_python(lcmgen_t *lcm);

void setup_lua_options(getopt_t *gopt);
int emit_lua(lcmgen_t *lcm);

void setup_csharp_options(getopt_t *gopt);
int emit_csharp(lcmgen_t *lcm);

void setup_go_options(getopt_t *gopt);
int emit_go(lcmgen_t *lcm);

void setup_cpp_options(getopt_t *gopt);
int emit_cpp(lcmgen_t *lcm);

int main(int argc, char *argv[])
{
    getopt_t *gopt = getopt_create();

    // clang-format off
    getopt_add_bool(gopt, 'h', "help",     0, "Show this help");
    getopt_add_bool(gopt, 't', "tokenize", 0, "Show tokenization");
    getopt_add_bool(gopt, 'd', "debug",    0, "Show parsed file");
    getopt_add_bool(gopt,   0, "lazy",     0, "Generate output file only if .lcm is newer");
    getopt_add_bool(gopt,   0, "use-quotes-for-includes", 0,
        "Use quotes instead of angular brackets for including header files");
    getopt_add_string(gopt, 0, "package-prefix", "",
        "Add this package name as a prefix to the declared package");
    getopt_add_bool(gopt,   0, "version",  0, "Show version information and exit");

    getopt_add_spacer(gopt, "**** C options ****");
    getopt_add_bool(gopt, 'c', "c",        0, "Emit C code");
    setup_c_options(gopt);

    getopt_add_spacer(gopt, "**** C++ options ****");
    getopt_add_bool(gopt, 'x', "cpp",      0, "Emit C++ code");
    setup_cpp_options(gopt);

    getopt_add_spacer(gopt, "**** Java options ****");
    getopt_add_bool(gopt, 'j', "java",     0, "Emit Java code");
    setup_java_options(gopt);

    getopt_add_spacer(gopt, "**** Python options ****");
    getopt_add_bool(gopt, 'p', "python",   0, "Emit Python code");
    setup_python_options(gopt);

    getopt_add_spacer(gopt, "**** Lua options ****");
    getopt_add_bool(gopt, 'l', "lua",      0, "Emit Lua code");
    setup_lua_options(gopt);

    getopt_add_spacer(gopt, "**** C#.NET options ****");
    getopt_add_bool(gopt, 0, "csharp",     0, "Emit C#.NET code");
    setup_csharp_options(gopt);

    getopt_add_spacer(gopt, "**** Go options ****");
    getopt_add_bool(gopt, 'g', "go",       0, "Emit Go code");
    setup_go_options(gopt);
    // clang-format on

    if (!getopt_parse(gopt, argc, argv, 1) || getopt_get_bool(gopt, "help")) {
        printf("Usage: %s [options] <input files>\n\n", argv[0]);
        getopt_do_usage(gopt);
        return 0;
    }

    lcmgen_t *lcm = lcmgen_create();
    lcm->gopt = gopt;

    for (unsigned int i = 0; i < g_ptr_array_size(gopt->extraargs); i++) {
        char *path = (char *) g_ptr_array_index(gopt->extraargs, i);

        int res = lcmgen_handle_file(lcm, path);
        if (res)
            return res;
    }

    // If "--version" was specified, then show version information and exit.
    if (getopt_get_bool(gopt, "version")) {
        printf("lcm-gen %d.%d.%d\n", LCM_MAJOR_VERSION, LCM_MINOR_VERSION, LCM_MICRO_VERSION);
        return 0;
    }

    // If "-t" or "--tokenize" was specified, then show tokenization
    // information and exit.
    if (getopt_get_bool(gopt, "tokenize")) {
        return 0;
    }

    int did_something = 0;
    int res = 0;
    if (getopt_get_bool(gopt, "debug")) {
        did_something = 1;
        lcmgen_dump(lcm);
    }

    if (getopt_get_bool(gopt, "c")) {
        did_something = 1;
        if (emit_c(lcm)) {
            printf("An error occurred while emitting C code.\n");
            res = -1;
        }
    }

    if (getopt_get_bool(gopt, "cpp")) {
        did_something = 1;
        if (emit_cpp(lcm)) {
            printf("An error occurred while emitting C++ code.\n");
            res = -1;
        }
    }

    if (getopt_get_bool(gopt, "java")) {
        did_something = 1;
        if (emit_java(lcm)) {
            perror("An error occurred while emitting Java code.\n");
            res = -1;
        }
    }

    if (getopt_get_bool(gopt, "python")) {
        did_something = 1;
        if (emit_python(lcm)) {
            printf("An error occurred while emitting Python code.\n");
            res = -1;
        }
    }

    if (getopt_get_bool(gopt, "lua")) {
        did_something = 1;
        if (emit_lua(lcm)) {
            printf("An error occurred while emitting Lua code.\n");
            res = -1;
        }
    }

    if (getopt_get_bool(gopt, "csharp")) {
        did_something = 1;
        if (emit_csharp(lcm)) {
            printf("An error occurred while emitting C#.NET code.\n");
            res = -1;
        }
    }

    if (getopt_get_bool(gopt, "go")) {
        did_something = 1;
        if (emit_go(lcm)) {
            printf("An error occurred while emitting Go code.\n");
            res = -1;
        }
    }

    if (did_something == 0) {
        printf("No actions specified. Try --help.\n");
        res = -1;
    }

    return res;
}
