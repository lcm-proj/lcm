#pragma once

#include <stdbool.h>


/**
 *  \brief Does str start with sub string?
 *  \param[in] str
 *  \param[in] sub the sub string searched in str
 */
bool starts_with(
	const char * str,
	const char * sub);


/**
 *  \brief Does str contain a sub string?
 *  \param[in] str
 *  \param[in] sub
 */
bool contains(
        const char * str,
        const char * sub);