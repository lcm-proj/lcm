#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <string.h>
#include <ctype.h>

#ifdef WIN32
#include <lcm/windows/WinPorting.h>
#endif

#include "getopt.h"

#define GOO_BOOL_TYPE 1
#define GOO_STRING_TYPE 2

#ifndef MAX
#define MAX(a,b) ((a)>(b)?(a):(b))
#endif

getopt_t *getopt_create()
{
    getopt_t *gopt = (getopt_t*) calloc(1, sizeof(getopt_t));
    gopt->lopts = g_hash_table_new(g_str_hash, g_str_equal);
    gopt->sopts = g_hash_table_new(g_str_hash, g_str_equal);
    gopt->options = g_ptr_array_new();
    gopt->extraargs = g_ptr_array_new();

    return gopt;
}

void getopt_destroy(getopt_t *gopt)
{
    // XXX We need to free the members
    g_hash_table_destroy(gopt->lopts);
    g_hash_table_destroy(gopt->sopts);
    g_ptr_array_free(gopt->options, TRUE);
    g_ptr_array_free(gopt->extraargs, TRUE);

    free(gopt);
}

// returns 1 if no error
int getopt_parse(getopt_t *gopt, int argc, char *argv[], int showErrors)
{
    int okay = 1;
    GPtrArray *toks = g_ptr_array_new();

    // take the input stream and chop it up into tokens
    for (int i = 1; i < argc; i++) {
        char *arg = strdup(argv[i]);
        char *eq = strstr(arg, "=");

        // no equal sign? Push the whole thing.
        if (eq == NULL) {
            g_ptr_array_add(toks, strdup(arg));
        } else {
            // there was an equal sign. Push the part
            // before and after the equal sign
            char *val = &eq[1];
            eq[0] = 0;
            g_ptr_array_add(toks, arg);

            // if the part after the equal sign is
            // enclosed by quotation marks, strip them.
            if (val[0]=='\"') {
                int last = strlen(val) - 1;
                if (val[last]=='\"')
                    val[last] = 0;
                g_ptr_array_add(toks, &val[1]);
            } else {
                g_ptr_array_add(toks, val);
            }
        }
    }

    // now loop over the elements and evaluate the arguments
    unsigned int i = 0;
    while (i < toks->len) {

        char *tok = (char*) g_ptr_array_index(toks, i);

        if (!strncmp(tok,"--", 2)) {
            char *optname = &tok[2];
            getopt_option_t *goo = (getopt_option_t*) g_hash_table_lookup(gopt->lopts, optname);
            if (goo == NULL) {
                okay = 0;
                if (showErrors)
                    printf("Unknown option --%s\n", optname);
                i++;
                continue;
            }

            goo->was_specified = 1;

            if (goo->type == GOO_BOOL_TYPE) {
                if ((i+1) < toks->len) {
                    char *val = (char*) g_ptr_array_index(toks, i+1);

                    if (!strcmp(val,"true")) {
                        i+=2;
                        goo->svalue = "true";
                        continue;
                    }
                    if (!strcmp(val,"false")) {
                        i+=2;
                        goo->svalue = "false";
                        continue;
                    }
                }

                goo->svalue = "true";
                i++;
                continue;
            }

            if (goo->type == GOO_STRING_TYPE) {
                if ((i+1) < toks->len) {
                    char *val = (char*) g_ptr_array_index(toks, i+1);
                    i+=2;

                    goo->svalue = strdup(val);
                    continue;
                }

                okay = 0;
                if (showErrors)
                    printf("Option %s requires a string argument.\n",optname);
            }
        }

        if (!strncmp(tok,"-",1) && strncmp(tok,"--",2)) {
            int len = strlen(tok);
            int pos;
            for (pos = 1; pos < len; pos++) {
                char sopt[2];
                sopt[0] = tok[pos];
                sopt[1] = 0;
                getopt_option_t *goo = (getopt_option_t*) g_hash_table_lookup(gopt->sopts, sopt);

                if (goo==NULL) {
                    // is the argument a numerical literal that happens to be negative?
                    if (pos==1 && isdigit(tok[pos])) {
                        g_ptr_array_add(gopt->extraargs, tok);
                        break;
                    } else {
                        okay = 0;
                        if (showErrors)
                            printf("Unknown option -%c\n", tok[pos]);
                        i++;
                        continue;
                    }
                }

                goo->was_specified = 1;

                if (goo->type == GOO_BOOL_TYPE) {
                    goo->svalue = "true";
                    continue;
                }

                if (goo->type == GOO_STRING_TYPE) {
                    if ((i+1) < toks->len) {
                        char *val = (char*) g_ptr_array_index(toks, i+1);
                        if (val[0]=='-')
                        {
                            okay = 0;
                            if (showErrors)
                                printf("Ran out of arguments for option block %s\n", tok);
                        }
                        i++;

                        goo->svalue=strdup(val);
                        continue;
                    }

                    okay = 0;
                    if (showErrors)
                        printf("Option -%c requires a string argument.\n", tok[pos]);
                }
            }
            i++;
            continue;
        }

        // it's not an option-- it's an argument.
        g_ptr_array_add(gopt->extraargs, tok);
        i++;
    }

    return okay;
}

