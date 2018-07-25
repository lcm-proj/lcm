#ifndef __common_hpp__
#define __common_hpp__

#include <lcm/lcm-cpp.hpp>
#include "lcmtest/multidim_array_t.hpp"
#include "lcmtest/node_t.hpp"
#include "lcmtest/primitives_list_t.hpp"
#include "lcmtest/primitives_t.hpp"
#include "lcmtest2/cross_package_t.hpp"

int CheckLcmType(const lcmtest::multidim_array_t *msg, int expected);
void FillLcmType(int num_children, lcmtest::multidim_array_t *result);
void ClearLcmType(lcmtest::multidim_array_t *msg);

int CheckLcmType(const lcmtest::node_t *msg, int expected);
void FillLcmType(int num_children, lcmtest::node_t *result);
void ClearLcmType(lcmtest::node_t *msg);

int CheckLcmType(const lcmtest::primitives_list_t *msg, int expected);
void FillLcmType(int num, lcmtest::primitives_list_t *msg);
void ClearLcmType(lcmtest::primitives_list_t *msg);

int CheckLcmType(const lcmtest::primitives_t *msg, int expected);
void FillLcmType(int n, lcmtest::primitives_t *msg);
void ClearLcmType(lcmtest::primitives_t *msg);

int CheckLcmType(const lcmtest2::cross_package_t *msg, int expected);
void FillLcmType(int n, lcmtest2::cross_package_t *msg);
void ClearLcmType(lcmtest2::cross_package_t *msg);

int CheckLcmType(const lcmtest2::another_type_t *msg, int expected);
void FillLcmType(int n, lcmtest2::another_type_t *msg);
void ClearLcmType(lcmtest2::another_type_t *msg);

#endif
