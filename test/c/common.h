#ifndef __common_h__
#define __common_h__

#include "lcmtest_primitives_t.h"
#include "lcmtest_primitives_list_t.h"
#include "lcmtest_node_t.h"
#include "lcmtest_multidim_array_t.h"

char* _strdup(const char* src);
int _lcm_handle_timeout(lcm_t* lcm, int ms);

int check_lcmtest_multidim_array_t(const lcmtest_multidim_array_t* msg, int expected);
void fill_lcmtest_multidim_array_t(int num_children, lcmtest_multidim_array_t* result);
void clear_lcmtest_multidim_array_t(lcmtest_multidim_array_t* msg);

int check_lcmtest_node_t(const lcmtest_node_t* msg, int expected);
void fill_lcmtest_node_t(int num_children, lcmtest_node_t* result);
void clear_lcmtest_node_t(lcmtest_node_t* msg);

int check_lcmtest_primitives_list_t(const lcmtest_primitives_list_t* msg, int expected);
void fill_lcmtest_primitives_list_t(int num, lcmtest_primitives_list_t* msg);
void clear_lcmtest_primitives_list_t(lcmtest_primitives_list_t* msg);

int check_lcmtest_primitives_t(const lcmtest_primitives_t* msg, int expected);
void fill_lcmtest_primitives_t(int n, lcmtest_primitives_t* msg);
void clear_lcmtest_primitives_t(lcmtest_primitives_t* msg);

#endif