int getopt_was_specified(getopt_t *gopt, const char *lname)
{
   getopt_option_t *goo = (getopt_option_t*) g_hash_table_lookup(gopt->lopts, lname);
    if (goo == NULL)
        return 0;

    return goo->was_specified;

}

void getopt_add_spacer(getopt_t *gopt, const char *s)
{
    getopt_option_t *goo = (getopt_option_t*) calloc(1, sizeof(getopt_option_t));
    goo->spacer = 1;
    goo->help = strdup(s);
    g_ptr_array_add(gopt->options, goo);
}

void getopt_add_bool(getopt_t *gopt, char sopt, const char *lname, int def, const char *help)
{
    char sname[2];
    sname[0] = sopt;
    sname[1] = 0;

    getopt_option_t *goo = (getopt_option_t*) calloc(1, sizeof(getopt_option_t));
    goo->sname=strdup(sname);
    goo->lname=strdup(lname);
    goo->svalue=strdup(def ? "true" : "false");
    goo->type=GOO_BOOL_TYPE;
    goo->help=strdup(help);

    g_hash_table_insert(gopt->lopts, goo->lname, goo);
    g_hash_table_insert(gopt->sopts, goo->sname, goo);
    g_ptr_array_add(gopt->options, goo);
}

void getopt_add_int(getopt_t *gopt, char sopt, const char *lname, const char *def, const char *help)
{
    getopt_add_string(gopt, sopt, lname, def, help);
}

void getopt_add_string(getopt_t *gopt, char sopt, const char *lname, const char *def, const char *help)
{
    char sname[2];
    sname[0] = sopt;
    sname[1] = 0;

    getopt_option_t *goo = (getopt_option_t*) calloc(1, sizeof(getopt_option_t));
    goo->sname=strdup(sname);
    goo->lname=strdup(lname);
    goo->svalue=strdup(def);
    goo->type=GOO_STRING_TYPE;
    goo->help=strdup(help);

    g_hash_table_insert(gopt->lopts, goo->lname, goo);
    g_hash_table_insert(gopt->sopts, goo->sname, goo);
    g_ptr_array_add(gopt->options, goo);
}

char *getopt_get_string(getopt_t *gopt, const char *lname)
{
    getopt_option_t *goo = (getopt_option_t*) g_hash_table_lookup(gopt->lopts, lname);
    if (goo == NULL)
        return NULL;

    return goo->svalue;
}

int getopt_get_int(getopt_t *getopt, const char *lname)
{
    const char *v = getopt_get_string(getopt, lname);
    assert(v != NULL);
    return atoi(v);
}

int getopt_get_bool(getopt_t *getopt, const char *lname)
{
    const char *v = getopt_get_string(getopt, lname);
    assert (v!=NULL);
    return !strcmp(v, "true");
}

void getopt_do_usage(getopt_t *gopt)
{
    int leftmargin=2;
    int longwidth=12;
    int valuewidth=10;

    for (unsigned int i = 0; i < gopt->options->len; i++) {
        getopt_option_t *goo = (getopt_option_t*) g_ptr_array_index(gopt->options, i);

        if (goo->spacer)
            continue;

        longwidth = MAX(longwidth, strlen(goo->lname));

        if (goo->type == GOO_STRING_TYPE)
            valuewidth = MAX(valuewidth, strlen(goo->svalue));
    }

    for (unsigned int i = 0; i < gopt->options->len; i++) {
        getopt_option_t *goo = (getopt_option_t*) g_ptr_array_index(gopt->options, i);

        if (goo->spacer)
        {
            if (goo->help==NULL || strlen(goo->help)==0)
                printf("\n");
            else
                printf("\n%*s%s\n\n", leftmargin, "", goo->help);
            continue;
        }

        printf("%*s", leftmargin, "");

        if (goo->sname[0]==0)
            printf("     ");
        else
            printf("-%c | ", goo->sname[0]);

        printf("--%*s ", -longwidth, goo->lname);

        printf(" [ %s ]", goo->svalue);

        printf("%*s", (int) (valuewidth-strlen(goo->svalue)), "");

        printf(" %s   ", goo->help);
        printf("\n");
    }
}
