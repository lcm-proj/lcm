#ifndef DBG_H
#define DBG_H

#ifdef __cplusplus
extern "C" {
#endif

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdarg.h>

#define _NORMAL_    "\x1b[0m"
#define _BLACK_     "\x1b[30;47m"
#define _RED_       "\x1b[31;40m"
#define _GREEN_     "\x1b[32;40m"
#define _YELLOW_    "\x1b[33;40m"
#define _BLUE_      "\x1b[34;40m"
#define _MAGENTA_   "\x1b[35;40m"
#define _CYAN_      "\x1b[36;40m"
#define _WHITE_     "\x1b[37;40m"

#define _BRED_      "\x1b[1;31;40m"
#define _BGREEN_    "\x1b[1;32;40m"
#define _BYELLOW_   "\x1b[1;33;40m"
#define _BBLUE_     "\x1b[1;34;40m"
#define _BMAGENTA_  "\x1b[1;35;40m"
#define _BCYAN_     "\x1b[1;36;40m"
#define _BWHITE_    "\x1b[1;37;40m"

#define DBG_MODE(x) (1ULL << (x))

#define DBG_ALL     (~0ULL)
#define DBG_ERROR   DBG_MODE(0)
#define DBG_DEFAULT DBG_ERROR

// ================== add debugging modes here ======================

#define DBG_TEST    DBG_MODE(1) /* foo */
#define DBG_LCM      DBG_MODE(2)
#define DBG_LCM_MSG  DBG_MODE(3)
#define DBG_MAIN    DBG_MODE(4)
#define DBG_5       DBG_MODE(5)
#define DBG_6       DBG_MODE(6)
#define DBG_7       DBG_MODE(7)
#define DBG_8       DBG_MODE(8)
#define DBG_9       DBG_MODE(9)
#define DBG_10      DBG_MODE(10)
#define DBG_11      DBG_MODE(11)
#define DBG_12      DBG_MODE(12)
#define DBG_13      DBG_MODE(13)
#define DBG_14      DBG_MODE(14)
#define DBG_15      DBG_MODE(15)
#define DBG_16      DBG_MODE(16)

/// There can be no white space in these strings

#define DBG_NAMETAB \
{ "all", DBG_ALL }, \
{ "error", DBG_ERROR }, \
{ "test", DBG_TEST }, \
{ "lc", DBG_LCM }, \
{ "lc_msg", DBG_LCM_MSG }, \
{ "main", DBG_MAIN }, \
{ "5", DBG_5 }, \
{ "6", DBG_6 }, \
{ "7", DBG_7 }, \
{ "8",  DBG_8   }, \
{ "9",  DBG_9 }, \
{ "10", DBG_10 }, \
{ "11", DBG_11 }, \
{ "12", DBG_12 }, \
{ "13", DBG_13 }, \
{ "14", DBG_14 }, \
{ "15", DBG_15 }, \
{ "16", DBG_16 }, \
{ NULL,     0 } 

#define DBG_COLORTAB \
{ DBG_TEST, _CYAN_ }, \
{ DBG_LCM, _RED_ }, \
{ DBG_LCM_MSG, _RED_ }, \
{ DBG_MAIN, _YELLOW_ }, \
{ DBG_5, _GREEN_ }, \
{ DBG_6, _BLUE_ }, \
{ DBG_7, _MAGENTA_ }, \
{ DBG_8, _BCYAN_ }, \
{ DBG_9, _WHITE_ }, \
{ DBG_10, _CYAN_ }, \
{ DBG_11, _BGREEN_ }, \
{ DBG_12, _BYELLOW_ }, \
{ DBG_13, _CYAN_ }, \
{ DBG_14, _BBLUE_ }, \
{ DBG_15, _BMAGENTA_ }, \
{ DBG_16, _BWHITE_ } \

#define DBG_ENV     "LCM_DBG"


// ===================  do not modify after this line ==================

static long long dbg_modes = 0;
static short dbg_initiated = 0;

typedef struct dbg_mode {
    const char *d_name;
    unsigned long long d_mode;
} dbg_mode_t;

typedef struct dbg_mode_color {
    unsigned long long d_mode;
    const char *color;
} dbg_mode_color_t;


static dbg_mode_color_t dbg_colortab[] = {
    DBG_COLORTAB
};

static dbg_mode_t dbg_nametab[] = {
    DBG_NAMETAB
};

static inline 
const char* DCOLOR(unsigned long long d_mode)
{
    dbg_mode_color_t *mode;

    for (mode = dbg_colortab; mode->d_mode != 0; mode++)
    {
        if (mode->d_mode & d_mode)
            return mode->color;
    }

    return _BWHITE_;
}


static void dbg_init()
{
    const char *dbg_env;
    dbg_initiated = 1;

    dbg_modes = DBG_DEFAULT;

    dbg_env = getenv(DBG_ENV);
    if (!dbg_env) {
        return;
    } else {
        char env[256];
        char *name;

        strncpy(env, dbg_env, sizeof(env));
        for (name = strtok(env,","); name; name = strtok(NULL, ",")) {
            int cancel;
            dbg_mode_t *mode;

            if (*name == '-') {
                cancel = 1;
                name++;
            }
            else
                cancel = 0;

            for (mode = dbg_nametab; mode->d_name != NULL; mode++)
                if (strcmp(name, mode->d_name) == 0)
                    break;
            if (mode->d_name == NULL) {
                fprintf(stderr, "Warning: Unknown debug option: "
                        "\"%s\"\n", name);
                return;
            }

            if (cancel) 
            {
                dbg_modes &= ~mode->d_mode;
            }
            else
            {
                dbg_modes = dbg_modes | mode->d_mode;    
            }

        }
    }
}

#ifndef NO_DBG

#define dbg(mode, ...) { \
    if( !dbg_initiated) dbg_init(); \
    if( dbg_modes & (mode) ) { \
        printf("%s", DCOLOR(mode)); \
        printf(__VA_ARGS__); \
        printf(_NORMAL_); \
    } \
}
#define dbg_active(mode) (dbg_modes & (mode))

#else

#define dbg(mode, arg) 
#define dbg_active(mode) false
#define cdbg(mode,color,dtag,arg) 

#endif

#define cprintf(color, ...) { printf(color); printf(__VA_ARGS__); \
    printf(_NORMAL_); }



#ifdef __cplusplus
}
#endif
#endif
