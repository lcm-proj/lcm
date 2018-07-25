#ifndef __common_h__
#define __common_h__

#ifdef __cplusplus
extern "C" {
#endif

#include "lcmtest2_cross_package_t.h"
#include "lcmtest_multidim_array_t.h"
#include "lcmtest_node_t.h"
#include "lcmtest_primitives_list_t.h"
#include "lcmtest_primitives_t.h"

#ifndef _MSC_VER
char *_strdup(const char *src);
#endif

int check_lcmtest_multidim_array_t(const lcmtest_multidim_array_t *msg, int expected);
void fill_lcmtest_multidim_array_t(int num_children, lcmtest_multidim_array_t *result);
void clear_lcmtest_multidim_array_t(lcmtest_multidim_array_t *msg);

int check_lcmtest_node_t(const lcmtest_node_t *msg, int expected);
void fill_lcmtest_node_t(int num_children, lcmtest_node_t *result);
void clear_lcmtest_node_t(lcmtest_node_t *msg);

int check_lcmtest_primitives_list_t(const lcmtest_primitives_list_t *msg, int expected);
void fill_lcmtest_primitives_list_t(int num, lcmtest_primitives_list_t *msg);
void clear_lcmtest_primitives_list_t(lcmtest_primitives_list_t *msg);

int check_lcmtest_primitives_t(const lcmtest_primitives_t *msg, int expected);
void fill_lcmtest_primitives_t(int n, lcmtest_primitives_t *msg);
void clear_lcmtest_primitives_t(lcmtest_primitives_t *msg);

int check_lcmtest2_cross_package_t(const lcmtest2_cross_package_t *msg, int expected);
void fill_lcmtest2_cross_package_t(int n, lcmtest2_cross_package_t *msg);
void clear_lcmtest2_cross_package_t(lcmtest2_cross_package_t *msg);

int check_lcmtest2_another_type_t(const lcmtest2_another_type_t *msg, int expected);
void fill_lcmtest2_another_type_t(int n, lcmtest2_another_type_t *msg);
void clear_lcmtest2_another_type_t(lcmtest2_another_type_t *msg);

char *make_tmpnam();
void free_tmpnam(char *tmpnam);

#ifdef __cplusplus
}
#endif

#endif
