

#ifndef TPRK77_LCM_LCM_H
#define TPRK77_LCM_LCM_H

#ifdef __cplusplus
extern "C" {
#endif
#include "lua.h"
#include "lauxlib.h"
#ifdef __cplusplus
}
#endif

/**
 * @page LCM object related functions.
 *
 * @see ll_lcm_makemetatable
 * @see ll_lcm_register_new
 */

void ll_lcm_makemetatable(lua_State *);
void ll_lcm_register_new(lua_State *);

#endif

